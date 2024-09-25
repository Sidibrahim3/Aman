package com.sidibrahim.Aman.repository;

import com.sidibrahim.Aman.entity.User;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByPhoneNumber(String phoneNumber);
    List<User> findByAgencyId(Long agencyId);
    @Query("SELECT u FROM User u ORDER BY u.updateDate DESC")
    Page<User> findAllUsers(Pageable pageable);
}
