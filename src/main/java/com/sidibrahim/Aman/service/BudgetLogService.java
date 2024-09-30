package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.BudgetLogDto;
import com.sidibrahim.Aman.entity.BudgetLog;
import com.sidibrahim.Aman.mapper.BudgetLogMapper;
import com.sidibrahim.Aman.repository.BudgetLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BudgetLogService {
    private final BudgetLogRepository budgetLogRepository;
    private final BudgetLogMapper budgetLogMapper;

    public BudgetLogService(BudgetLogRepository budgetLogRepository, BudgetLogMapper budgetLogMapper) {
        this.budgetLogRepository = budgetLogRepository;
        this.budgetLogMapper = budgetLogMapper;
    }

    public void saveBudgetLog(BudgetLog budgetLog){
        budgetLogRepository.save(budgetLog);
    }

    public Page<BudgetLogDto> getBudgetLogs(Long agencyId, int page, int size) {
        return budgetLogMapper.toDtos(budgetLogRepository.findByAgencyIdOrderByActionDateTimeDesc(agencyId,PageRequest.of(page,size)));
    }
}
