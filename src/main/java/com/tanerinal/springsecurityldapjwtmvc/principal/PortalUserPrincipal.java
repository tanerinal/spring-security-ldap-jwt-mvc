package com.tanerinal.springsecurityldapjwtmvc.principal;

import com.tanerinal.springsecurityldapjwtmvc.model.PortalUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PortalUserPrincipal implements UserDetails {
	private static final long serialVersionUID = -8603695528030228319L;

	private final PortalUser portalUser;

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return this.portalUser.getGrantedAuthorities().stream()
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {
		return StringUtils.EMPTY;
	}

	@Override
	public String getUsername() {
		return Optional.ofNullable(this.portalUser.getUsername())
				.orElse(null);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
