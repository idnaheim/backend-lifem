package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumAccountCategory;
import com.idnaheim.lifem.enums.EnumAccountType;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.expense.ExpenseEntity;
import com.idnaheim.lifem.expense.ExpenseRepository;
import com.idnaheim.lifem.income.IncomeEntity;
import com.idnaheim.lifem.income.IncomeRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private IncomeRepository incomeRepository;

    @InjectMocks
    private TransactionService transactionService;

    private AccountEntity sampleAccount;
    private ExpenseEntity sampleExpense;
    private IncomeEntity sampleIncome;
    private TransactionEntity sampleTransaction;
    private TransactionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountEntity();
        sampleAccount.setId(1L);
        sampleAccount.setName("BDO Savings");
        sampleAccount.setBalance(new BigDecimal("10000.00"));
        sampleAccount.setCategory(EnumAccountCategory.BANK);
        sampleAccount.setType(EnumAccountType.SAVINGS);

        sampleExpense = new ExpenseEntity();
        sampleExpense.setId(2L);
        sampleExpense.setName("Netflix");

        sampleIncome = new IncomeEntity();
        sampleIncome.setId(3L);
        sampleIncome.setName("Salary");

        sampleTransaction = new TransactionEntity();
        sampleTransaction.setId(10L);
        sampleTransaction.setAccount(sampleAccount);
        sampleTransaction.setAmount(new BigDecimal("500.00"));
        sampleTransaction.setType(EnumTransactionType.EXPENSE);
        sampleTransaction.setCategory(EnumBaseCategory.FOOD);
        sampleTransaction.setRemarks("Lunch");
        sampleTransaction.setTransactionDateTime(LocalDateTime.now());

        sampleRequest = new TransactionRequest();
        sampleRequest.setAccountId(1L);
        sampleRequest.setAmount(new BigDecimal("500.00"));
        sampleRequest.setCategory(EnumBaseCategory.FOOD);
        sampleRequest.setType(EnumTransactionType.EXPENSE);
        sampleRequest.setRemarks("Lunch");
        sampleRequest.setTransactionDateTime(LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // getAllTransactions
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllTransactions")
    class GetAllTransactions {

        @Test
        @DisplayName("returns all transactions from repository")
        void returnsAll() {
            when(transactionRepository.findAll()).thenReturn(List.of(sampleTransaction));

            List<TransactionEntity> result = transactionService.getAllTransactions();

            assertThat(result).hasSize(1);
            verify(transactionRepository).findAll();
        }

        @Test
        @DisplayName("returns empty list when no transactions exist")
        void returnsEmpty() {
            when(transactionRepository.findAll()).thenReturn(List.of());

            assertThat(transactionService.getAllTransactions()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getTransactionById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getTransactionById")
    class GetTransactionById {

        @Test
        @DisplayName("returns present Optional when transaction exists")
        void found() {
            when(transactionRepository.findById(10L)).thenReturn(Optional.of(sampleTransaction));

            Optional<TransactionEntity> result = transactionService.getTransactionById(10L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("returns empty Optional when transaction not found")
        void notFound() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(transactionService.getTransactionById(99L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // createTransaction
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createTransaction")
    class CreateTransaction {

        @Test
        @DisplayName("creates transaction linked only to account")
        void createsWithAccountOnly() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionEntity result = transactionService.createTransaction(sampleRequest);

            assertThat(result.getAccount()).isEqualTo(sampleAccount);
            assertThat(result.getAmount()).isEqualByComparingTo("500.00");
            assertThat(result.getExpense()).isNull();
            assertThat(result.getIncome()).isNull();
        }

        @Test
        @DisplayName("links expense when expenseId is provided")
        void linksExpense() {
            sampleRequest.setExpenseId(2L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(expenseRepository.findById(2L)).thenReturn(Optional.of(sampleExpense));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionEntity result = transactionService.createTransaction(sampleRequest);

            assertThat(result.getExpense()).isEqualTo(sampleExpense);
        }

        @Test
        @DisplayName("links income when incomeId is provided")
        void linksIncome() {
            sampleRequest.setIncomeId(3L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(incomeRepository.findById(3L)).thenReturn(Optional.of(sampleIncome));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionEntity result = transactionService.createTransaction(sampleRequest);

            assertThat(result.getIncome()).isEqualTo(sampleIncome);
        }

        @Test
        @DisplayName("throws when account not found")
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.createTransaction(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("throws when expenseId provided but expense not found")
        void throwsWhenExpenseMissing() {
            sampleRequest.setExpenseId(99L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.createTransaction(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expense not found");
        }

        @Test
        @DisplayName("throws when incomeId provided but income not found")
        void throwsWhenIncomeMissing() {
            sampleRequest.setIncomeId(99L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.createTransaction(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Income not found");
        }
    }

    // -------------------------------------------------------------------------
    // updateTransaction
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateTransaction")
    class UpdateTransaction {

        @Test
        @DisplayName("updates fields and returns updated transaction when found")
        void updatesWhenFound() {
            when(transactionRepository.findById(10L)).thenReturn(Optional.of(sampleTransaction));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionRequest updateRequest = new TransactionRequest();
            updateRequest.setAccountId(1L);
            updateRequest.setAmount(new BigDecimal("750.00"));
            updateRequest.setCategory(EnumBaseCategory.SHOPPING);
            updateRequest.setRemarks("Updated");
            updateRequest.setTransactionDateTime(LocalDateTime.now());

            Optional<TransactionEntity> result = transactionService.updateTransaction(10L, updateRequest);

            assertThat(result).isPresent();
            assertThat(result.get().getAmount()).isEqualByComparingTo("750.00");
            assertThat(result.get().getCategory()).isEqualTo(EnumBaseCategory.SHOPPING);
        }

        @Test
        @DisplayName("clears expense and income links when IDs are null")
        void clearsLinksWhenNull() {
            sampleTransaction.setExpense(sampleExpense);
            sampleTransaction.setIncome(sampleIncome);

            when(transactionRepository.findById(10L)).thenReturn(Optional.of(sampleTransaction));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionRequest updateRequest = new TransactionRequest();
            updateRequest.setAccountId(1L);
            updateRequest.setAmount(new BigDecimal("100.00"));
            updateRequest.setCategory(EnumBaseCategory.OTHER);
            updateRequest.setTransactionDateTime(LocalDateTime.now());
            // expenseId and incomeId are null by default

            Optional<TransactionEntity> result = transactionService.updateTransaction(10L, updateRequest);

            assertThat(result).isPresent();
            assertThat(result.get().getExpense()).isNull();
            assertThat(result.get().getIncome()).isNull();
        }

        @Test
        @DisplayName("returns empty Optional when transaction not found")
        void returnsEmptyWhenMissing() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(transactionService.updateTransaction(99L, sampleRequest)).isEmpty();
            verify(transactionRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteTransaction
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteTransaction")
    class DeleteTransaction {

        @Test
        @DisplayName("reverses balance effect on account and deletes transaction")
        void reversesBalanceAndDeletes() {
            // Account has 10000, transaction amount is +500 (expense stored as positive here)
            sampleTransaction.setAmount(new BigDecimal("500.00"));

            when(transactionRepository.findById(10L)).thenReturn(Optional.of(sampleTransaction));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = transactionService.deleteTransaction(10L);

            assertThat(result).isTrue();
            // balance = 10000 - 500 = 9500
            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("9500.00");
            verify(transactionRepository).deleteById(10L);
        }

        @Test
        @DisplayName("returns false when transaction not found")
        void returnsFalseWhenMissing() {
            when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(transactionService.deleteTransaction(99L)).isFalse();
            verify(transactionRepository, never()).deleteById(anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // recordExpense
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("recordExpense")
    class RecordExpense {

        @Test
        @DisplayName("deducts from account balance and stores negated amount")
        void recordsExpenseWithNegatedAmount() {
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionEntity result = transactionService.recordExpense(sampleRequest);

            // Balance deducted by 500
            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("9500.00");

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("-500.00");
            assertThat(txCaptor.getValue().getType()).isEqualTo(EnumTransactionType.EXPENSE);
        }

        @Test
        @DisplayName("links expense entity when expenseId is present")
        void linksExpense() {
            sampleRequest.setExpenseId(2L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(expenseRepository.findById(2L)).thenReturn(Optional.of(sampleExpense));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transactionService.recordExpense(sampleRequest);

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getExpense()).isEqualTo(sampleExpense);
        }

        @Test
        @DisplayName("throws when account not found")
        void throwsWhenAccountMissing() {
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.recordExpense(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    // -------------------------------------------------------------------------
    // recordIncome
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("recordIncome")
    class RecordIncome {

        @Test
        @DisplayName("credits account balance and stores positive amount")
        void recordsIncomeAndCreditsBalance() {
            sampleRequest.setAmount(new BigDecimal("5000.00"));
            sampleRequest.setType(EnumTransactionType.INCOME);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transactionService.recordIncome(sampleRequest);

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("15000.00");

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getAmount()).isEqualByComparingTo("5000.00");
            assertThat(txCaptor.getValue().getType()).isEqualTo(EnumTransactionType.INCOME);
        }

        @Test
        @DisplayName("throws when amount is zero")
        void throwsOnZeroAmount() {
            sampleRequest.setAmount(BigDecimal.ZERO);

            assertThatThrownBy(() -> transactionService.recordIncome(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than zero");
        }

        @Test
        @DisplayName("throws when amount is negative")
        void throwsOnNegativeAmount() {
            sampleRequest.setAmount(new BigDecimal("-100"));

            assertThatThrownBy(() -> transactionService.recordIncome(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be greater than zero");
        }

        @Test
        @DisplayName("throws when account not found")
        void throwsWhenAccountMissing() {
            sampleRequest.setAmount(new BigDecimal("1000"));
            when(accountRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.recordIncome(sampleRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Account not found");
        }

        @Test
        @DisplayName("links income entity when incomeId is present")
        void linksIncome() {
            sampleRequest.setAmount(new BigDecimal("5000.00"));
            sampleRequest.setIncomeId(3L);

            when(accountRepository.findById(1L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(incomeRepository.findById(3L)).thenReturn(Optional.of(sampleIncome));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            transactionService.recordIncome(sampleRequest);

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getIncome()).isEqualTo(sampleIncome);
        }
    }
}
