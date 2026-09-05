package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.expense.ExpenseEntity;
import com.idnaheim.lifem.expense.ExpenseRepository;
import com.idnaheim.lifem.income.IncomeEntity;
import com.idnaheim.lifem.income.IncomeRepository;
import lombok.AllArgsConstructor;
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

    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<TransactionEntity> getTransactionById(long id) {
        return transactionRepository.findById(id);
    }

    public TransactionEntity createTransaction(TransactionRequest request) {
        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDateTime(request.getTransactionDateTime());

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

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Optional<TransactionEntity> updateTransaction(long id, TransactionRequest request) {
        return transactionRepository.findById(id).map(existing -> {
            AccountEntity account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));
            existing.setAccount(account);
            existing.setCategory(request.getCategory());
            existing.setAmount(request.getAmount());
            existing.setRemarks(request.getRemarks());
            existing.setTransactionDateTime(request.getTransactionDateTime());

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
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                accountRepository.save(account);
            }

            transactionRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public TransactionEntity recordExpense(TransactionRequest request) {

        AccountEntity account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + request.getAccountId()));

        // Deduct from account balance
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        // Create transaction record
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setType(EnumTransactionType.EXPENSE);
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount().negate());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDateTime(request.getTransactionDateTime());

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

        return transactionRepository.save(transaction);
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

        // Create transaction record
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setType(EnumTransactionType.INCOME);
        transaction.setCategory(request.getCategory());
        transaction.setAmount(request.getAmount());
        transaction.setRemarks(request.getRemarks());
        transaction.setTransactionDateTime(request.getTransactionDateTime());

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

        return transactionRepository.save(transaction);
    }

}
