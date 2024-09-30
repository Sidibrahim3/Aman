package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.*;
import com.sidibrahim.Aman.entity.Agency;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.exception.GenericException;
import com.sidibrahim.Aman.mapper.AgencyMapper;
import com.sidibrahim.Aman.repository.AgencyRepository;
import com.sidibrahim.Aman.repository.UserRepository;
import com.sidibrahim.Aman.service.AgencyService;
import com.sidibrahim.Aman.service.BudgetLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/api/agencies")
@Slf4j
public class AgencyController {
    private final AgencyRepository agencyRepository;
    private final AgencyService agencyService;
    private final AgencyMapper agencyMapper;
    private final UserRepository userRepository;
    private final BudgetLogService budgetLogService;

    public AgencyController(AgencyRepository agencyRepository, AgencyService agencyService, AgencyMapper agencyMapper, UserRepository userRepository, BudgetLogService budgetLogService) {
        this.agencyRepository = agencyRepository;
        this.agencyService = agencyService;
        this.agencyMapper = agencyMapper;
        this.userRepository = userRepository;
        this.budgetLogService = budgetLogService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> getAll(@RequestParam(name = "page",defaultValue = "0") int page,
                                                  @RequestParam(name = "size",defaultValue = "10") int size) {
        Page<AgencyDto> agencyDtoPage =agencyService.getAll(page,size);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Agencies Retrieved successfully")
                .data(agencyDtoPage.getContent())
                        .meta(new PaginationData(agencyDtoPage))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Agency Retrieved successfully")
                .data(agencyService.getById(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> addAgency(@RequestBody AgencyDto agency) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Agency Added successfully")
                .status(HttpStatus.OK.value())
                .data(agencyService.save(agency))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> deleteAgency(@PathVariable Long id) {
        Optional<Agency> agency = agencyRepository.findById(id);

        if (agency.isEmpty()) {
            ResponseMessage responseMessage = ResponseMessage.builder()
                    .message("Agency Does Not Exist")
                    .status(HttpStatus.NOT_FOUND.value())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMessage);
        } else {
            agencyService.deleteById(id);
            ResponseMessage responseMessage = ResponseMessage.builder()
                    .message("Agency Deleted Successfully")
                    .status(HttpStatus.OK.value())
                    .build();
            return ResponseEntity.ok(responseMessage);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ResponseMessage> updateAgency(@PathVariable Long id, @RequestBody Agency updatedAgency) {
        Optional<Agency> optionalAgency = agencyRepository.findById(id);
        if (optionalAgency.isEmpty()) {
            throw new GenericException("Agency not found");
        }

        Agency agencyToUpdate = optionalAgency.get();
        agencyToUpdate.setName(updatedAgency.getName() != null ? updatedAgency.getName() : agencyToUpdate.getName());
        agencyToUpdate.setAddress(updatedAgency.getAddress() != null ? updatedAgency.getAddress() : agencyToUpdate.getAddress());

        Agency savedAgency = agencyRepository.save(agencyToUpdate);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                        .status(HttpStatus.OK.value())
                        .message("Agency Updated Successfully ")
                        .data(agencyMapper.toAgencyDto(savedAgency))
                .build());
    }

    @PostMapping("/change-email")
    public ResponseEntity<String> changeEmailOfReports(@RequestParam(name = "email") String email){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = userRepository.findUserByPhoneNumber(authentication.getName()).get();
            Agency agency = user.getAgency();
            agency.setEmail(email);
            agencyRepository.save(agency);
            return ResponseEntity.ok(agency.getName()+" Email updated successfully");
        }catch (Exception e){
            log.info("Error occurred while trying to update agency email");
            throw new GenericException("Error Occurred While trying to update agency with "+e.getMessage());
        }
    }

    @PostMapping("/update-budget")
    public ResponseEntity<ResponseMessage> updateBudget(@RequestParam(name = "budget", required = true) BigDecimal budget) {
        //Ex Post : /api/agencies/update-budget?budget=120000
        AgencyDto agencyDto = agencyService.updateBudget(budget);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Agency Budget updated successfully ")
                .data(agencyDto)
                .build());
    }

    @PostMapping("/reset-budget")
    public ResponseEntity<ResponseMessage> resetBudget() {
        //Ex :
        BigDecimal zero = BigDecimal.valueOf(0);
        AgencyDto agencyDto = agencyService.resetBudget();
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Budget reinitialised successfully")
                .data(agencyDto)
                .build());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ResponseMessage> getEarnings() {
        EarningDTO earningDTO = agencyService.getEarnings();
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Earnings for dashboard retrieved")
                .data(earningDTO)
                .build());
    }

    @GetMapping("/budgetLogs")
    public ResponseEntity<ResponseMessage> getBudgetLogs(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<BudgetLogDto> budgetLogDtos = budgetLogService.getBudgetLogs(user.getAgency().getId(), page,size);
        PaginationData paginationData = new PaginationData(budgetLogDtos);
        return ResponseEntity.ok(ResponseMessage.builder()
                .message("Retrieved BudgetLogs ")
                .data(budgetLogDtos.getContent())
                .meta(paginationData)
                .status(500)
                .build());
    }
}
