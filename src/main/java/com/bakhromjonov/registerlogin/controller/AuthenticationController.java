package com.bakhromjonov.registerlogin.controller;

import com.bakhromjonov.registerlogin.dto.UserDTO;
import com.bakhromjonov.registerlogin.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/v1/authentication")
@RequiredArgsConstructor
public class AuthenticationController {

    private final RegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO) throws MessagingException {
        return ResponseEntity.ok(registrationService.register(userDTO));
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestParam String token) throws MessagingException {
        return ResponseEntity.ok(registrationService.confirm(token));
    }
}
