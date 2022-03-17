package com.entropy.authentication;

import com.entropy.authentication.exception.ServiceExceptionHandler;
import com.entropy.authentication.exception.ErrorResponse;
import com.entropy.authentication.exception.ExceptionHandler;
import com.entropy.authentication.exception.ServiceException;
import io.grpc.StatusRuntimeException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrameworkConfiguration { 
    @Bean
    public ExceptionHandler<StatusRuntimeException> statusRuntimeExceptionHandler() {
        return new ExceptionHandler<StatusRuntimeException>() {
            @Override
            public ErrorResponse handle(StatusRuntimeException exception) {
                return null;
            }
        };
    }

    @Bean
    public ExceptionHandler<ServiceException> serviceExceptionHandler() {
        return new ServiceExceptionHandler();
    }
    
}
