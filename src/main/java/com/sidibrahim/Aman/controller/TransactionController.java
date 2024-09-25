package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.PaginationData;
import com.sidibrahim.Aman.dto.ResponseMessage;
import com.sidibrahim.Aman.dto.TransactionDto;
import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.enums.TransactionType;
import com.sidibrahim.Aman.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Add Transaction
    @PostMapping
    public ResponseEntity<ResponseMessage> addTransaction(@RequestBody Transaction transaction, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully added transaction")
                .status(HttpStatus.OK.value())
                .data(transactionService.save(transaction, user))
                .build());
    }

    // Get All Transactions (Paginated)
    @GetMapping
    public ResponseEntity<ResponseMessage> getAllTransactions(@RequestParam(name = "page", defaultValue = "0") int page,
                                                              @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<TransactionDto> dtoPage = transactionService.findAll(page, size);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved all transactions")
                .status(HttpStatus.OK.value())
                .data(dtoPage.getContent())
                .meta(new PaginationData(dtoPage))
                .build());
    }

    @GetMapping("/deleted")
    public ResponseEntity<ResponseMessage> getAllDeletedTransactions(@RequestParam(name = "page", defaultValue = "0") int page,
                                                              @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<TransactionDto> dtoPage = transactionService.findAllDeletedTransactions(page, size);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved all transactions")
                .status(HttpStatus.OK.value())
                .data(dtoPage.getContent())
                .meta(new PaginationData(dtoPage))
                .build());
    }

    // Get Transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved transaction")
                .status(HttpStatus.OK.value())
                .data(transactionService.findById(id))
                .build());
    }

    // Get Transactions by Agent ID
    @GetMapping("/{id}/agent")
    public ResponseEntity<ResponseMessage> getTransactionsByAgent(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved transactions for agent")
                .status(HttpStatus.OK.value())
                .data(transactionService.findByAgentId(id))
                .build());
    }

    // Delete Transaction (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Transaction successfully deleted")
                .status(HttpStatus.OK.value())
                .build());
    }

    // Retrieve Transactions by Agency ID
    @GetMapping("/agencies/{agencyId}")
    public ResponseEntity<ResponseMessage> retrieveTransactionsByAgencyId(@PathVariable Long agencyId) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved transactions for agency")
                .data(transactionService.findByAgencyId(agencyId))
                .build());
    }

    // Get Transactions by Date Range
    @GetMapping("/by-date")
    public ResponseEntity<ResponseMessage> getTransactionsByDateRange(@RequestParam("startDate") LocalDateTime startDate,
                                                                      @RequestParam("endDate") LocalDateTime endDate) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved transactions for date range")
                .data(transactionService.findByDateRange(startDate, endDate))
                .build());
    }

    // Get Transactions by Date Range and Type
    @GetMapping("/by-date-and-type")
    public ResponseEntity<ResponseMessage> getTransactionsByDateRangeAndType(@RequestParam("startDate") LocalDateTime startDate,
                                                                             @RequestParam("endDate") LocalDateTime endDate,
                                                                             @RequestParam("type") TransactionType type) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved transactions for date range and type")
                .data(transactionService.findByDateRangeAndType(startDate, endDate, type))
                .build());
    }

    // Get Today's Transactions
    @GetMapping("/today")
    public ResponseEntity<ResponseMessage> getTodayTransactions() {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved today's transactions")
                .data(transactionService.getTodayTransactions())
                .build());
    }

    // Get This Month's Transactions
    @GetMapping("/this-month")
    public ResponseEntity<ResponseMessage> getThisMonthTransactions() {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved this month's transactions")
                .data(transactionService.getThisMonthTransactions())
                .build());
    }

    // Export Transactions by Date Range
    @GetMapping("/export")
    public ResponseEntity<ResponseMessage> exportTransactionsByDateRange(@RequestParam("startDate") LocalDateTime startDate,
                                                                         @RequestParam("endDate") LocalDateTime endDate) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully exported transactions for date range")
                .data(transactionService.exportTransactionsByDateRange(startDate, endDate))
                .build());
    }

    // Soft Delete Transactions by Date Range
    @DeleteMapping("/soft-delete")
    public ResponseEntity<ResponseMessage> softDeleteTransactionsByDateRange(@RequestParam("startDate") LocalDateTime startDate,
                                                                             @RequestParam("endDate") LocalDateTime endDate) {
        transactionService.softDeleteTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Transactions successfully soft deleted for date range")
                .build());
    }

    // Get Transactions by Customer Phone Number
    @GetMapping("/by-phone")
    public ResponseEntity<ResponseMessage> getTransactionsByPhoneNumber(@RequestParam("phoneNumber") String phoneNumber) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved transactions for customer phone number")
                .data(transactionService.findByCustomerPhoneNumber(phoneNumber))
                .build());
    }

    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        // Default to today's transactions if dates are not provided
        if (startDate == null) {
            startDate = LocalDate.now().atStartOfDay();
        }
        if (endDate == null) {
            endDate = LocalDate.now().atTime(LocalTime.MAX);
        }

        byte[] excelData = transactionService.exportToExcel(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "transactions.xlsx");

        return ResponseEntity.ok().headers(headers).body(excelData);
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportToPdf(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        // Default to today's transactions if dates are not provided
        if (startDate == null) {
            startDate = LocalDate.now().atStartOfDay();
        }
        if (endDate == null) {
            endDate = LocalDate.now().atTime(LocalTime.MAX);
        }

        byte[] pdfData = transactionService.exportToPdf(startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "transactions.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfData);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionDto transactionDto) {

        TransactionDto updatedTransaction = transactionService.updateTransaction(transactionId, transactionDto);

        return ResponseEntity.ok(updatedTransaction);
    }


    @GetMapping("/search")
    //Page<TransactionDto>
    public ResponseEntity<ResponseMessage> searchTransactions(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> transactionsPage = transactionService.searchTransactions(keyword, pageable);
        PaginationData paginationData = new PaginationData(transactionsPage);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                        .message("Retrieved search results")
                        .status(200)
                        .data(transactionsPage.getContent())
                        .meta(paginationData)
                .build());
        //;
    }
}
