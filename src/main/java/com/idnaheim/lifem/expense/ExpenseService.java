package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Cacheable(value = "expenseRunRate")
    public Map<EnumBaseFrequency, BigDecimal> getRunRateExpenses() {
        List<ExpenseEntity> expenses = expenseRepository.findAll();
        Map<EnumBaseFrequency, BigDecimal> result = new HashMap<>();

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
                case BIYEARLY:
                    semiAnnual = semiAnnual.add(amount);
                    break;
                case YEARLY:
                    annual = annual.add(amount);
                    break;
                case ONCE:
                    oneTime = oneTime.add(amount);
                    break;
            }
        }

        BigDecimal aggregatedQuarterly = quarterly.add(monthly.multiply(BigDecimal.valueOf(3)));
        BigDecimal aggregatedSemiAnnual = semiAnnual.add(aggregatedQuarterly.multiply(BigDecimal.valueOf(2)));
        BigDecimal aggregatedAnnual = annual.add(aggregatedSemiAnnual.multiply(BigDecimal.valueOf(2)).add(oneTime));

        result.put(EnumBaseFrequency.MONTHLY, monthly);
        result.put(EnumBaseFrequency.QUARTERLY, aggregatedQuarterly);
        result.put(EnumBaseFrequency.BIYEARLY, aggregatedSemiAnnual);
        result.put(EnumBaseFrequency.YEARLY, aggregatedAnnual);

        return result;
    }

    @Cacheable(value = "expenses")
    public List<ExpenseResponse> getAllExpenses() {
        List<ExpenseEntity> result = new ArrayList<>();
        for (ExpenseEntity expenseEntity : expenseRepository.findAll()) {
            List<TransactionEntity> transactionsList = transactionRepository.findByExpenseId(expenseEntity.getId());
            expenseEntity.setTransactions(transactionsList);
            result.add(expenseEntity);
        }
        return result.stream().map(ExpenseResponse::fromEntity).toList();
    }

    @Cacheable(value = "expenseById", key = "#id")
    public Optional<ExpenseResponse> getExpenseById(long id) {
        Optional<ExpenseEntity> expenseEntity = expenseRepository.findById(id);

        if (expenseEntity.isPresent()) {
            ExpenseEntity result = expenseEntity.get();
            List<TransactionEntity> transactionsList = transactionRepository.findByExpenseId(result.getId());
            result.setTransactions(transactionsList);
            return Optional.of(ExpenseResponse.fromEntity(result));
        }

        return Optional.empty();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "expenses", allEntries = true),
            @CacheEvict(value = "expenseRunRate", allEntries = true)
    })
    public ExpenseResponse createExpense(ExpenseEntity expense) {
        return ExpenseResponse.fromEntity(expenseRepository.save(expense));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "expenses", allEntries = true),
            @CacheEvict(value = "expenseById", key = "#id"),
            @CacheEvict(value = "expenseRunRate", allEntries = true)
    })
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

    @Caching(evict = {
            @CacheEvict(value = "expenses", allEntries = true),
            @CacheEvict(value = "expenseById", key = "#id"),
            @CacheEvict(value = "expenseRunRate", allEntries = true)
    })
    public boolean deleteExpense(long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "expenses", allEntries = true),
            @CacheEvict(value = "expenseById", key = "#expenseId"),
            @CacheEvict(value = "expenseRunRate", allEntries = true)
    })
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
        transaction.setCategory(expense.getCategory());
        transaction.setType(EnumTransactionType.EXPENSE);
        transaction.setAmount(deductionAmount.negate());
        transaction.setRemarks(remarks);
        transaction.setTransactionDateTime(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

}
