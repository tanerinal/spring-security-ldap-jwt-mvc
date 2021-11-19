package com.tanerinal.springsecurityldapjwtmvc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalUser implements Serializable {
	private static final long serialVersionUID = 8257090513210333742L;

	private String username;
	private List<String> grantedAuthorities;
}
