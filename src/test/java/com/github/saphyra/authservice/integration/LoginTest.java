package com.github.saphyra.authservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.domain.LoginRequest;
import com.github.saphyra.authservice.integration.component.MockMvcWrapper;
import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, LoginTest.class})
public class LoginTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;


    @Test
    public void loginByRest_successful() throws Exception {
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/login", true, loginRequest);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void loginByRest_unauthorized() {

    }

    @Test
    public void loginByForm_successful() {

    }

    @Test
    public void loginByForm_unauthorized() {

    }
}
