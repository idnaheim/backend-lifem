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

    public Map<String, BigDecimal> getRunRateExpenses() {
        List<ExpenseEntity> expenses = expenseRepository.findAll();
        Map<String, BigDecimal> result = new HashMap<>();


        BigDecimal monthlyExpenses = expenses.stream()
                .filter(expense -> expense.getFrequency() == ExpenseFrequency.MONTHLY)
                .map(ExpenseEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal annualExpenses = monthlyExpenses.multiply(BigDecimal.valueOf(12));

        result.put("monthly", monthlyExpenses);
        result.put("annually", annualExpenses);

        return result;
    }

    public List<ExpenseEntity> getAllExpenses() {
        List<ExpenseEntity> expenses = expenseRepository.findAll();
        expenses.forEach(this::populateIsPaid);
        return expenses;
    }

    public Optional<ExpenseEntity> getExpenseById(long id) {
        Optional<ExpenseEntity> expense = expenseRepository.findById(id);
        expense.ifPresent(this::populateIsPaid);
        return expense;
    }

    private void populateIsPaid(ExpenseEntity expense) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate now = LocalDate.now();

        switch (expense.getFrequency()) {
            case WEEKLY:
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                start = startOfWeek.atStartOfDay();
                end = startOfWeek.plusDays(6).atTime(LocalTime.MAX);
                break;
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
            case WEEKLY:
                return ChronoUnit.WEEKS.between(start, now) + 1;
            case MONTHLY:
                return ChronoUnit.MONTHS.between(start, now) + 1;
            case QUARTERLY:
                return ChronoUnit.MONTHS.between(start, now) / 3 + 1;
            default:
                return 0;
        }
    }

    @Transactional
    public ExpenseEntity createExpense(ExpenseEntity expense) {
        return expenseRepository.save(expense);
    }

    @Transactional
    public Optional<ExpenseEntity> updateExpense(long id, ExpenseEntity updatedExpense) {
        return expenseRepository.findById(id).map(existing -> {
            existing.setName(updatedExpense.getName());
            existing.setFrequency(updatedExpense.getFrequency());
            existing.setAmount(updatedExpense.getAmount());
            existing.setDescription(updatedExpense.getDescription());
            existing.setPaymentStartDate(updatedExpense.getPaymentStartDate());
            existing.setCategory(updatedExpense.getCategory());
            existing.setFixedAmount(updatedExpense.isFixedAmount());
            return expenseRepository.save(existing);
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
        double newBalance = account.getBalance() - deductionAmount.doubleValue();
        account.setBalance(newBalance);
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
