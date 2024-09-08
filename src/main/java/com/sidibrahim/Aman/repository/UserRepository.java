package com.sidibrahim.Aman.repository;

import com.sidibrahim.Aman.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByPhoneNumber(String phoneNumber);
    List<User> findByAgencyId(Long agencyId);
}
