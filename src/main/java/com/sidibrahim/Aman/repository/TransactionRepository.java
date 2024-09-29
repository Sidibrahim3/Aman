package com.sidibrahim.Aman.repository;

import com.sidibrahim.Aman.entity.Transaction;
import com.sidibrahim.Aman.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT t FROM Transaction t WHERE t.createDate >= :startOfDay AND t.createDate <= CURRENT_TIMESTAMP AND (t.isDeleted = false OR t.isDeleted IS NULL) AND t.agent.id = :userId")
    List<Transaction> findTransactionsForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("userId") Long userId);

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

    @Query("SELECT t FROM Transaction t WHERE t.createDate BETWEEN :startDate AND :endDate AND t.agent.id = :userId AND (t.isDeleted = false OR t.isDeleted IS NULL) ORDER BY t.updateDate DESC")
    List<Transaction> findTransactionsByDateRangeAndUserId(@Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate,
                                                           @Param("userId") Long userId);

    /**
     * Retrieves a paginated list of active (non-deleted) transactions for a specific agency.
     *
     * @param agencyId The ID of the agency.
     * @param pageable Pagination information.
     * @return A page of non-deleted transactions for the specified agency.
     */
    @Query("SELECT t FROM Transaction t WHERE t.agency.id = :agencyId AND (t.isDeleted = false OR t.isDeleted IS NULL) ORDER BY t.updateDate DESC")
    Page<Transaction> findAllActiveTransactionsByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    /**
     * Retrieves a paginated list of deleted transactions for a specific agency.
     *
     * @param agencyId The ID of the agency.
     * @param pageable Pagination information.
     * @return A page of deleted transactions for the specified agency.
     */
    @Query("SELECT t FROM Transaction t WHERE t.agency.id = :agencyId AND t.isDeleted = true")
    Page<Transaction> findAllDeletedTransactionsByAgencyId(@Param("agencyId") Long agencyId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(t.customerPhoneNumber LIKE %:keyword% OR CAST(t.reference AS string) LIKE %:keyword%) " +
            "AND t.agency.id = :agencyId " +
            "ORDER BY t.updateDate DESC")
    Page<Transaction> searchTransactionsByKeyword(String keyword, Long agencyId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.agent.id = :agentId AND (t.isDeleted = false OR t.isDeleted IS NULL) ORDER BY t.updateDate DESC")
    Page<Transaction> findByAgentIdAndNotDeletedOrderByUpdateDateDesc(@Param("agentId") Long agentId, Pageable pageable);

}
