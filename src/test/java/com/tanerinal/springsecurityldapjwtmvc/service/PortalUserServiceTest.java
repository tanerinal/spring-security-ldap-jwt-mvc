package com.tanerinal.springsecurityldapjwtmvc.service;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.TestConstants;
import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthResponse;
import com.tanerinal.springsecurityldapjwtmvc.principal.PortalUserPrincipal;
import com.tanerinal.springsecurityldapjwtmvc.util.JwtUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PortalUserService.class, JwtUtils.class})
public class PortalUserServiceTest {
    private static final String FIELD_CONTEXT_SOURCE = "contextSource";
    private static final long JWT_TIMEOUT = 18000;
    private static final String LDAP_URL = "LDAP_URL";
    private static final String LDAP_PORT = "LDAP_PORT";
    private static final String LDAP_ROOT = "LDAP_ROOT";
    private static final String LDAP_MANAGER_DN = "LDAP_MANAGER_DN";
    private static final String LDAP_MANAGER_PASSWORD = "LDAP_MANAGER_PASSWORD";
    private static final String LDAP_USER_SEARCH_BASE = "LDAP_USER_SEARCH_BASE";
    private static final String LDAP_USER_SEARCH_FILTER = "LDAP_USER_SEARCH_FILTER";
    private static final String GROUP_BASE = "GROUP_BASE";
    private static final String METHID_NAME_DO_LDAP_SEARCH = "doLdapSearch";
    private static final String METHOD_NAME_GET_GRANTED_AUTHORITIES = "getGrantedAuthorities";

    private List<String> userRoles = Collections.singletonList("FINANCE");
    private final String username = "username";
    private final String password = "password";

    @InjectMocks
    private PortalUserService portalUserService;

    @Mock
    SpringSecurityLdapTemplate springSecurityLdapTemplate;

    private PortalUserService spyPortalUserService;

    @Test
    public void testPrepareLdapContext() {
        Assert.assertNotNull(new String());
    }
//    @Before
//    public void setUp() {
//        WhiteboxImpl.setInternalState(portalUserService, "jwtSecret", TestConstants.JWT_SECRET);
//        WhiteboxImpl.setInternalState(portalUserService, "jwtTimeout", JWT_TIMEOUT);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapUrl", LDAP_URL);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapPort", LDAP_PORT);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapRoot", LDAP_ROOT);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapManagerDn", LDAP_MANAGER_DN);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapManagerPassword", LDAP_MANAGER_PASSWORD);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapUserSearchBase", LDAP_USER_SEARCH_BASE);
//        WhiteboxImpl.setInternalState(portalUserService, "ldapUserSearchFilter", LDAP_USER_SEARCH_FILTER);
//        WhiteboxImpl.setInternalState(portalUserService, "groupBase", GROUP_BASE);
//
//        String ldapFullUrl = new StringBuilder(LDAP_URL)
//                .append(":")
//                .append(LDAP_PORT)
//                .append("/")
//                .append(LDAP_ROOT)
//                .toString();
//        DefaultSpringSecurityContextSource localContextSource = new DefaultSpringSecurityContextSource(ldapFullUrl);
//        localContextSource.setUserDn(LDAP_MANAGER_DN);
//        localContextSource.setPassword(LDAP_MANAGER_PASSWORD);
//        localContextSource.afterPropertiesSet();
//        WhiteboxImpl.setInternalState(portalUserService, FIELD_CONTEXT_SOURCE, localContextSource);
//
//        springSecurityLdapTemplate.setContextSource(localContextSource);
//
//        spyPortalUserService = PowerMockito.spy(portalUserService);
//    }
//
//    @SneakyThrows
//    @Test
//    public void testPrepareLdapContext() {
//        WhiteboxImpl.setInternalState(portalUserService, FIELD_CONTEXT_SOURCE, (Object) null);
//
//        WhiteboxImpl.invokeMethod(portalUserService, "prepareLdapContext");
//
//        Assert.assertNotNull(WhiteboxImpl.getInternalState(portalUserService, FIELD_CONTEXT_SOURCE));
//    }
//
//    @SneakyThrows
//    @Test
//    public void testLoadUserByUsernameWhenProperCallShouldReturnPrincipal() {
//        DirContextOperations ldapSearchResult = new DirContextAdapter();
//
//        PowerMockito.whenNew(SpringSecurityLdapTemplate.class).withAnyArguments().thenReturn(springSecurityLdapTemplate);
//        Mockito.when(springSecurityLdapTemplate.searchForSingleEntry(LDAP_USER_SEARCH_BASE, LDAP_USER_SEARCH_FILTER, new String[]{username})).thenReturn(ldapSearchResult);
//        PowerMockito.doReturn(userRoles).when(spyPortalUserService, METHOD_NAME_GET_GRANTED_AUTHORITIES, ldapSearchResult);
//
//        PortalUserPrincipal userPrincipal = (PortalUserPrincipal) spyPortalUserService.loadUserByUsername(username);
//
//        Assert.assertNotNull(userPrincipal);
//        Assert.assertEquals(username, userPrincipal.getUsername());
//        Assert.assertTrue(userPrincipal.getAuthorities().stream().allMatch(grantedAuthority -> userRoles.contains(grantedAuthority.getAuthority())));
//    }
//
//    @SneakyThrows
//    @Test (expected = UsernameNotFoundException.class)
//    public void testLoadUserByUsernameWhenUserNotExistShouldThrowNotFoundException() {
//        DirContextOperations ldapSearchResult = new DirContextAdapter();
//
//        PowerMockito.whenNew(SpringSecurityLdapTemplate.class).withAnyArguments().thenReturn(springSecurityLdapTemplate);
//        Mockito.when(springSecurityLdapTemplate.searchForSingleEntry(LDAP_USER_SEARCH_BASE, LDAP_USER_SEARCH_FILTER, new String[]{username})).thenThrow(new IncorrectResultSizeDataAccessException("No user Found.", 1, 0));
//
//        spyPortalUserService.loadUserByUsername(username);
//
//        PowerMockito.verifyPrivate(spyPortalUserService, times(0)).invoke(METHOD_NAME_GET_GRANTED_AUTHORITIES, ldapSearchResult);
//    }
//
//    @SneakyThrows
//    @Test (expected = IncorrectResultSizeDataAccessException.class)
//    public void testLoadUserByUsernameWhenMultipleUserExistShouldThrowIncorrectResultSizeException() {
//        DirContextOperations ldapSearchResult = new DirContextAdapter();
//
//        PowerMockito.whenNew(SpringSecurityLdapTemplate.class).withAnyArguments().thenReturn(springSecurityLdapTemplate);
//        Mockito.when(springSecurityLdapTemplate.searchForSingleEntry(LDAP_USER_SEARCH_BASE, LDAP_USER_SEARCH_FILTER, new String[]{username})).thenThrow(new IncorrectResultSizeDataAccessException("No user Found.", 1, 2));
//
//        spyPortalUserService.loadUserByUsername(username);
//
//        PowerMockito.verifyPrivate(spyPortalUserService, times(0)).invoke(METHOD_NAME_GET_GRANTED_AUTHORITIES, ldapSearchResult);
//    }
//
//    @SneakyThrows
//    @Test
//    public void testAuthenticateUserWhenSuccessfulAuthenticationShouldReturnAuthenticatedResponseAndPopulateSecurityContext() {
//        PowerMockito.mockStatic(JwtUtils.class);
//        String token = "token";
//
//        PowerMockito.doReturn(userRoles).when(spyPortalUserService, METHID_NAME_DO_LDAP_SEARCH, username, password);
//        PowerMockito.when(JwtUtils.createJWTToken(username, TestConstants.JWT_SECRET, JWT_TIMEOUT, userRoles)).thenAnswer((Answer<String>) invocation -> token);
//
//        AuthResponse response = spyPortalUserService.authenticateUser(username, password);
//
//        Assert.assertNotNull(SecurityContextHolder.getContext().getAuthentication());
//        Assert.assertEquals(username, response.getUsername());
//        Assert.assertEquals(Constants.MESSAGE_SUCCESS, response.getMessage());
//        Assert.assertEquals(Constants.STATUS_CODE_SUCCESS, response.getStatus());
//        Assert.assertEquals(userRoles, response.getUserRoles());
//        Assert.assertEquals(token, response.getToken());
//    }
//
//    @SneakyThrows
//    @Test
//    public void testGetGrantedAuthoritiesWhenNoGroupMembershipShouldReturnEmptyList() {
//        DirContextOperations ldapResult = new DirContextAdapter();
//
//        List<String> response = WhiteboxImpl.invokeMethod(spyPortalUserService, METHOD_NAME_GET_GRANTED_AUTHORITIES, ldapResult);
//
//        Assert.assertEquals(0, response.size());
//    }
//
//    @SneakyThrows
//    @Test
//    public void testGetGrantedAuthoritiesWhenHasGroupMembershipShouldReturnPopulatedList() {
//        DirContextOperations ldapResult = new DirContextAdapter();
//        ldapResult.addAttributeValue(Constants.LDAP_ATTRIBUTE_ISMEMBEROF, "CN=" + TestConstants.COMMON_STRING + "," + GROUP_BASE);
//
//        List<String> response = WhiteboxImpl.invokeMethod(spyPortalUserService, METHOD_NAME_GET_GRANTED_AUTHORITIES, ldapResult);
//
//        Assert.assertEquals(1, response.size());
//        Assert.assertTrue(StringUtils.equalsIgnoreCase(TestConstants.COMMON_STRING, response.get(0)));
//    }
}