package com.sidibrahim.Aman.entity;

import com.sidibrahim.Aman.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private Role role;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    private Agency agency;

}
