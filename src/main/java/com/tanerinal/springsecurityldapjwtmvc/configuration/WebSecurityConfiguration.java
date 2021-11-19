package com.tanerinal.springsecurityldapjwtmvc.configuration;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.entrypoint.AccessDeniedEntryPoint;
import com.tanerinal.springsecurityldapjwtmvc.entrypoint.UnauthenticatedEntryPoint;
import com.tanerinal.springsecurityldapjwtmvc.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final JwtRequestFilter jwtRequestFilter;
    private final UnauthenticatedEntryPoint unauthenticatedEntryPoint;
    private final AccessDeniedEntryPoint accessDeniedEntryPoint;

    @Value("${autz.permitted.paths.all}")
    private String[] permittedPaths;
    @Value("${autz.permitted.paths.finance}")
    private String[] financeRolePermittedPaths;
    @Value("${autz.permitted.paths.business}")
    private String[] businessRolePermittedPaths;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                    .disable()
                .authorizeRequests()
                .antMatchers(permittedPaths).permitAll()
                .antMatchers(financeRolePermittedPaths).hasAuthority(Constants.LDAP_ROLE_FINANCE)
                .antMatchers(businessRolePermittedPaths).hasAuthority(Constants.LDAP_ROLE_BUSINESS)
                    .anyRequest().authenticated()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint(this.unauthenticatedEntryPoint)
                    .accessDeniedHandler(this.accessDeniedEntryPoint)
                .and()
                    .logout()
                        .disable()
                    .formLogin()
                        .disable();

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
