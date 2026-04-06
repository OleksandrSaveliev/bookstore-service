package com.my.bookstore.repo;

import com.my.bookstore.model.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, Long> {
    Optional<EmployeeProfile> findByUserId(Long userId);
    Optional<EmployeeProfile> findByUserEmail(String email);
}