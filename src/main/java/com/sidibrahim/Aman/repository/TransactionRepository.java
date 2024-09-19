package com.sidibrahim.Aman.repository;

import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAgentId(Long id);

    List<Transaction> findByAgency_Id(Long id);

    @Query("SELECT t FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    List<Transaction> findTransactionsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.type = :type AND t.isDeleted = false")
    List<Transaction> findTransactionsByDateRangeAndType(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("type") TransactionType type);

    @Query("SELECT t FROM Transaction t WHERE t.customerPhoneNumber = :customerPhoneNumber AND t.isDeleted = false")
    List<Transaction> findTransactionsByCustomerPhoneNumber(@Param("customerPhoneNumber") String customerPhoneNumber);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    Long countTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    BigDecimal sumTransactionAmountsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.earn) FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    BigDecimal sumEarningsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type AND t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    Long countTransactionsByType(
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = false")
    List<Transaction> findAllNonDeletedTransactions();

    @Query("SELECT t FROM Transaction t WHERE t.createDate >= CURRENT_DATE AND t.isDeleted = false")
    List<Transaction> findTransactionsForToday();

    @Query("SELECT t FROM Transaction t WHERE YEAR(t.createDate) = YEAR(CURRENT_DATE) AND MONTH(t.createDate) = MONTH(CURRENT_DATE) AND t.isDeleted = false")
    List<Transaction> findTransactionsForCurrentMonth();

    @Query("SELECT t FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.isDeleted = false")
    List<Transaction> findTransactionsForExport(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Modifying
    @Query("UPDATE Transaction t SET t.isDeleted = true WHERE t.id = :transactionId")
    void softDeleteById(@Param("transactionId") Long transactionId);

    @Modifying
    @Query("UPDATE Transaction t SET t.isDeleted = true WHERE t.createDate BETWEEN :startDate AND :endDate")
    void softDeleteTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
        @Query("SELECT t FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.agent.id = :userId")
        List<Transaction> findTransactionsByDateRangeAndUserId(@Param("startDate") LocalDateTime startDate,
                                                               @Param("endDate") LocalDateTime endDate,
                                                               @Param("userId") Long userId);


}
