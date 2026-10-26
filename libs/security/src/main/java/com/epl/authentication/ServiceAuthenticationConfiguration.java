package com.epl.authentication;

import com.epl.authentication.exceptions.ExceptionHandler;
import com.epl.authentication.exceptions.ServiceException;
import com.epl.authentication.exceptions.ServiceExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceAuthenticationConfiguration {

    @Bean
    public ExceptionHandler<ServiceException> serviceExceptionHandler() {
        return new ServiceExceptionHandler();
    }

}
