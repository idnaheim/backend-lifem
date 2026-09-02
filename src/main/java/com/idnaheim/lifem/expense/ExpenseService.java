package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.ExpenseFrequency;
import com.idnaheim.lifem.enums.TransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public Map<ExpenseFrequency, BigDecimal> getRunRateExpenses() {
        List<ExpenseEntity> expenses = expenseRepository.findAll();
        Map<ExpenseFrequency, BigDecimal> result = new HashMap<>();
        
        BigDecimal monthly = BigDecimal.ZERO;
        BigDecimal quarterly = BigDecimal.ZERO;
        BigDecimal semiAnnual = BigDecimal.ZERO;
        BigDecimal annual = BigDecimal.ZERO;
        BigDecimal oneTime = BigDecimal.ZERO;
        
        for (ExpenseEntity expense : expenses) {
            if (!expense.isActive()) continue;
            
            BigDecimal amount = expense.getAmount();
            switch (expense.getFrequency()) {
                case MONTHLY:
                    monthly = monthly.add(amount);
                    break;
                case QUARTERLY:
                    quarterly = quarterly.add(amount);
                    break;
                case SEMI_ANNUAL:
                    semiAnnual = semiAnnual.add(amount);
                    break;
                case ANNUAL:
                    annual = annual.add(amount);
                    break;
                case ONE_TIME:
                    oneTime = oneTime.add(amount);
                    break;
            }
        }

        BigDecimal aggregatedQuarterly = quarterly.add(monthly.multiply(BigDecimal.valueOf(3)));
        BigDecimal aggregatedSemiAnnual = semiAnnual.add(aggregatedQuarterly.multiply(BigDecimal.valueOf(2)));
        BigDecimal aggregatedAnnual = annual.add(aggregatedSemiAnnual.multiply(BigDecimal.valueOf(2)).add(oneTime));
        
        result.put(ExpenseFrequency.MONTHLY, monthly);
        result.put(ExpenseFrequency.QUARTERLY, aggregatedQuarterly);
        result.put(ExpenseFrequency.SEMI_ANNUAL, aggregatedSemiAnnual);
        result.put(ExpenseFrequency.ANNUAL, aggregatedAnnual);
        
        return result;
    }

    public List<ExpenseResponse> getAllExpenses() {
        List<ExpenseEntity> expenses = expenseRepository.findAll();
        expenses.forEach(this::populateIsPaid);
        return expenses.stream().map(ExpenseResponse::fromEntity).toList();
    }

    public Optional<ExpenseResponse> getExpenseById(long id) {
        Optional<ExpenseEntity> expense = expenseRepository.findById(id);
        expense.ifPresent(this::populateIsPaid);
        return expense.map(ExpenseResponse::fromEntity);
    }

    private void populateIsPaid(ExpenseEntity expense) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate now = LocalDate.now();

        switch (expense.getFrequency()) {
            case QUARTERLY:
                int quarterStartMonth = ((now.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate startOfQuarter = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                LocalDate endOfQuarter = startOfQuarter.plusMonths(2)
                        .withDayOfMonth(startOfQuarter.plusMonths(2).lengthOfMonth());
                start = startOfQuarter.atStartOfDay();
                end = endOfQuarter.atTime(LocalTime.MAX);
                break;
            case ONE_TIME:
                start = LocalDateTime.of(2000, 1, 1, 0, 0);
                end = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
                break;
            default:
                // MONTHLY
                start = now.withDayOfMonth(1).atStartOfDay();
                end = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
        }

        boolean paidThisPeriod = transactionRepository.existsByExpenseIdAndCreatedDateBetween(
                expense.getId(), start, end);
        expense.setPaid(paidThisPeriod);

        // Populate transactions for the current frequency period
        expense.setTransactions(transactionRepository.findByExpenseIdAndCreatedDateBetween(
                expense.getId(), start, end));

        // Calculate missed payments from paymentStartDate
        if (expense.getPaymentStartDate() != null && expense.getFrequency() != ExpenseFrequency.ONE_TIME) {
            LocalDateTime paymentStart = LocalDateTime.ofInstant(
                    expense.getPaymentStartDate(), ZoneId.systemDefault());
            long expectedPayments = calculateExpectedPayments(paymentStart, now, expense.getFrequency());
            long actualPayments = transactionRepository.countByExpenseIdAndCreatedDateAfter(
                    expense.getId(), paymentStart);
            long missed = expectedPayments - actualPayments;
            expense.setMissedPayments(Math.max(0, missed));
        } else {
            expense.setMissedPayments(0);
        }
    }

    private long calculateExpectedPayments(LocalDateTime startDate, LocalDate now, ExpenseFrequency frequency) {
        LocalDate start = startDate.toLocalDate();
        if (start.isAfter(now)) {
            return 0;
        }

        switch (frequency) {
            case MONTHLY:
                return ChronoUnit.MONTHS.between(start, now) + 1;
            case QUARTERLY:
                return ChronoUnit.MONTHS.between(start, now) / 3 + 1;
            default:
                return 0;
        }
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseEntity expense) {
        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }

    @Transactional
    public Optional<ExpenseResponse> updateExpense(long id, ExpenseEntity updatedExpense) {
        return expenseRepository.findById(id).map(existing -> {
            existing.setName(updatedExpense.getName());
            existing.setFrequency(updatedExpense.getFrequency());
            existing.setAmount(updatedExpense.getAmount());
            existing.setDescription(updatedExpense.getDescription());
            existing.setPaymentStartDate(updatedExpense.getPaymentStartDate());
            existing.setCategory(updatedExpense.getCategory());
            existing.setFixedAmount(updatedExpense.isFixedAmount());
            existing.setActive(updatedExpense.isActive());
            return ExpenseResponse.fromEntity(expenseRepository.save(existing));
        });
    }

    public boolean deleteExpense(long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public TransactionEntity payExpense(long expenseId, long accountId, BigDecimal amount, String remarks) {
        ExpenseEntity expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        // Always deduct using absolute value so negative amounts don't increase balance
        BigDecimal deductionAmount = amount.abs();

        // Deduct from account balance
        account.setBalance(account.getBalance().subtract(deductionAmount));
        accountRepository.save(account);

        // Create transaction record with negative amount
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setExpense(expense);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setAmount(deductionAmount.negate());
        transaction.setRemarks(remarks);

        return transactionRepository.save(transaction);
    }

}
