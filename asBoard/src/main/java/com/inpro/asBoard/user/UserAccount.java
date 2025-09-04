package com.inpro.asBoard.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private Long userId;
    private String username;
    private String passwordHash; // BCrypt
    private String roles;        // "ROLE_USER,ROLE_ADMIN"
    private boolean active;
    private LocalDateTime regDate;
}