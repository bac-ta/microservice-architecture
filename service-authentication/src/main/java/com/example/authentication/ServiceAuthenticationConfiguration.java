package com.example.authentication;

import com.example.authentication.exceptions.ExceptionHandler;
import com.example.authentication.exceptions.ServiceException;
import com.example.authentication.exceptions.ServiceExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceAuthenticationConfiguration {

    @Bean
    public ExceptionHandler<ServiceException> serviceExceptionHandler() {
        return new ServiceExceptionHandler();
    }

}
