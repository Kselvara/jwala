package com.cerner.jwala.web.controller;

import com.cerner.jwala.web.controller.SamlController;
import com.siemens.cto.security.saml.service.SamlIdentityProviderService;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by Z003BPEJ on 6/18/14.
 */
public class SamlControllerTest {

    final SamlController controller = new SamlController();

    @Test
    public void testIdProvider() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        SamlIdentityProviderService mockService = mock(SamlIdentityProviderService.class);
        controller.setSamlIdentityProviderService(mockService);
        
        assertEquals("saml/post", controller.idProvider(mockRequest, mockResponse));
        
        verify(mockService).createSamlResponse(mockRequest, mockResponse);
    }
}