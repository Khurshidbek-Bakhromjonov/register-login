package com.bakhromjonov.registerlogin.repository;

import com.bakhromjonov.registerlogin.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);
}
