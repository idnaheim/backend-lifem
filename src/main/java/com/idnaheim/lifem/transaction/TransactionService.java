package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.TransactionType;
import com.idnaheim.lifem.expense.ExpenseEntity;
import com.idnaheim.lifem.expense.ExpenseRepository;
import com.idnaheim.lifem.income.IncomeEntity;
import com.idnaheim.lifem.income.IncomeRepository;
import com.idnaheim.lifem.messaging.TransactionEvent;
import com.idnaheim.lifem.messaging.TransactionEventProducer;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CacheManager cacheManager;
    private final TransactionEventProducer transactionEventProducer;

    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<TransactionEntity> getTransactionById(long id) {
        return transactionRepository.findById(id);
    }

    public TransactionEntity createTransaction(TransactionEntity transaction) {
        return transactionRepository.save(transaction);
    }

    public Optional<TransactionEntity> updateTransaction(long id, TransactionRequest request) {
        return transactionRepository.findById(id).map(existing -> {
            AccountEntity account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));
            existing.setAccount(account);
            existing.setCategory(request.getCategory());
            existing.setAmount(request.getAmount());
            existing.setRemarks(request.getRemarks());

            if (request.getExpenseId() != null) {
                ExpenseEntity expense = expenseRepository.findById(request.getExpenseId())
                        .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + request.getExpenseId()));
                existing.setExpense(expense);
            } else {
                existing.setExpense(null);
            }

            if (request.getIncomeId() != null) {
                IncomeEntity income = incomeRepository.findById(request.getIncomeId())
                        .orElseThrow(() -> new IllegalArgumentException("Income not found: " + request.getIncomeId()));
                existing.setIncome(income);
            } else {
                existing.setIncome(null);
            }

            return transactionRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteTransaction(long id) {
        Optional<TransactionEntity> optionalTransaction = transactionRepository.findById(id);
        if (optionalTransaction.isPresent()) {
            TransactionEntity transaction = optionalTransaction.get();

            // Reverse the balance effect on the account
            if (transaction.getAccount() != null && transaction.getAmount() != null) {
                AccountEntity account = transaction.getAccount();
                // Subtract the transaction amount to reverse it
                // (expense amounts are negative, so subtracting a negative adds back;
                //  income amounts are positive, so subtracting a positive deducts it)
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(account);

                // Evict account caches since balance changed
                cacheManager.getCache("accounts").clear();
                cacheManager.getCache("accountById").evict(account.getId());
            }

            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public TransactionEntity recordExpense(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));

        // Deduct from account balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Evict account caches since balance changed
        cacheManager.getCache("accounts").clear();
        cacheManager.getCache("accountById").evict(request.getAccountId());

        // Create transaction record
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount().negate());
        transaction.setRemarks(request.getRemarks());

        if (request.getExpenseId() != null) {
            ExpenseEntity expense = expenseRepository.findById(request.getExpenseId())
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + request.getExpenseId()));
            transaction.setExpense(expense);
        }

        if (request.getIncomeId() != null) {
            IncomeEntity income = incomeRepository.findById(request.getIncomeId())
                    .orElseThrow(() -> new IllegalArgumentException("Income not found: " + request.getIncomeId()));
            transaction.setIncome(income);
        }

        TransactionEntity saved = transactionRepository.save(transaction);
        transactionEventProducer.publish(TransactionEvent.fromEntity(saved));
        return saved;
    }

    @Transactional
    public TransactionEntity recordIncome(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));

        // Add to account balance
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        // Evict account caches since balance changed
        cacheManager.getCache("accounts").clear();
        cacheManager.getCache("accountById").evict(request.getAccountId());

        // Create transaction record
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setRemarks(request.getRemarks());

        if (request.getIncomeId() != null) {
            IncomeEntity income = incomeRepository.findById(request.getIncomeId())
                    .orElseThrow(() -> new IllegalArgumentException("Income not found: " + request.getIncomeId()));
            transaction.setIncome(income);
        }

        if (request.getExpenseId() != null) {
            ExpenseEntity expense = expenseRepository.findById(request.getExpenseId())
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + request.getExpenseId()));
            transaction.setExpense(expense);
        }

        TransactionEntity saved = transactionRepository.save(transaction);
        transactionEventProducer.publish(TransactionEvent.fromEntity(saved));
        return saved;
    }

}
