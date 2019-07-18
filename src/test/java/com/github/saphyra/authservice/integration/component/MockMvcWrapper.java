package com.github.saphyra.authservice.integration.component;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    public MockHttpServletResponse postRequest(String uri, boolean isRest, Object requestBody) throws Exception {
        MockHttpServletRequestBuilder request = post(uri);

        if (isRest) {
            request.header("RequestType", "rest")
                .content(objectMapperWrapper.writeValueAsString(requestBody))
                .contentType(MediaType.APPLICATION_JSON);
        } else {
            request.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(requestBody.toString());
        }
        return sendRequest(request);
    }

    private MockHttpServletResponse sendRequest(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn().getResponse();
    }
}
