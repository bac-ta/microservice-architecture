package com.example.gateway.rpc;

import com.example.gateway.configurations.GatewayProperties;
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
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Every RPC of the generated {@code *Grpc} classes found in {@code epl.gateway.scan-packages}, by full method name.
 * A new service or RPC in proto/ is reachable through the gateway without any change here.
 */
@Component
public class RpcRegistry {

    private static final Logger logger = LoggerFactory.getLogger(RpcRegistry.class);
    private static final Pattern VERSION_SEGMENT = Pattern.compile("v\\d+\\w*");

    private final Map<String, RpcRoute> routes;

    public RpcRegistry(GatewayProperties properties) {
        this.routes = Collections.unmodifiableMap(scan(properties.scanPackages()));
        logger.info("Gateway exposes {} RPCs from packages {}", routes.size(), properties.scanPackages());
    }

    /**
     * @param service full service name, e.g. {@code epl.inventory.v1.StockService}
     * @param method  method name, e.g. {@code Search}
     */
    public Optional<RpcRoute> find(String service, String method) {
        return Optional.ofNullable(routes.get(service + "/" + method));
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

    private static Map<String, RpcRoute> scan(List<String> packages) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*Grpc")));

        var routes = new TreeMap<String, RpcRoute>();
        for (var basePackage : packages) {
            for (var candidate : scanner.findCandidateComponents(basePackage)) {
                var service = serviceDescriptor(candidate.getBeanClassName());
                var backend = backendOf(service.getName());
                for (var method : service.getMethods()) {
                    routes.put(method.getFullMethodName(), RpcRoute.of(backend, method));
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
