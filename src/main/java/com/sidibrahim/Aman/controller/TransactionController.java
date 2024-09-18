package com.sidibrahim.Aman.controller;

import com.sidibrahim.Aman.dto.PaginationData;
import com.sidibrahim.Aman.dto.ResponseMessage;
import com.sidibrahim.Aman.dto.TransactionDto;
import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.entity.User;
import com.sidibrahim.Aman.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ResponseMessage> addTransaction(@RequestBody Transaction transaction, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully added transaction")
                .status(HttpStatus.OK.value())
                .data(transactionService.save(transaction, user))
                .build());
    }

    @GetMapping
    public ResponseEntity<ResponseMessage> getAllTransactions(@RequestParam(name = "page",defaultValue = "0")int page,
                                                              @RequestParam(name = "size",defaultValue = "10")int size) {
        Page<TransactionDto> dtoPage = transactionService.findAll(page,size);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved all transactions")
                .status(HttpStatus.OK.value())
                .data(dtoPage.getContent())
                .meta(new PaginationData(dtoPage))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseMessage> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved transaction")
                .status(HttpStatus.OK.value())
                .data(transactionService.findById(id))
                .build());
    }

    @GetMapping("/{id}/agent")
    public ResponseEntity<ResponseMessage> getTransactionsByAgent(@PathVariable Long id) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("Successfully retrieved transactions for agent")
                .status(HttpStatus.OK.value())
                .data(transactionService.findByAgentId(id))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseMessage> deleteTransaction(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.ok(ResponseMessage
                .builder()
                .message("transaction successfully deleted")
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("/agencies/{agencyId}")
    public ResponseEntity<ResponseMessage> retrieveTransactionsByAgencyId(@PathVariable Long agencyId) {
        return ResponseEntity.ok(ResponseMessage
                .builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully retrieved transactions for agency")
                        .data(transactionService.findByAgencyId(agencyId))
                .build());
    }
}
