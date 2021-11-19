package com.tanerinal.springsecurityldapjwtmvc.service;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.model.PortalUser;
import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthResponse;
import com.tanerinal.springsecurityldapjwtmvc.principal.PortalUserPrincipal;
import com.tanerinal.springsecurityldapjwtmvc.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
import org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.naming.directory.SearchControls;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortalUserService implements UserDetailsService {
    private static final String LDAP_ATTRIBUTE_USERPASSWORD = "userpassword";

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.timeout:18000}")
    private long jwtTimeout;

    @Value("${ldap.url}")
    private String ldapUrl;
    @Value("${ldap.port}")
    private String ldapPort;
    @Value("${ldap.directory.root}")
    private String ldapRoot;
    @Value("${ldap.managerDN}")
    private String ldapManagerDn;
    @Value("${ldap.managerPassword}")
    private String ldapManagerPassword;
    @Value("${ldap.user.base}")
    private String ldapUserSearchBase;
    @Value("${ldap.user.filter}")
    private String ldapUserSearchFilter;
    @Value("${ldap.group.base}")
    private String groupBase;

    private BaseLdapPathContextSource contextSource;

    /**
     * Prepares LDAP context in order to use in authentication processes.
     */
    @PostConstruct
    private void prepareLdapContext() {
        String ldapFullUrl = new StringBuilder(this.ldapUrl)
                .append(":")
                .append(this.ldapPort)
                .append("/")
                .append(this.ldapRoot)
                .toString();
        DefaultSpringSecurityContextSource localContextSource = new DefaultSpringSecurityContextSource(ldapFullUrl);
        localContextSource.setUserDn(this.ldapManagerDn);
        localContextSource.setPassword(this.ldapManagerPassword);
        localContextSource.afterPropertiesSet();
        this.contextSource = localContextSource;
    }


    /**
     * Locates the user from remote LDAP and returns the information wrapped in a {@link PortalUserPrincipal} object.
     *
     * @param username: Username to be queried from LDAP. Mostly, case-insensitive.
     * @return {@link UserDetails}
     * @throws {@link UsernameNotFoundException} when there is no entry matches with <code>username</code>.
     * @throws {@link IncorrectResultSizeDataAccessException} when there is more than one entry matches with <code>username</code>.
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        try {
            log.info("Searching LDAP for user {}", username);
            SearchControls searchControls = new SearchControls();
            searchControls.setReturningAttributes(new String[]{Constants.LDAP_ATTRIBUTE_ISMEMBEROF, Constants.LDAP_ATTRIBUTE_UID});
            SpringSecurityLdapTemplate template = new SpringSecurityLdapTemplate(this.contextSource);
            template.setSearchControls(searchControls);

            DirContextOperations searchResult = template.searchForSingleEntry(this.ldapUserSearchBase, this.ldapUserSearchFilter, new String[]{username});

            List<String> grantedAuthorities = new ArrayList<>(this.getGrantedAuthorities(searchResult));
            log.info("User {} retrieved. User's roles are: {}", username, grantedAuthorities);

            return new PortalUserPrincipal(PortalUser.builder()
                    .username(username)
                    .grantedAuthorities(grantedAuthorities)
                    .build());
        } catch (IncorrectResultSizeDataAccessException ex) {
            log.error("Unexpected result size returned from LDAP for search for user {}", username);

            if (ex.getActualSize() == 0) {
                throw new UsernameNotFoundException("User " + username + " not found in LDAP.");
            } else {
                throw ex;
            }
        }
    }

    /**
     * Authenticates the user from remote LDAP by using <code>username</code> and <code>password</code> credentials provided.
     * Populates the {@link SecurityContextHolder} with an {@link Authentication} object.
     *
     * @param username
     * @param password
     *
     * @return An {@link AuthResponse} object populated with user detail information and a JWT token to be used in following calls.
     */
    public AuthResponse authenticateUser(String username, String password) {
        Assert.isTrue(StringUtils.isNotBlank(username), "Username should not left blank!");
        Assert.isTrue(StringUtils.isNotBlank(password), "Password should not left blank!");

        List<String> grantedAuthorities = this.doLdapSearch(username, password);
        log.info("Authentication of {} successfull! Users groups are: {}", username, grantedAuthorities);

        PortalUserPrincipal portalUserPrincipal = new PortalUserPrincipal(PortalUser.builder()
                .username(username)
                .grantedAuthorities(grantedAuthorities)
                .build());
        Authentication authentication = new UsernamePasswordAuthenticationToken(portalUserPrincipal, null, portalUserPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        List<String> userRoles = portalUserPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return AuthResponse.builder()
                .username(username)
                .message(Constants.MESSAGE_SUCCESS)
                .status(Constants.STATUS_CODE_SUCCESS)
                .userRoles(userRoles)
                .token(JwtUtils.createJWTToken(username, this.jwtSecret, this.jwtTimeout, userRoles))
                .build();
    }

    private List<String> doLdapSearch (String username, String password) {
        try {
            PortalUserPrincipal portalUserPrincipal = new PortalUserPrincipal(PortalUser.builder().username(username).build());
            Authentication authentication = new UsernamePasswordAuthenticationToken(portalUserPrincipal, password);
            PasswordComparisonAuthenticator passwordComparisonAuthenticator = new PasswordComparisonAuthenticator(this.contextSource);
            passwordComparisonAuthenticator.setPasswordEncoder(new LdapShaPasswordEncoder());
            passwordComparisonAuthenticator.setUserDnPatterns(new String[]{this.ldapUserSearchFilter + "," + ldapUserSearchBase});
            passwordComparisonAuthenticator.setUserAttributes(new String[]{Constants.LDAP_ATTRIBUTE_ISMEMBEROF, LDAP_ATTRIBUTE_USERPASSWORD});

            DirContextOperations authenticationResult = passwordComparisonAuthenticator.authenticate(authentication);

            return this.getGrantedAuthorities(authenticationResult);
        } catch (BadCredentialsException e) {
            log.error("LDAP authentication failed for {}. Wrong password!", username);
            throw e;
        } catch (UsernameNotFoundException e) {
            log.error("LDAP authentication failed for {}. No such user!", username);
            throw e;
        }
    }

    /**
     * Extracts the list of granted authorities from LDAP query result.
     *
     * @return A {@link List} of users roles defined in LDAP.
     */
    private List<String> getGrantedAuthorities(DirContextOperations ldapResult) {
        if (ArrayUtils.isEmpty(ldapResult.getStringAttributes(Constants.LDAP_ATTRIBUTE_ISMEMBEROF))) {
            log.info("No roles found for user: {}. Returning empty granted authorities list.", ldapResult.getStringAttribute(Constants.LDAP_ATTRIBUTE_UID));
            return  new ArrayList<>();
        }

        return Arrays.asList(ldapResult.getStringAttributes(Constants.LDAP_ATTRIBUTE_ISMEMBEROF)).stream()
                .filter(groupDn -> StringUtils.endsWith(groupDn, this.groupBase))
                .map(groupDn -> StringUtils.substringBetween(StringUtils.upperCase(groupDn), "CN=", ","))
                .collect(Collectors.toList());
    }
}