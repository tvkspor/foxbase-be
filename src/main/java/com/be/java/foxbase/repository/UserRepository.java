package com.be.java.foxbase.repository;

import com.be.java.foxbase.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    Optional<User> findByUsername(String username);
    User create(User user);
    User update(User user);
}
