package com.epl.gateway;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * POST /m/graphql: queries answer asynchronously (fields run on virtual threads), mutations synchronously.
 */
final class GraphQlRequests {

    private GraphQlRequests() {
    }

    /** @param body JSON request body, e.g. {@code {"query": "..."}} */
    static ResultActions graphql(MockMvc mvc, String body, HttpHeaders headers) throws Exception {
        var result = mvc.perform(post("/m/graphql").contentType(MediaType.APPLICATION_JSON).headers(headers).content(body))
                .andReturn();
        return result.getRequest().isAsyncStarted() ? mvc.perform(asyncDispatch(result)) : completed(result);
    }

    private static ResultActions completed(MvcResult result) {
        return new ResultActions() {
            @Override
            public ResultActions andExpect(ResultMatcher matcher) throws Exception {
                matcher.match(result);
                return this;
            }

            @Override
            public ResultActions andDo(ResultHandler handler) throws Exception {
                handler.handle(result);
                return this;
            }

            @Override
            public MvcResult andReturn() {
                return result;
            }
        };
    }
}
