// UserRepository.java
package com.example.xshare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.xshare.backend.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    User findByUsername(String username);
}
