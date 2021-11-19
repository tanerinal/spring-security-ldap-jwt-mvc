package com.tanerinal.springsecurityldapjwtmvc.filter;

import com.tanerinal.springsecurityldapjwtmvc.Constants;
import com.tanerinal.springsecurityldapjwtmvc.principal.PortalUserPrincipal;
import com.tanerinal.springsecurityldapjwtmvc.service.PortalUserService;
import com.tanerinal.springsecurityldapjwtmvc.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	@Value("${jwt.secret}")
	private String jwtSecret;
	
	private final PortalUserService portalUserService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			Optional<String> token = JwtUtils.getTokenWithoutBearer(request);
			token.ifPresent(t -> verifyAndAuthenticatePortalUser(request, t));
		} catch (Exception e) {
			log.error(Constants.MESSAGE_AUTHENTICATION_FAILED);
		}
		
		filterChain.doFilter(request, response);
	}

	private void verifyAndAuthenticatePortalUser(HttpServletRequest request, String token) {
		if (JwtUtils.verifyToken(token, jwtSecret)) {
			String username = JwtUtils.extractUsername(token, jwtSecret);
			PortalUserPrincipal principal = (PortalUserPrincipal) portalUserService.loadUserByUsername(username);

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

			SecurityContextHolder.getContext().setAuthentication(auth);
		}
	}
}
