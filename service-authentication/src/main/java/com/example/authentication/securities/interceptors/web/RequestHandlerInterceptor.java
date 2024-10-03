package com.example.authentication.securities.interceptors.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;

import java.util.Collections;

public class RequestHandlerInterceptor extends WebRequestHandlerInterceptorAdapter {

    public RequestHandlerInterceptor(WebRequestInterceptor requestInterceptor) {
        super(requestInterceptor);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        var req = new OAuth2Request(Maps.newHashMap(),
                "", Lists.newArrayList(), false, Sets.newHashSet(), Sets.newHashSet(),
                "", Sets.newHashSet(), Maps.newHashMap());

        var token = new UsernamePasswordAuthenticationToken("user", "N/A", Collections.emptyList());
        var authentication = new OAuth2Authentication(req, token);

        var context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        return true;
    }

}
