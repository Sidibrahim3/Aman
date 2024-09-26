package com.sidibrahim.Aman.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data @AllArgsConstructor
public class ExportDataDto {
    private List<ExportTransactionDto> transactions;
    private BigDecimal totalEarning;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal netCash;
}
