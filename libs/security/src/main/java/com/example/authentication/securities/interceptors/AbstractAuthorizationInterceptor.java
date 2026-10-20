package com.example.authentication.securities.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.lang.annotation.Annotation;

public abstract class AbstractAuthorizationInterceptor extends WebRequestHandlerInterceptorAdapter {
    public AbstractAuthorizationInterceptor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }

    // Variables
    // ------------------------------------------------------------------------

    // Constructor
    // ------------------------------------------------------------------------

    // Override Methods
    // ------------------------------------------------------------------------
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {

        var hm = (HandlerMethod) handler;
        var method = hm.getMethod();
        var annotation = AnnotationUtils.findAnnotation(method, Annotation.class);
        return true;

//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        return false;
    }

}
