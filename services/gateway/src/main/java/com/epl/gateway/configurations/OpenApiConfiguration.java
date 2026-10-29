package com.epl.gateway.configurations;

import com.epl.gateway.rpc.ProtoSchemas;
import com.epl.gateway.rpc.RpcRegistry;
import com.epl.gateway.rpc.RpcRoute;
import io.grpc.MethodDescriptor.MethodType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Swagger UI ({@code /api/swagger-ui.html}) and OpenAPI document ({@code /api/v3/api-docs}).
 * The RPCs are not controller methods, so one operation per unary / server-streaming RPC of the
 * {@link RpcRegistry} is added here, with the proto3 JSON schemas of its messages.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String BEARER = "bearer";
    /** Error body of every failed call; referenced by controllers as {@link #API_ERROR_REF}. */
    public static final String API_ERROR_REF = "#/components/schemas/ApiError";
    private static final String API_ERROR = "ApiError";
    private static final List<String> TENANT_HEADERS = List.of("X-Company", "X-Warehouse", "X-Customer");

    @Bean
    public OpenAPI eplOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EPL gateway")
                        .version("v1")
                        .description("""
                                Every backend RPC as `POST /rpc/{package.Service}/{Method}` with a proto3 JSON body.
                                Sign in with `epl.auth.v1.AuthService/Login` (clientId `PC_WEB`), then **Authorize**
                                with the `accessToken`. Tenant-scoped RPCs need the X-Company / X-Warehouse / X-Customer headers."""))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }

    @Bean
    public OpenApiCustomizer rpcOperations(RpcRegistry registry, GatewayProperties properties) {
        return openApi -> {
            var components = openApi.getComponents();
            if (components.getSchemas() == null) {
                components.setSchemas(new LinkedHashMap<>());
            }
            components.addSchemas(API_ERROR, new ObjectSchema()
                    .addProperty("code", new StringSchema().description("gRPC status code name").example("NOT_FOUND"))
                    .addProperty("message", new StringSchema())
                    .addProperty("requestId", new StringSchema()));
            for (var header : TENANT_HEADERS) {
                components.addParameters(header, new HeaderParameter().name(header).required(false).schema(new StringSchema()));
            }

            var schemas = new ProtoSchemas(components.getSchemas());
            var matcher = new AntPathMatcher();
            for (var route : registry.routes()) {
                if (route.type() != MethodType.UNARY && route.type() != MethodType.SERVER_STREAMING) {
                    // no /rpc operation, but controllers (UploadController) refer to their messages
                    schemas.ref(route.requestPrototype().getDescriptorForType());
                    schemas.ref(route.responsePrototype().getDescriptorForType());
                    continue;
                }
                var path = "/rpc/" + route.fullMethodName();
                var isPublic = properties.security().publicPaths().stream().anyMatch(pattern -> matcher.match(pattern, path));
                openApi.path(path, new PathItem().post(operation(route, schemas, isPublic)));
            }
        };
    }

    private static Operation operation(RpcRoute route, ProtoSchemas schemas, boolean isPublic) {
        var descriptor = route.method();
        var streaming = route.type() == MethodType.SERVER_STREAMING;
        var operation = new Operation()
                .addTagsItem(descriptor.getServiceName())
                .summary(descriptor.getBareMethodName())
                .operationId(route.fullMethodName().replaceAll("[^A-Za-z0-9]", "_"))
                .description("Backend `" + route.backend() + "`, " + route.type()
                        + (streaming ? ": one JSON message per line (NDJSON)" : ""))
                .requestBody(new RequestBody().required(true).content(json(schemas.ref(route.requestPrototype().getDescriptorForType()))))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse().description("OK").content(new Content().addMediaType(
                                streaming ? "application/x-ndjson" : "application/json",
                                new MediaType().schema(schemas.ref(route.responsePrototype().getDescriptorForType())))))
                        .addApiResponse("default", new ApiResponse()
                                .description("gRPC status mapped to HTTP (404 NOT_FOUND, 400 INVALID_ARGUMENT, 401, 403 …)")
                                .content(json(new Schema<>().$ref(API_ERROR_REF)))));
        if (isPublic) {
            // no token needed
            operation.setSecurity(List.of());
        } else {
            addTenantHeaders(operation);
        }
        return operation;
    }

    /** Tenant headers on the controller methods marked {@link TenantScoped}. */
    @Bean
    public OperationCustomizer tenantHeaders() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.hasMethodAnnotation(TenantScoped.class)) {
                addTenantHeaders(operation);
            }
            return operation;
        };
    }

    private static void addTenantHeaders(Operation operation) {
        TENANT_HEADERS.forEach(header -> operation.addParametersItem(new Parameter().$ref("#/components/parameters/" + header)));
    }

    private static Content json(Schema<?> schema) {
        return new Content().addMediaType("application/json", new MediaType().schema(schema));
    }
}
