package com.idnaheim.lifem.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    boolean existsByExpenseIdAndCreatedDateBetween(long expenseId, LocalDateTime start, LocalDateTime end);

    long countByExpenseIdAndCreatedDateAfter(long expenseId, LocalDateTime after);

    List<TransactionEntity> findByExpenseIdAndCreatedDateBetween(long expenseId, LocalDateTime start, LocalDateTime end);

    List<TransactionEntity> findByIncomeIdAndCreatedDateBetween(long incomeId, LocalDateTime start, LocalDateTime end);

    long countByIncomeIdAndCreatedDateAfter(long incomeId, LocalDateTime after);

}
