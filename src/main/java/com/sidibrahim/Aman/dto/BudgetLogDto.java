package com.sidibrahim.Aman.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetLogDto {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal oldValue;
    private BigDecimal newValue;
    private LocalDateTime actionDate;
    private Long agencyId;
}