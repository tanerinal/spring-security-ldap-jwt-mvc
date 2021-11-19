package com.tanerinal.springsecurityldapjwtmvc.filter;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.TestConstants;
import com.tanerinal.springsecurityldapjwtmvc.model.PortalUser;
import com.tanerinal.springsecurityldapjwtmvc.principal.PortalUserPrincipal;
import com.tanerinal.springsecurityldapjwtmvc.service.PortalUserService;
import com.tanerinal.springsecurityldapjwtmvc.util.JwtUtils;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JwtRequestFilter.class, JwtUtils.class})
public class JwtRequestFilterTest {
    public static final String METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER = "verifyAndAuthenticatePortalUser";
    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    @Mock
    MockFilterChain mockFilterChain;

    @Mock
    PortalUserService portalUserService;

    private JwtRequestFilter spyJwtRequestFilter;


    @Before
    public void setUp() {
        mockStatic(JwtUtils.class);

        ReflectionTestUtils.setField(jwtRequestFilter, "jwtSecret", TestConstants.JWT_SECRET);
        SecurityContextHolder.getContext().setAuthentication(null);

        spyJwtRequestFilter = spy(jwtRequestFilter);
    }

    @SneakyThrows
    @Test
    public void testDoFilterInternalWhenSuccessAuthenticationShouldNotAppendAnyLog() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        String token = "token";

        PowerMockito.when(JwtUtils.getTokenWithoutBearer(mockHttpServletRequest)).thenAnswer((Answer<Optional>) invocation -> Optional.of(token));
        doNothing().when(spyJwtRequestFilter, METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);

        spyJwtRequestFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        PowerMockito.verifyPrivate(spyJwtRequestFilter).invoke(METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);
        Mockito.verify (mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
    }

    @SneakyThrows
    @Test
    public void testDoFilterInternalWhenNoTokenFoundShouldNotCallVerifyMethod() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        PowerMockito.when(JwtUtils.getTokenWithoutBearer(mockHttpServletRequest)).thenAnswer((Answer<Optional>) invocation -> Optional.empty());

        spyJwtRequestFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        PowerMockito.verifyPrivate(spyJwtRequestFilter, times(0)).invoke(METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, eq(mockHttpServletRequest), anyString());
        Mockito.verify (mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
    }

    @SneakyThrows
    @Test
    public void testDoFilterInternalWhenExceptionOccuredShouldAppendErrorLog() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        String token = "token";

        PowerMockito.when(JwtUtils.getTokenWithoutBearer(mockHttpServletRequest)).thenAnswer((Answer<Optional>) invocation -> Optional.of(token));
        doThrow(new UsernameNotFoundException(Constants.MESSAGE_AUTHENTICATION_FAILED)).when(spyJwtRequestFilter, METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);

        spyJwtRequestFilter.doFilterInternal(mockHttpServletRequest, mockHttpServletResponse, mockFilterChain);

        PowerMockito.verifyPrivate(spyJwtRequestFilter).invoke(METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);
        Mockito.verify (mockFilterChain).doFilter(mockHttpServletRequest, mockHttpServletResponse);
    }

    @SneakyThrows
    @Test
    public void testVerifyAndAuthenticatePortalUserWhenTokenNotVerifiedShouldHaveSecurityContextWithoutAuth() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        String token = "token";

        PowerMockito.when(JwtUtils.verifyToken(token, TestConstants.JWT_SECRET)).thenAnswer((Answer<Boolean>) invocation -> false);

        WhiteboxImpl.invokeMethod(spyJwtRequestFilter, METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);

        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @SneakyThrows
    @Test
    public void testVerifyAndAuthenticatePortalUserWhenTokenVerifiedShouldHaveSecurityContextWithAuth() {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        String token = "token";
        String username = "username";
        List<String> userRoles = Collections.singletonList("FINANCE");
        PortalUserPrincipal portalUserPrincipal = new PortalUserPrincipal(PortalUser.builder()
                .username(username)
                .grantedAuthorities(userRoles)
                .build());

        PowerMockito.when(JwtUtils.verifyToken(token, TestConstants.JWT_SECRET)).thenAnswer((Answer<Boolean>) invocation -> true);
        PowerMockito.when(JwtUtils.extractUsername(token, TestConstants.JWT_SECRET)).thenAnswer((Answer<String>) invocation -> username);
        when(portalUserService.loadUserByUsername(username)).thenReturn(portalUserPrincipal);

        WhiteboxImpl.invokeMethod(spyJwtRequestFilter, METHOD_VERIFY_AND_AUTHENTICATE_PORTAL_USER, mockHttpServletRequest, token);

        Assert.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Assert.assertEquals(portalUserPrincipal, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Assert.assertEquals(portalUserPrincipal.getAuthorities(), SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        Assert.assertNull(SecurityContextHolder.getContext().getAuthentication().getCredentials());
    }
}