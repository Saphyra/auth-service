package com.github.saphyra.authservice.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, LoginTest.class})
public class LoginTest {

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
