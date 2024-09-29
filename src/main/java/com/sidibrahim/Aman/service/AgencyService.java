package com.sidibrahim.Aman.service;

import com.sidibrahim.Aman.dto.AgencyDto;
import com.sidibrahim.Aman.dto.EarningDTO;
import com.sidibrahim.Aman.dto.TransactionDto;
import com.sidibrahim.Aman.entity.Agency;
import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.entity.User;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyService {

    private final AgencyRepository agencyRepository;
    private final AgencyMapper agencyMapper;
    private final TransactionService transactionService;

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
        BigDecimal newBudget = agency.getBudget().add(budget);
        agency.setBudget(newBudget);
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    public AgencyDto resetBudget(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = user.getAgency();
        agency.setBudget(BigDecimal.valueOf(0));
        return agencyMapper.toAgencyDto(agencyRepository.save(agency));
    }

    @Transactional
    public void incrementBudget(BigDecimal budget) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(() -> new GenericException("Agency Not Found"));

        // Increment the budget
        BigDecimal newBudget = agency.getBudget().add(budget);
        agency.setBudget(newBudget);

        // Log the new budget
        System.out.println("Incremented Budget: " + newBudget);
        log.error("Incremented Budget: " + newBudget);

        agencyRepository.save(agency);
    }

    @Transactional
    public void decrementBudget(BigDecimal budget) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        Agency agency = agencyRepository.findById(user.getAgency().getId())
                .orElseThrow(() -> new GenericException("Agency Not Found"));

        // Decrement the budget
        BigDecimal newBudget = agency.getBudget().subtract(budget);

        if (newBudget.compareTo(BigDecimal.ZERO) < 0) {
            throw new GenericException("Insufficient funds for withdrawal.");
        }

        agency.setBudget(newBudget);

        // Log the new budget
        System.out.println("Decremented Budget: " + newBudget);
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

        // Build and return the EarningDTO
        return EarningDTO.builder()
                .withdrawals(totalWithdrawals.toString()) // Convert BigDecimal to String
                .deposits(totalDeposits.toString()) // Convert BigDecimal to String
                .earnings(totalEarnings.toString()) // Convert Double to String
                .budget(budget)
                .build();
    }

}
