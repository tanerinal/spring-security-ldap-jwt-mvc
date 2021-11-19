package com.tanerinal.springsecurityldapjwtmvc.principal;

import com.tanerinal.springsecurityldapjwtmvc.model.PortalUser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PortalUserPrincipalTest {
    @InjectMocks
    private PortalUserPrincipal portalUserPrincipal;

    private List<String> userRoles = Collections.singletonList("FINANCE");
    private String username = "username";

    private PortalUser portalUser = new PortalUser();

    @Before
    public void setUp() {
        portalUser = new PortalUser(username, userRoles);
        ReflectionTestUtils.setField(portalUserPrincipal, "portalUser", portalUser);
    }

    @Test
    public void testGetAuthorities() {
        Collection<GrantedAuthority> result = portalUserPrincipal.getAuthorities();
        Assert.assertEquals(userRoles.size(), result.size());
        result.forEach(grantedAuthority -> Assert.assertTrue(userRoles.contains(grantedAuthority.getAuthority())));
    }

    @Test
    public void testGetPassword() {
        Assert.assertEquals(StringUtils.EMPTY, portalUserPrincipal.getPassword());
    }

    @Test
    public void testGetUsernameWhenPortalUserHasUsernameShouldReturnThat() {
        Assert.assertEquals(username, portalUserPrincipal.getUsername());
    }

    @Test
    public void testGetUsernameWhenPortalUserHasNoUsernameShouldReturnNull() {
        portalUser.setUsername(null);
        ReflectionTestUtils.setField(portalUserPrincipal, "portalUser", portalUser);

        Assert.assertNull(username, portalUserPrincipal.getUsername());
    }

    @Test
    public void testIsAccountNonExpired() {
        Assert.assertTrue(portalUserPrincipal.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLocked() {
        Assert.assertTrue(portalUserPrincipal.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        Assert.assertTrue(portalUserPrincipal.isCredentialsNonExpired());
    }

    @Test
    public void testIsEnabled() {
        Assert.assertTrue(portalUserPrincipal.isEnabled());
    }
}