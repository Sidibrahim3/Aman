package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.EmailDetailsDto;
import com.sidibrahim.Aman.dto.UserDto;
import com.sidibrahim.Aman.service.SendMailService;
import com.sidibrahim.Aman.service.TransactionService;
import com.sidibrahim.Aman.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@Slf4j
public class ReportController {
    private final TransactionService transactionService;
    private final SendMailService sendMailService;
    private final UserService userService;

    public ReportController(TransactionService transactionService, SendMailService sendMailService, UserService userService) {
        this.transactionService = transactionService;
        this.sendMailService = sendMailService;
        this.userService = userService;
    }

    @PostMapping("/all-transactions")
    public ResponseEntity<String> sendReportOfAll(){
        try {
            LocalDate localDate = LocalDate.now();
            byte[] pdfData = transactionService.exportToPdf(LocalDateTime.of(2024,9,19,1,1,1), LocalDateTime.now());
            return getStringResponseEntity(localDate, pdfData);
        } catch (Exception e){
            log.error("Exception Occurred  :=> "+e.getMessage() );
            throw new RuntimeException(e.getMessage());
        }

    }

    @PostMapping("/daily-report")
    public ResponseEntity<String> sendDailyReport(){
        try {
            LocalDate localDate = LocalDate.now();
            byte[] pdfData = transactionService.exportToPdf(LocalDate.now().atTime(2, 0,0),LocalDateTime.now());
            return getStringResponseEntity(localDate, pdfData);
        } catch (Exception e){
            log.error("Exception Occurred :=> "+e.getMessage() );
            throw new RuntimeException(e.getMessage());
        }
    }

    private ResponseEntity<String> getStringResponseEntity(LocalDate localDate, byte[] pdfData) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDto userDto = userService.getUserByPhoneNumber(authentication.getName());
        String email = userDto.getEmail();
        EmailDetailsDto emailDetailsDto = new EmailDetailsDto();
        emailDetailsDto.setEmailBody("Hello from AmanSystem here is the report of "+localDate);
        emailDetailsDto.setRecipient(email);
        emailDetailsDto.setEmailSubject("Report of "+localDate);
        emailDetailsDto.setAttachment(pdfData);
        emailDetailsDto.setAttachmentFilename("r"+ localDate +".pdf");
        sendMailService.sendEmailWithAttachment(emailDetailsDto);
        return ResponseEntity.ok("Email sent successfully");
    }

    @PostMapping("/send-custom-report")
    public ResponseEntity<String> customRangeReport(@RequestParam(name = "startDate")LocalDateTime startDate,
                                                    @RequestParam(name = "endDate")LocalDateTime endDate){
        try {
            LocalDate localDate = LocalDate.now();
            byte[] pdfData = transactionService.exportToPdf(startDate,endDate);
            return getStringResponseEntity(localDate, pdfData);
        } catch (Exception e){
            log.error("Exception Occurred :  => "+e.getMessage() );
            throw new RuntimeException(e.getMessage());
        }
    }
}
