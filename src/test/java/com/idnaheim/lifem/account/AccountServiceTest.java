package com.idnaheim.lifem.account;

import com.idnaheim.lifem.enums.EnumAccountCategory;
import com.idnaheim.lifem.enums.EnumAccountType;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountEntity sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountEntity();
        sampleAccount.setId(1L);
        sampleAccount.setName("Savings BDO");
        sampleAccount.setBalance(new BigDecimal("10000.00"));
        sampleAccount.setCategory(EnumAccountCategory.BANK);
        sampleAccount.setType(EnumAccountType.SAVINGS);
        sampleAccount.setRemarks("Primary savings");
    }

    // -------------------------------------------------------------------------
    // getAllAccounts
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccounts {

        @Test
        @DisplayName("returns all accounts from repository")
        void returnsAll() {
            when(accountRepository.findAll()).thenReturn(List.of(sampleAccount));

            List<AccountEntity> result = accountService.getAllAccounts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Savings BDO");
            verify(accountRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no accounts exist")
        void returnsEmpty() {
            when(accountRepository.findAll()).thenReturn(List.of());

            assertThat(accountService.getAllAccounts()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getAccountById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAccountById")
    class GetAccountById {

        @Test
        @DisplayName("returns present Optional when account exists")
        void found() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));

            Optional<AccountEntity> result = accountService.getAccountById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("returns empty Optional when account does not exist")
        void notFound() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(accountService.getAccountById(99L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // createAccount
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createAccount")
    class CreateAccount {

        @Test
        @DisplayName("saves and returns the account")
        void saves() {
            when(accountRepository.save(sampleAccount)).thenReturn(sampleAccount);

            AccountEntity result = accountService.createAccount(sampleAccount);

            assertThat(result).isEqualTo(sampleAccount);
            verify(accountRepository).save(sampleAccount);
        }
    }

    // -------------------------------------------------------------------------
    // updateAccount
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateAccount")
    class UpdateAccount {

        @Test
        @DisplayName("updates fields and returns updated account when found")
        void updatesWhenFound() {
            AccountEntity update = new AccountEntity();
            update.setName("Updated Name");
            update.setBalance(new BigDecimal("5000.00"));
            update.setCategory(EnumAccountCategory.CASH);
            update.setType(EnumAccountType.CHECKING);
            update.setRemarks("updated");

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<AccountEntity> result = accountService.updateAccount(1L, update);

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Updated Name");
            assertThat(result.get().getBalance()).isEqualByComparingTo("5000.00");
        }

        @Test
        @DisplayName("returns empty Optional when account not found")
        void returnsEmptyWhenMissing() {
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(accountService.updateAccount(99L, sampleAccount)).isEmpty();
            verify(accountRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteAccount
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccount {

        @Test
        @DisplayName("returns true and deletes when account exists")
        void deletesExisting() {
            when(accountRepository.existsById(1L)).thenReturn(true);

            boolean result = accountService.deleteAccount(1L);

            assertThat(result).isTrue();
            verify(accountRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when account does not exist")
        void returnsFalseWhenMissing() {
            when(accountRepository.existsById(99L)).thenReturn(false);

            boolean result = accountService.deleteAccount(99L);

            assertThat(result).isFalse();
            verify(accountRepository, never()).deleteById(anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // transfer
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("transfer")
    class Transfer {

        private AccountEntity fromAccount;
        private AccountEntity toAccount;

        @BeforeEach
        void setUp() {
            fromAccount = new AccountEntity();
            fromAccount.setId(1L);
            fromAccount.setName("From Account");
            fromAccount.setBalance(new BigDecimal("5000.00"));

            toAccount = new AccountEntity();
            toAccount.setId(2L);
            toAccount.setName("To Account");
            toAccount.setBalance(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("deducts from source and credits destination")
        void transfersBalances() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            accountService.transfer(1L, 2L, new BigDecimal("2000.00"));

            assertThat(fromAccount.getBalance()).isEqualByComparingTo("3000.00");
            assertThat(toAccount.getBalance()).isEqualByComparingTo("3000.00");
        }

        @Test
        @DisplayName("saves two transaction records (debit and credit)")
        void savesTwoTransactions() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            accountService.transfer(1L, 2L, new BigDecimal("500.00"));

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository, times(2)).save(txCaptor.capture());

            List<TransactionEntity> txs = txCaptor.getAllValues();
            TransactionEntity debit = txs.get(0);
            TransactionEntity credit = txs.get(1);

            assertThat(debit.getAmount()).isEqualByComparingTo("-500.00");
            assertThat(debit.getType()).isEqualTo(EnumTransactionType.TRANSFER);
            assertThat(credit.getAmount()).isEqualByComparingTo("500.00");
            assertThat(credit.getType()).isEqualTo(EnumTransactionType.TRANSFER);
        }

        @Test
        @DisplayName("throws when amount is zero")
        void throwsOnZeroAmount() {
            assertThatThrownBy(() -> accountService.transfer(1L, 2L, BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("throws when amount is negative")
        void throwsOnNegativeAmount() {
            assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("-100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("throws when source account not found")
        void throwsWhenSourceMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Source account not found");
        }

        @Test
        @DisplayName("throws when destination account not found")
        void throwsWhenDestinationMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("100")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Destination account not found");
        }

        @Test
        @DisplayName("throws when source has insufficient balance")
        void throwsOnInsufficientBalance() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(fromAccount));
            when(accountRepository.findById(2L)).thenReturn(Optional.of(toAccount));

            assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("9999.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Insufficient balance");
        }
    }
}
