package com.sidibrahim.Aman.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EarningDTO {
    private String budget;
    private String withdrawals;
    private String deposits;
    private String earnings;
}
