package com.github.saphyra.integration.component;

import static java.util.Objects.isNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.Optional;

import javax.servlet.http.Cookie;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.github.saphyra.util.ObjectMapperWrapper;
import lombok.RequiredArgsConstructor;

@TestComponent
@RequiredArgsConstructor
public class MockMvcWrapper {
    private final MockMvc mockMvc;
    private final ObjectMapperWrapper objectMapperWrapper;

    public MockHttpServletResponse postRequest(String uri, boolean isRest, Object requestBody, Cookie... cookies) throws Exception {
        MockHttpServletRequestBuilder request = post(uri);

        return sendRequest(request, isRest, requestBody, cookies);
    }

    public MockHttpServletResponse putRequest(String uri, boolean isRest, Object requestBody, Cookie... cookies) throws Exception {
        MockHttpServletRequestBuilder request = put(uri);

        return sendRequest(request, isRest, requestBody, cookies);
    }

    private MockHttpServletResponse sendRequest(MockHttpServletRequestBuilder request, boolean isRest, Object requestBody, Cookie[] cookies) throws Exception {
        if (!isNull(cookies) && cookies.length > 0) {
            request.cookie(cookies);
        }

        if (isRest) {
            request.header("RequestType", "rest")
                .content(objectMapperWrapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON);
        } else {
            request.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(Optional.ofNullable(requestBody).map(Object::toString).orElse(""));
        }

        return sendRequest(request);
    }

    private MockHttpServletResponse sendRequest(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse();
    }
}
