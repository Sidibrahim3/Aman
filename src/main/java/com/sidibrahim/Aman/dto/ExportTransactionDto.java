package com.sidibrahim.Aman.dto;

import com.sidibrahim.Aman.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ExportTransactionDto {
    private BigDecimal amount;
    private Long reference;
    private TransactionType type;
    private String customerPhoneNumber;
    private Double earn;
}
