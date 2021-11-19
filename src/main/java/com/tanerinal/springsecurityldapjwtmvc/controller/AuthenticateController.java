package com.tanerinal.springsecurityldapjwtmvc.controller;

import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthRequest;
import com.tanerinal.springsecurityldapjwtmvc.model.dto.AuthResponse;
import com.tanerinal.springsecurityldapjwtmvc.service.PortalUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
@Slf4j
public class AuthenticateController {
    private final PortalUserService portalUserService;

    @PostMapping
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @NonNull AuthRequest authRequest) {
            log.info("Authentication request for user {} received!", authRequest.getUsername());
            return ResponseEntity.ok(portalUserService.authenticateUser(authRequest.getUsername(), authRequest.getPassword()));
    }
}
