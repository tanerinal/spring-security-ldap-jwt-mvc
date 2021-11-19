package com.tanerinal.springsecurityldapjwtmvc.controller;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class FinanceControllerTest {
    private static final String URL = "/finance-zone";

    private MockMvc mockMvc;

    @InjectMocks
    private FinanceController controller;


    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @SneakyThrows
    @Test
    public void testAuthenticateWhenProperRequestShouldReturnProperAuthentication() {
        mockMvc.perform(MockMvcRequestBuilders.get(URL))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string("Congrats! If you see this, you have FINANCE role..."));
    }
}