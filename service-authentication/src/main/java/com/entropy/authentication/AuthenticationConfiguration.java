package com.entropy.authentication;

import com.entropy.authentication.exceptions.ExceptionHandler;
import com.entropy.authentication.exceptions.ServiceException;
import com.entropy.authentication.exceptions.ServiceExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfiguration {

    @Bean
    public ExceptionHandler<ServiceException> serviceExceptionHandler() {
        return new ServiceExceptionHandler();
    }

}
