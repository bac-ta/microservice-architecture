package com.epl.gateway.configurations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller method forwarding the tenant headers (X-Company, X-Warehouse, X-Customer) to its backend:
 * {@link OpenApiConfiguration} documents them on its operation.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantScoped {
}
