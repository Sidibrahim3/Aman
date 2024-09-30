package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.AgencyDto;
import com.sidibrahim.Aman.dto.EarningDTO;
import com.sidibrahim.Aman.dto.TransactionDto;
import com.sidibrahim.Aman.entity.Agency;
import com.sidibrahim.Aman.entity.BudgetLog;
import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.enums.Role;
import com.sidibrahim.Aman.enums.TransactionType;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.mapper.AgencyMapper;
import com.sidibrahim.Aman.repository.AgencyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    private final TransactionService transactionService;
    private final BudgetLogService budgetLogService;

    @Transactional
    public AgencyDto save(AgencyDto agencyDto){
        Agency agency = agencyMapper.toAgency(agencyDto);
        agency.setCreateDate(LocalDate.now());
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    public Page<AgencyDto> getAll(int page, int size){
        return agencyMapper.toAgencyDtos(agencyRepository.findAll(PageRequest.of(page,size)));
    }

    public void deleteById(Long id){
        agencyRepository.deleteById(id);
    }

    public AgencyDto getById(Long id){
        return agencyMapper.toAgencyDto(agencyRepository.findById(id).orElseThrow(()->new GenericException("Agency Not Found With Id : " + id)));
    }

    @Transactional
    public AgencyDto updateBudget(BigDecimal budget){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = user.getAgency();
        BigDecimal oldBudget = agency.getBudget();
        BigDecimal newBudget = agency.getBudget().add(budget);
        agency.setBudget(newBudget);
        budgetLogService.saveBudgetLog(BudgetLog.builder()
                .actionDateTime(LocalDateTime.now())
                .oldValue(oldBudget)
                .newValue(newBudget)
                .userId(user.getId())
                .userName(user.getName())
                .agencyId(agency.getId())
                .build());
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    public AgencyDto resetBudget(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = user.getAgency();
        BigDecimal oldBudget = agency.getBudget();
        agency.setBudget(BigDecimal.valueOf(0));
        BigDecimal newBudget = BigDecimal.valueOf(0);
        budgetLogService.saveBudgetLog(BudgetLog.builder()
                .actionDateTime(LocalDateTime.now())
                .oldValue(oldBudget)
                .newValue(newBudget)
                .userId(user.getId())
                .userName(user.getName())
                .agencyId(agency.getId())
                .build());
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    @Transactional
    public void incrementBudget(BigDecimal budget) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(() -> new GenericException("Agency Not Found"));
        BigDecimal oldBudget = agency.getBudget();

        // Increment the budget
        BigDecimal newBudget = agency.getBudget().add(budget);
        agency.setBudget(newBudget);

        budgetLogService.saveBudgetLog(BudgetLog.builder()
                .actionDateTime(LocalDateTime.now())
                .oldValue(oldBudget)
                .newValue(newBudget)
                .userId(user.getId())
                .userName(user.getName())
                .agencyId(agency.getId())
                .build());
        log.error("Incremented Budget: " + newBudget);

        agencyRepository.save(agency);
    }

    @Transactional
    public void decrementBudget(BigDecimal budget) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(() -> new GenericException("Agency Not Found"));
        BigDecimal oldBudget = agency.getBudget();

        // Decrement the budget
        BigDecimal newBudget = agency.getBudget().subtract(budget);

        if (newBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new GenericException("Insufficient funds for withdrawal.");
        }

        agency.setBudget(newBudget);

        budgetLogService.saveBudgetLog(BudgetLog.builder()
                .actionDateTime(LocalDateTime.now())
                .oldValue(oldBudget)
                .newValue(newBudget)
                .userId(user.getId())
                .userName(user.getName())
                        .agencyId(agency.getId())
                .build());
        log.error("Decremented Budget: " + newBudget);

        agencyRepository.save(agency);
    }

    @Transactional
    public EarningDTO getEarnings() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        // Get the user's associated agency
        Agency agency = agencyRepository.findById(user.getAgency().getId()).orElseThrow(()->new GenericException("Agency Not Found Exception"));

        // Initialize variables for totals
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalDeposits = BigDecimal.ZERO;
        Double totalEarnings = 0.0;

        // Get today's transactions
        List<TransactionDto> transactionsDto = transactionService.getTodayTransactions();

        // Iterate through transactions and calculate totals
        for (TransactionDto transaction : transactionsDto) {
            if (transaction.getType() == TransactionType.WITHDRAWAL) {
                totalWithdrawals = totalWithdrawals.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.DEPOSIT) {
                totalDeposits = totalDeposits.add(transaction.getAmount());
            }
            totalEarnings += transaction.getEarn();
        }

        // Get the agency's budget
        String budget = agency.getBudget().toString();

        if(user.getRole()== Role.AGENCY_OWNER){
            BigDecimal totalAgencyWithdrawals = BigDecimal.ZERO;
            BigDecimal totalAgencyDeposits = BigDecimal.ZERO;
            Double totalAgencyEarnings = 0.0;

            List<TransactionDto> transactionsTotalDto = transactionService.getTodayTransactionsForAgency();

            // Iterate through transactions and calculate totals
            for (TransactionDto transaction : transactionsTotalDto) {
                if (transaction.getType() == TransactionType.WITHDRAWAL) {
                    totalAgencyWithdrawals = totalAgencyWithdrawals.add(transaction.getAmount());
                } else if (transaction.getType() == TransactionType.DEPOSIT) {
                    totalAgencyDeposits = totalAgencyDeposits.add(transaction.getAmount());
                }
                totalAgencyEarnings += transaction.getEarn();
            }

            return EarningDTO.builder()
                    .withdrawals(totalAgencyWithdrawals.toString()) // Convert BigDecimal to String
                    .deposits(totalAgencyDeposits.toString()) // Convert BigDecimal to String
                    .earnings(totalAgencyEarnings.toString()) // Convert Double to String
                    .budget(budget)
                    .role(user.getRole().toString())
                    .userName(user.getName())
                    .build();
        }
        else {
            // Build and return the EarningDTO
            return EarningDTO.builder()
                    .withdrawals(totalWithdrawals.toString()) // Convert BigDecimal to String
                    .deposits(totalDeposits.toString()) // Convert BigDecimal to String
                    .earnings(totalEarnings.toString()) // Convert Double to String
                    .budget(budget)
                    .role(user.getRole().toString())
                    .userName(user.getName())
                    .build();
        }

    }

}
