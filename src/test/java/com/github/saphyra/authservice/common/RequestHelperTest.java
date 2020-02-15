package com.github.saphyra.authservice.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class RequestHelperTest {
    private static final String REQUEST_TYPE_HEADER = "request_type_header";
    private static final String REQUEST_TYPE_VALUE = "request_type_value";
    @Mock
    private CommonAuthProperties commonAuthProperties;

    @InjectMocks
    private RequestHelper underTest;

    @Mock
    private HttpServletRequest request;

    @Before
    public void setUp() {
        given(commonAuthProperties.getRequestTypeHeader()).willReturn(REQUEST_TYPE_HEADER);
        given(commonAuthProperties.getRestTypeValue()).willReturn(REQUEST_TYPE_VALUE);
    }

    @Test
    public void getMethod() {
        //GIVEN
        given(request.getMethod()).willReturn(HttpMethod.POST.name());
        //WHEN
        HttpMethod result = underTest.getMethod(request);
        //THEN
        assertThat(result).isEqualTo(HttpMethod.POST);
    }

    @Test
    public void isRestCall_true() {
        //GIVEN
        given(request.getHeader(REQUEST_TYPE_HEADER)).willReturn(REQUEST_TYPE_VALUE);
        //WHEN
        boolean result = underTest.isRestCall(request);
        //THEN
        assertThat(result).isTrue();
    }

    @Test
    public void isRestCall_false() {
        //GIVEN
        given(request.getHeader(REQUEST_TYPE_HEADER)).willReturn(null);
        //WHEN
        boolean result = underTest.isRestCall(request);
        //THEN
        assertThat(result).isFalse();
    }
}