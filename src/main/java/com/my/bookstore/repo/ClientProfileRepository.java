package com.my.bookstore.repo;

import com.my.bookstore.model.ClientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {

    Optional<ClientProfile> findByUserId(Long userId);

    Optional<ClientProfile> findByUserEmail(String email);

}