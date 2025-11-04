package com.healthspace.backend.repository;
import com.healthspace.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA automatically creates this query for us
    Optional<User> findByEmail(String email);
}