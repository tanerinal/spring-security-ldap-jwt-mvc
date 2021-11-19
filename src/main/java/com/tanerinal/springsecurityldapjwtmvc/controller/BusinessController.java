package com.tanerinal.springsecurityldapjwtmvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business-zone")
public class BusinessController {

    @GetMapping
    public ResponseEntity<String> businessMethod() {
            return ResponseEntity.ok("Congrats! If you see this, you have BUSINESS role...");
    }
}
