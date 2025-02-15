package com.example.electrical_preorder_system_backend.repository;

import com.example.electrical_preorder_system_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByName(String name);

    Optional<User> findByGoogleAccountId(String googleAccountId);
}
