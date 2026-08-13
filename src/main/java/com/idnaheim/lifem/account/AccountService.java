package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.TransactionCategory;
import com.idnaheim.lifem.enums.TransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Cacheable("accounts")
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Cacheable(value = "accountById", key = "#id")
    public Optional<AccountEntity> getAccountById(long id) {
        return accountRepository.findById(id);
    }

    @Caching(evict = {
        @CacheEvict(value = "accounts", allEntries = true),
        @CacheEvict(value = "accountById", allEntries = true)
    })
    public AccountEntity createAccount(AccountEntity account) {
        return accountRepository.save(account);
    }

    @Caching(evict = {
        @CacheEvict(value = "accounts", allEntries = true),
        @CacheEvict(value = "accountById", key = "#id")
    })
    public Optional<AccountEntity> updateAccount(long id, AccountEntity updatedAccount) {
        return accountRepository.findById(id).map(existing -> {
            existing.setName(updatedAccount.getName());
            existing.setBalance(updatedAccount.getBalance());
            existing.setCategory(updatedAccount.getCategory());
            existing.setType(updatedAccount.getType());
            existing.setRemarks(updatedAccount.getRemarks());
            return accountRepository.save(existing);
        });
    }

    @Caching(evict = {
        @CacheEvict(value = "accounts", allEntries = true),
        @CacheEvict(value = "accountById", key = "#id")
    })
    public boolean deleteAccount(long id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "accounts", allEntries = true),
        @CacheEvict(value = "accountById", allEntries = true)
    })
    public void transfer(long fromAccountId, long toAccountId, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        AccountEntity fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));

        AccountEntity toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + toAccountId));

        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(BigDecimal.valueOf(amount)));
        toAccount.setBalance(toAccount.getBalance().add(BigDecimal.valueOf(amount)));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record debit transaction (from account)
        TransactionEntity debit = new TransactionEntity();
        debit.setAccount(fromAccount);
        debit.setCategory(TransactionCategory.TRANSFER);
        debit.setType(TransactionType.TRANSFER);
        debit.setAmount(BigDecimal.valueOf(amount).negate());
        debit.setRemarks("Transfer to " + toAccount.getName());
        transactionRepository.save(debit);

        // Record credit transaction (to account)
        TransactionEntity credit = new TransactionEntity();
        credit.setAccount(toAccount);
        credit.setCategory(TransactionCategory.TRANSFER);
        credit.setType(TransactionType.TRANSFER);
        credit.setAmount(BigDecimal.valueOf(amount));
        credit.setRemarks("Transfer from " + fromAccount.getName());
        transactionRepository.save(credit);
    }

}
