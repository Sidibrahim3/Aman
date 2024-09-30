package com.sidibrahim.Aman.mapper;

import com.sidibrahim.Aman.dto.BudgetLogDto;
import com.sidibrahim.Aman.entity.BudgetLog;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class BudgetLogMapper {

    public BudgetLogDto toDto(BudgetLog budgetLog){
        return BudgetLogDto.builder()
                .id(budgetLog.getId())
                .oldValue(budgetLog.getOldValue())
                .newValue(budgetLog.getNewValue())
                .userId(budgetLog.getUserId())
                .userName(budgetLog.getUserName())
                .actionDate(budgetLog.getActionDateTime())
                .agencyId(budgetLog.getAgencyId())
                .build();
    }

    public List<BudgetLogDto> toDtos(List<BudgetLog> budgetLogs){
        return budgetLogs.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Page<BudgetLogDto> toDtos(Page<BudgetLog> budgetLogs){
        return budgetLogs.map(this::toDto);
    }
}
