package com.bakhromjonov.registerlogin.repository;

import com.bakhromjonov.registerlogin.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
