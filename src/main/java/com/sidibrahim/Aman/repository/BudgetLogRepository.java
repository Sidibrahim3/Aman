package com.sidibrahim.Aman.repository;

import com.sidibrahim.Aman.entity.BudgetLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BudgetLogRepository extends JpaRepository<BudgetLog, Long> {
    @Query("SELECT b FROM BudgetLog b WHERE b.agencyId = :agencyId ORDER BY b.actionDateTime DESC")
    Page<BudgetLog> findByAgencyIdOrderByActionDateTimeDesc(Long agencyId, Pageable pageable);
}
