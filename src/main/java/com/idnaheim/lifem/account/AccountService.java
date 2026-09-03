package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.TransactionCategory;
import com.idnaheim.lifem.enums.TransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
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

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    public Optional<AccountEntity> getAccountById(long id) {
        return accountRepository.findById(id);
    }

    public AccountEntity createAccount(AccountEntity account) {
        return accountRepository.save(account);
    }

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

    public boolean deleteAccount(long id) {
        if (accountRepository.existsById(id)) {
            accountRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void transfer(long fromAccountId, long toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        AccountEntity fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));

        AccountEntity toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + toAccountId));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in source account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // Record debit transaction (from account)
        TransactionEntity debit = new TransactionEntity();
        debit.setAccount(fromAccount);
        debit.setCategory(TransactionCategory.TRANSFER);
        debit.setType(TransactionType.TRANSFER);
        debit.setAmount(amount.negate());
        debit.setRemarks("Transfer to " + toAccount.getName());
        transactionRepository.save(debit);

        // Record credit transaction (to account)
        TransactionEntity credit = new TransactionEntity();
        credit.setAccount(toAccount);
        credit.setCategory(TransactionCategory.TRANSFER);
        credit.setType(TransactionType.TRANSFER);
        credit.setAmount(amount);
        credit.setRemarks("Transfer from " + fromAccount.getName());
        transactionRepository.save(credit);
    }

}
