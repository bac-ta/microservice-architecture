package com.epl.gateway.graphql;

import com.epl.gateway.configurations.GatewayProperties;
import com.epl.gateway.rpc.ProtoJson;
import com.epl.gateway.rpc.RpcInvoker;
import com.epl.gateway.rpc.RpcRegistry;
import com.epl.gateway.rpc.RpcRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.Status;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * GraphQL schema of the mobile API ({@code POST /api/m/graphql}), generated from every unary RPC of the
 * {@link RpcRegistry}, so a new RPC in proto/ is reachable without any change here:
 * <pre>
 * query    { masterdata { product { getProduct(input: {code: "P001"}) { code names } } } }
 * mutation { inventory { stock { relocate(input: {...}) { movementId } } } }
 * </pre>
 * Namespace = backend (+ the version after v1, e.g. {@code inventoryV2}), then the service without its "Service"
 * suffix; field = method in lowerCamelCase, with the request message as {@code input}. Get / List / Search /
 * Resolve / Check RPCs are queries, the others mutations. Server- and client-streaming RPCs and the RPCs of
 * {@code epl.gateway.graphql.exclude} are not mapped.
 * Each field is one gRPC call through the same proto3 JSON mapping as /rpc (see {@link ProtoGraphQlTypes}), with the
 * selected fields as {@code x-field-mask} ({@link FieldMasks}): the backend sends, and may only load, those fields.
 * Query fields run in parallel on virtual threads (one screen loads its data in one round trip); mutation
 * fields run one after the other, in document order, as the GraphQL spec requires.
 */
@Component
public class MobileGraphQlSchema implements DisposableBean {

    /** gRPC metadata of the HTTP request, in the GraphQL context (set by GraphQlConfiguration). */
    public static final String METADATA = MobileGraphQlSchema.class.getName() + ".metadata";
    /** True when the request carries a verified JWT. */
    public static final String AUTHENTICATED = MobileGraphQlSchema.class.getName() + ".authenticated";

    private static final Pattern QUERY_METHOD = Pattern.compile("(Get|List|Search|Resolve|Check|BatchCheck)([A-Z]\\w*)?");
    private static final String SERVICE_SUFFIX = "Service";
    private static final String INPUT = "input";
    private static final DataFetcher<Object> NAMESPACE = env -> Map.of();
    private static final Metadata.Key<String> FIELD_MASK = Metadata.Key.of("x-field-mask", Metadata.ASCII_STRING_MARSHALLER);
    /** Well below the 8 KiB gRPC metadata limit; a larger selection asks for the full response. */
    private static final int MAX_FIELD_MASK_LENGTH = 4096;

    private final RpcInvoker invoker;
    private final ObjectMapper objectMapper;
    private final GatewayProperties.Security security;
    private final ExecutorService queryExecutor = Executors.newVirtualThreadPerTaskExecutor();
    /** "Type.field" of every field that calls an RPC. */
    private final Set<String> rpcFields = new HashSet<>();
    private final GraphQLSchema schema;

    public MobileGraphQlSchema(RpcRegistry registry, RpcInvoker invoker, ObjectMapper objectMapper, GatewayProperties properties) {
        this.invoker = invoker;
        this.objectMapper = objectMapper;
        this.security = properties.security();
        var matcher = new AntPathMatcher();
        var exclude = properties.graphql().exclude();
        this.schema = build(registry.routes().stream()
                .filter(route -> route.type() == MethodType.UNARY)
                .filter(route -> exclude.stream().noneMatch(pattern -> matcher.match(pattern, route.fullMethodName())))
                .toList());
    }

    public GraphQLSchema schema() {
        return schema;
    }

    /** True when the field calls a backend RPC (counted against epl.gateway.graphql.max-rpc-calls). */
    public boolean isRpcField(String typeName, String fieldName) {
        return rpcFields.contains(typeName + "." + fieldName);
    }

    @Override
    public void destroy() {
        queryExecutor.close();
    }

    private GraphQLSchema build(List<RpcRoute> routes) {
        var types = new ProtoGraphQlTypes(routes.stream()
                .flatMap(route -> Stream.of(route.requestPrototype().getDescriptorForType(),
                        route.responsePrototype().getDescriptorForType()))
                .toList());
        var code = GraphQLCodeRegistry.newCodeRegistry();

        // root -> namespace -> service -> RPCs
        var queries = new TreeMap<String, TreeMap<String, List<RpcRoute>>>();
        var mutations = new TreeMap<String, TreeMap<String, List<RpcRoute>>>();
        for (var route : routes) {
            var root = QUERY_METHOD.matcher(route.method().getBareMethodName()).matches() ? queries : mutations;
            root.computeIfAbsent(namespace(route), key -> new TreeMap<>())
                    .computeIfAbsent(serviceField(route), key -> new ArrayList<>())
                    .add(route);
        }

        var schema = GraphQLSchema.newSchema().query(root("Query", "Queries", queries, types, code, true));
        if (!mutations.isEmpty()) {
            schema.mutation(root("Mutation", "Mutations", mutations, types, code, false));
        }
        return schema.additionalTypes(types.additionalTypes()).codeRegistry(code.build()).build();
    }

    private GraphQLObjectType root(String name, String suffix, Map<String, TreeMap<String, List<RpcRoute>>> namespaces,
                                   ProtoGraphQlTypes types, GraphQLCodeRegistry.Builder code, boolean parallel) {
        var root = GraphQLObjectType.newObject().name(name);
        namespaces.forEach((namespace, services) -> {
            var namespaceTypeName = capitalize(namespace) + suffix;
            var namespaceType = GraphQLObjectType.newObject().name(namespaceTypeName)
                    .description("Backend " + services.firstEntry().getValue().getFirst().backend());
            services.forEach((service, serviceRoutes) -> {
                var serviceTypeName = capitalize(namespace) + capitalize(service) + suffix;
                var serviceType = GraphQLObjectType.newObject().name(serviceTypeName)
                        .description(serviceRoutes.getFirst().method().getServiceName());
                for (var route : serviceRoutes) {
                    var field = operation(route, types);
                    serviceType.field(field);
                    code.dataFetcher(FieldCoordinates.coordinates(serviceTypeName, field.getName()),
                            parallel ? inParallel(call(route)) : call(route));
                    rpcFields.add(serviceTypeName + "." + field.getName());
                }
                namespaceType.field(GraphQLFieldDefinition.newFieldDefinition().name(service)
                        .type(GraphQLNonNull.nonNull(serviceType.build())));
                code.dataFetcher(FieldCoordinates.coordinates(namespaceTypeName, service), NAMESPACE);
            });
            root.field(GraphQLFieldDefinition.newFieldDefinition().name(namespace)
                    .type(GraphQLNonNull.nonNull(namespaceType.build())));
            code.dataFetcher(FieldCoordinates.coordinates(name, namespace), NAMESPACE);
        });
        return root.build();
    }

    private static GraphQLFieldDefinition operation(RpcRoute route, ProtoGraphQlTypes types) {
        var field = GraphQLFieldDefinition.newFieldDefinition()
                .name(lowerCamel(route.method().getBareMethodName()))
                .description("`" + route.fullMethodName() + "` on backend `" + route.backend() + "` (POST /rpc" + route.path() + ")")
                .type(types.output(route.responsePrototype().getDescriptorForType()));
        var input = types.input(route.requestPrototype().getDescriptorForType());
        if (input != null) {
            field.argument(GraphQLArgument.newArgument().name(INPUT).type(input));
        }
        return field.build();
    }

    private DataFetcher<Object> call(RpcRoute route) {
        var isPublic = isPublic(route);
        return env -> {
            var context = env.getGraphQlContext();
            if (security.enabled() && !isPublic && !Boolean.TRUE.equals(context.get(AUTHENTICATED))) {
                throw Status.UNAUTHENTICATED
                        .withDescription("Sign in with auth.auth.login, then send Authorization: Bearer <accessToken>")
                        .asRuntimeException();
            }
            Map<String, Object> input = env.getArgument(INPUT);
            var request = ProtoJson.parse(input == null ? null : objectMapper.writeValueAsString(input), route.requestPrototype());
            Metadata metadata = context.getOrDefault(METADATA, new Metadata());
            var response = invoker.unary(route.backend(), route.method(), request, withFieldMask(metadata, route, env));
            return objectMapper.readValue(ProtoJson.print(response), Object.class);
        };
    }

    /**
     * The request metadata plus x-field-mask: the backend returns (and may only load) the selected fields.
     * Shared by the fields of the request, so copied. Without a usable mask, the full response is asked for.
     */
    private static Metadata withFieldMask(Metadata metadata, RpcRoute route, DataFetchingEnvironment env) {
        var paths = FieldMasks.of(route.responsePrototype().getDescriptorForType(), env.getSelectionSet());
        if (paths.isEmpty() || paths.length() > MAX_FIELD_MASK_LENGTH) {
            return metadata;
        }
        var call = new Metadata();
        call.merge(metadata);
        call.put(FIELD_MASK, paths);
        return call;
    }

    /** Runs the blocking gRPC call on a virtual thread, so that sibling query fields wait for their backends together. */
    private DataFetcher<Object> inParallel(DataFetcher<Object> call) {
        return env -> {
            var mdc = MDC.getCopyOfContextMap();
            return CompletableFuture.supplyAsync(() -> {
                if (mdc != null) {
                    MDC.setContextMap(mdc);
                }
                try {
                    return call.get(env);
                } catch (Exception e) {
                    throw new CompletionException(e);
                } finally {
                    MDC.clear();
                }
            }, queryExecutor);
        };
    }

    /** Public in GraphQL when its /rpc path is one of epl.gateway.security.public-paths (Login, Refresh …). */
    private boolean isPublic(RpcRoute route) {
        var matcher = new AntPathMatcher();
        var path = "/rpc" + route.path();
        return security.publicPaths().stream().anyMatch(pattern -> matcher.match(pattern, path));
    }

    /** {@code epl.inventory.v1 -> inventory}, {@code epl.inventory.v2 -> inventoryV2}. */
    private static String namespace(RpcRoute route) {
        var service = route.method().getServiceName();
        var segments = List.of(service.substring(0, service.lastIndexOf('.')).split("\\."));
        var namespace = new StringBuilder(lowerCamel(route.backend()));
        for (var segment : segments.subList(segments.lastIndexOf(route.backend()) + 1, segments.size())) {
            if (!segment.equals("v1")) {
                namespace.append(capitalize(segment));
            }
        }
        return namespace.toString();
    }

    /** {@code StockService -> stock}. */
    private static String serviceField(RpcRoute route) {
        var name = route.method().getServiceName().substring(route.method().getServiceName().lastIndexOf('.') + 1);
        if (name.endsWith(SERVICE_SUFFIX) && name.length() > SERVICE_SUFFIX.length()) {
            name = name.substring(0, name.length() - SERVICE_SUFFIX.length());
        }
        return lowerCamel(name);
    }

    private static String lowerCamel(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
