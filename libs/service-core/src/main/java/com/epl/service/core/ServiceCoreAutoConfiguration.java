package com.epl.service.core;

import com.epl.database.DatabasePersistenceAutoConfiguration;
import com.epl.database.DatabaseSessionSettings;
import com.epl.grpc.server.services.GrpcServerInterceptorInitializerService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AutoConfiguration(before = DatabasePersistenceAutoConfiguration.class)
@EnableConfigurationProperties(ServiceCoreProperties.class)
public class ServiceCoreAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public TenantResolver tenantResolver(ServiceCoreProperties properties) {
        return new GrpcTenantResolver(properties.tenancy());
    }

    @Bean
    public RequestContextServerInterceptor requestContextServerInterceptor(TenantResolver tenantResolver) {
        return new RequestContextServerInterceptor(tenantResolver);
    }

    /**
     * Interceptors of every @GrpcServer of the service. The last one runs first: the call log (request id,
     * status, duration) wraps everything, including a request context that cannot be resolved; the response
     * mask (x-field-mask) is closest to the service.
     */
    @ConditionalOnMissingBean(name = "eplServerInterceptors")
    @Bean
    public GrpcServerInterceptorInitializerService eplServerInterceptors(RequestContextServerInterceptor requestContext) {
        return () -> List.of(new ResponseMaskServerInterceptor(), requestContext, new CallLoggingServerInterceptor());
    }

    /** RLS context of every transaction = the tenant of the current gRPC call. */
    @Bean
    public DatabaseSessionSettings databaseSessionSettings() {
        return () -> {
            var context = RequestContext.current();
            var settings = new HashMap<String, String>();
            settings.put("app.tenant_id", text(context.tenantId()));
            settings.put("app.company_id", text(context.companyId()));
            settings.put("app.warehouse_id", text(context.warehouseId()));
            settings.put("app.customer_id", text(context.customerId()));
            return settings;
        };
    }

    @Bean
    public DocumentNumbers documentNumbers() {
        return new DocumentNumbers();
    }

    @Bean
    public AuditorAware<String> eplAuditorAware() {
        return () -> Optional.of(Optional.ofNullable(RequestContext.current().user()).orElse("system"));
    }

    private static String text(UUID id) {
        return id == null ? "" : id.toString();
    }
}
