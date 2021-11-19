package com.tanerinal.springsecurityldapjwtmvc.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finance-zone")
public class FinanceController {

    @GetMapping
    public ResponseEntity<String> financeMethod() {
            return ResponseEntity.ok("Congrats! If you see this, you have FINANCE role...");
    }
}
