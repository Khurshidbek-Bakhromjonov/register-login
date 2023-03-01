package com.bakhromjonov.registerlogin.service;

import com.bakhromjonov.registerlogin.dto.UserDTO;
import com.bakhromjonov.registerlogin.models.Role;
import com.bakhromjonov.registerlogin.models.Token;
import com.bakhromjonov.registerlogin.models.User;
import com.bakhromjonov.registerlogin.repository.TokenRepository;
import com.bakhromjonov.registerlogin.repository.UserRepository;
import javax.mail.MessagingException;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final String CONFIRMATION_URL = "http://localhost:8080/api/v1/authentication/confirm?token=%s";

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    @Transactional
    public String register(UserDTO userDTO) throws MessagingException {
        boolean userExists = userRepository.findByEmail(userDTO.getEmail()).isPresent();
        if (userExists) throw new IllegalStateException("A user already exists with the same email");
        String encodedPassword = passwordEncoder.encode(userDTO.getPassword());

        User user = User.builder()
                .firstName(userDTO.getFirstname())
                .lastName(userDTO.getLastname())
                .email(userDTO.getEmail())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        String generatedToken = UUID.randomUUID().toString();
        Token token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(savedUser)
                .build();
        tokenRepository.save(token);

        emailService.send(
                userDTO.getEmail(),
                userDTO.getFirstname(),
                null,
                String.format(CONFIRMATION_URL, generatedToken)
        );

        return generatedToken;
    }

    public String confirm(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token).orElseThrow(() -> new IllegalStateException("Token not found"));
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            String generatedToken = UUID.randomUUID().toString();
            Token newToken = Token.builder()
                    .token(generatedToken)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(10))
                    .user(savedToken.getUser())
                    .build();
            tokenRepository.save(newToken);
            emailService.send(
                    savedToken.getUser().getEmail(),
                    savedToken.getUser().getFirstName(),
                    null,
                    String.format(CONFIRMATION_URL, generatedToken));
            return "Token expired, a new token has been sent to your email";
        }

        User user = userRepository.findById(savedToken.getUser().getId()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        savedToken.setValidatedAt(LocalDateTime.now());
        tokenRepository.save(savedToken);
        return "<h1>Your account hase been successfully activated</h1>";
    }
}