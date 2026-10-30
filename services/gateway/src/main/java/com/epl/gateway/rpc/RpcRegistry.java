package com.epl.gateway.rpc;

import com.epl.gateway.configurations.GatewayProperties;
import io.grpc.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Every RPC of the generated {@code *Grpc} classes found in {@code epl.gateway.scan-packages}, by HTTP path.
 * A new service or RPC in proto/ is reachable through the gateway without any change here.
 */
@Component
public class RpcRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRegistry.class);
    private static final Pattern VERSION_SEGMENT = Pattern.compile("v\\d+\\w*");
    private static final Pattern WORD_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
    private static final String SERVICE_SUFFIX = "Service";

    private final Map<String, RpcRoute> routes;

    public RpcRegistry(GatewayProperties properties) {
        this.routes = Collections.unmodifiableMap(scan(properties.scanPackages()));
        logger.info("Gateway exposes {} RPCs from packages {}", routes.size(), properties.scanPackages());
    }

    /**
     * @param path path below {@code /rpc}, e.g. {@code /inventory/v1/stock/search}
     */
    public Optional<RpcRoute> find(String path) {
        return Optional.ofNullable(routes.get(path));
    }

    public Collection<RpcRoute> routes() {
        return routes.values();
    }

    /**
     * Backend serving a service, from its proto package: the last package segment that is not a version.
     * {@code epl.inventory.v1.StockService -> inventory}, {@code user.UserService -> user}.
     */
    public static String backendOf(String fullServiceName) {
        var segments = List.of(fullServiceName.split("\\."));
        for (int i = segments.size() - 2; i >= 0; i--) {
            if (!VERSION_SEGMENT.matcher(segments.get(i)).matches()) {
                return segments.get(i);
            }
        }
        throw new IllegalArgumentException("Service " + fullServiceName + " has no proto package");
    }

    /**
     * Path of an RPC below {@code /rpc}, lowercase words joined by '-': the proto package from the backend on,
     * the service without its "Service" suffix, the method.
     * {@code epl.masterdata.v1.ProductService/GetProduct -> /masterdata/v1/product/get-product}.
     */
    public static String pathOf(String fullMethodName) {
        var service = fullMethodName.substring(0, fullMethodName.indexOf('/'));
        var method = fullMethodName.substring(fullMethodName.indexOf('/') + 1);
        var backend = backendOf(service);
        var packageName = service.substring(0, service.lastIndexOf('.'));
        var serviceName = service.substring(service.lastIndexOf('.') + 1);
        if (serviceName.endsWith(SERVICE_SUFFIX) && serviceName.length() > SERVICE_SUFFIX.length()) {
            serviceName = serviceName.substring(0, serviceName.length() - SERVICE_SUFFIX.length());
        }

        var path = new StringBuilder();
        var segments = List.of(packageName.split("\\."));
        for (var segment : segments.subList(segments.lastIndexOf(backend), segments.size())) {
            path.append('/').append(kebab(segment));
        }
        return path.append('/').append(kebab(serviceName)).append('/').append(kebab(method)).toString();
    }

    private static String kebab(String name) {
        return WORD_BOUNDARY.matcher(name).replaceAll("-").replace('_', '-').toLowerCase(Locale.ROOT);
    }

    private static Map<String, RpcRoute> scan(List<String> packages) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*Grpc")));

        var routes = new TreeMap<String, RpcRoute>();
        for (var basePackage : packages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                var service = serviceDescriptor(candidate.getBeanClassName());
                var backend = backendOf(service.getName());
                for (var method : service.getMethods()) {
                    var route = RpcRoute.of(backend, method);
                    var previous = routes.put(route.path(), route);
                    if (previous != null) {
                        throw new IllegalStateException(previous.fullMethodName() + " and " + route.fullMethodName()
                                + " both map to /rpc" + route.path());
                    }
                }
            }
        }
        return routes;
    }

    private static ServiceDescriptor serviceDescriptor(String grpcClassName) {
        try {
            var grpcClass = ClassUtils.forName(grpcClassName, RpcRegistry.class.getClassLoader());
            return (ServiceDescriptor) grpcClass.getMethod("getServiceDescriptor").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Not a generated gRPC class: " + grpcClassName, e);
        }
    }
}
