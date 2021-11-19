package com.tanerinal.springsecurityldapjwtmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.TestConstants;
import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthRequest;
import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthResponse;
import com.tanerinal.springsecurityldapjwtmvc.service.PortalUserService;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticateControllerTest {
    private static final String URL_AUTHENTICATE = "/authenticate";
    private static final String RESPONSE_FIELD_PATH_USERROLES = "$.userRoles";
    private static final String RESPONSE_FIELD_PATH_TOKEN = "$.token";
    private static final String RESPONSE_FIELD_PATH_USERNAME = "$.username";

    private MockMvc mockMvc;
    private AuthRequest authRequest = AuthRequest.builder()
            .username(TestConstants.TEST_AUTH_USERNAME)
            .password(TestConstants.TEST_AUTH_PASSWORD)
            .build();

    @InjectMocks
    private AuthenticateController controller;

    @Mock
    private PortalUserService service;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @SneakyThrows
    @Test
    public void testAuthenticateWhenProperRequestShouldReturnProperAuthentication() {
        String token = "token";
        List<String> userRoles = Arrays.asList("role1", "role2");
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .message(Constants.MESSAGE_SUCCESS)
                .status(Constants.STATUS_CODE_SUCCESS)
                .username(TestConstants.TEST_AUTH_USERNAME)
                .userRoles(userRoles)
                .build();

        when(service.authenticateUser(authRequest.getUsername(), authRequest.getPassword())).thenReturn(authResponse);

        ObjectMapper om = new ObjectMapper();
        mockMvc.perform(MockMvcRequestBuilders.post(URL_AUTHENTICATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(authRequest)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath(TestConstants.RESPONSE_FIELD_PATH_STATUS).value(Constants.STATUS_CODE_SUCCESS))
                .andExpect(jsonPath(TestConstants.RESPONSE_FIELD_PATH_MESSAGE).value(Constants.MESSAGE_SUCCESS))
                .andExpect(jsonPath(RESPONSE_FIELD_PATH_TOKEN).value(token))
                .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERNAME).value(TestConstants.TEST_AUTH_USERNAME))
                .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERROLES).isArray())
                .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERROLES, Matchers.hasSize(userRoles.size())))
                .andExpect(jsonPath(RESPONSE_FIELD_PATH_USERROLES, Matchers.hasItems(userRoles.toArray())));
    }
}