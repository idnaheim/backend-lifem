package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumAccountCategory;
import com.idnaheim.lifem.enums.EnumAccountType;
import com.idnaheim.lifem.enums.EnumBaseCategory;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseEntity sampleExpense;
    private AccountEntity sampleAccount;

    @BeforeEach
    void setUp() {
        sampleExpense = new ExpenseEntity();
        sampleExpense.setId(1L);
        sampleExpense.setName("Netflix");
        sampleExpense.setAmount(new BigDecimal("599.00"));
        sampleExpense.setFrequency(EnumBaseFrequency.MONTHLY);
        sampleExpense.setCategory(EnumBaseCategory.LEISURE);
        sampleExpense.setActive(true);
        sampleExpense.setFixedAmount(true);

        sampleAccount = new AccountEntity();
        sampleAccount.setId(10L);
        sampleAccount.setName("GCash");
        sampleAccount.setBalance(new BigDecimal("3000.00"));
        sampleAccount.setCategory(EnumAccountCategory.E_WALLET);
        sampleAccount.setType(EnumAccountType.SAVINGS);
    }

    // -------------------------------------------------------------------------
    // getAllExpenses
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllExpenses")
    class GetAllExpenses {

        @Test
        @DisplayName("returns all expenses mapped to response DTOs")
        void returnsAll() {
            when(expenseRepository.findAll()).thenReturn(List.of(sampleExpense));
            when(transactionRepository.findByExpenseId(1L)).thenReturn(Collections.emptyList());

            List<ExpenseResponse> result = expenseService.getAllExpenses();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Netflix");
        }

        @Test
        @DisplayName("returns empty list when no expenses exist")
        void returnsEmpty() {
            when(expenseRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(expenseService.getAllExpenses()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getExpenseById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getExpenseById")
    class GetExpenseById {

        @Test
        @DisplayName("returns mapped response when expense exists")
        void found() {
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
            when(transactionRepository.findByExpenseId(1L)).thenReturn(Collections.emptyList());

            Optional<ExpenseResponse> result = expenseService.getExpenseById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(1L);
            assertThat(result.get().name()).isEqualTo("Netflix");
        }

        @Test
        @DisplayName("returns empty Optional when expense not found")
        void notFound() {
            when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(expenseService.getExpenseById(99L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // createExpense
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createExpense")
    class CreateExpense {

        @Test
        @DisplayName("saves and returns expense response")
        void saves() {
            when(expenseRepository.save(sampleExpense)).thenReturn(sampleExpense);

            ExpenseResponse result = expenseService.createExpense(sampleExpense);

            assertThat(result.name()).isEqualTo("Netflix");
            assertThat(result.amount()).isEqualByComparingTo("599.00");
            verify(expenseRepository).save(sampleExpense);
        }
    }

    // -------------------------------------------------------------------------
    // updateExpense
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateExpense")
    class UpdateExpense {

        @Test
        @DisplayName("updates fields and returns updated response when found")
        void updatesWhenFound() {
            ExpenseEntity updated = new ExpenseEntity();
            updated.setName("Spotify");
            updated.setFrequency(EnumBaseFrequency.MONTHLY);
            updated.setAmount(new BigDecimal("169.00"));
            updated.setDescription("Music");
            updated.setCategory(EnumBaseCategory.LEISURE);
            updated.setFixedAmount(true);
            updated.setActive(true);

            when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
            when(expenseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<ExpenseResponse> result = expenseService.updateExpense(1L, updated);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Spotify");
            assertThat(result.get().amount()).isEqualByComparingTo("169.00");
        }

        @Test
        @DisplayName("returns empty Optional when expense not found")
        void returnsEmptyWhenMissing() {
            when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(expenseService.updateExpense(99L, sampleExpense)).isEmpty();
            verify(expenseRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteExpense
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteExpense")
    class DeleteExpense {

        @Test
        @DisplayName("returns true and deletes when expense exists")
        void deletesExisting() {
            when(expenseRepository.existsById(1L)).thenReturn(true);

            assertThat(expenseService.deleteExpense(1L)).isTrue();
            verify(expenseRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when expense does not exist")
        void returnsFalseWhenMissing() {
            when(expenseRepository.existsById(99L)).thenReturn(false);

            assertThat(expenseService.deleteExpense(99L)).isFalse();
            verify(expenseRepository, never()).deleteById(anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // payExpense
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("payExpense")
    class PayExpense {

        @Test
        @DisplayName("deducts from account balance and creates transaction")
        void paysExpense() {
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TransactionEntity savedTx = new TransactionEntity();
            savedTx.setAmount(new BigDecimal("-599.00"));
            savedTx.setType(EnumTransactionType.EXPENSE);
            when(transactionRepository.save(any())).thenReturn(savedTx);

            TransactionEntity result = expenseService.payExpense(1L, 10L, new BigDecimal("599.00"), "Monthly bill");

            // Account balance should be reduced
            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("2401.00");

            // Captured transaction should have negated amount and correct type
            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            TransactionEntity captured = txCaptor.getValue();
            assertThat(captured.getAmount()).isEqualByComparingTo("-599.00");
            assertThat(captured.getType()).isEqualTo(EnumTransactionType.EXPENSE);
            assertThat(captured.getRemarks()).isEqualTo("Monthly bill");
        }

        @Test
        @DisplayName("uses absolute value so a negative input still deducts correctly")
        void handlesNegativeInput() {
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            expenseService.payExpense(1L, 10L, new BigDecimal("-599.00"), "negative input");

            // Should deduct 599, not add 599
            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("2401.00");
        }

        @Test
        @DisplayName("throws RuntimeException when expense not found")
        void throwsWhenExpenseMissing() {
            when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> expenseService.payExpense(99L, 10L, new BigDecimal("100"), ""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Expense not found");
        }

        @Test
        @DisplayName("throws RuntimeException when account not found")
        void throwsWhenAccountMissing() {
            when(expenseRepository.findById(1L)).thenReturn(Optional.of(sampleExpense));
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> expenseService.payExpense(1L, 99L, new BigDecimal("100"), ""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    // -------------------------------------------------------------------------
    // getRunRateExpenses
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getRunRateExpenses")
    class GetRunRateExpenses {

        @Test
        @DisplayName("aggregates run-rate correctly for multiple frequencies")
        void aggregatesCorrectly() {
            ExpenseEntity monthly = buildExpense(1L, "Netflix", new BigDecimal("600"), EnumBaseFrequency.MONTHLY, true);
            ExpenseEntity quarterly = buildExpense(2L, "Domain", new BigDecimal("300"), EnumBaseFrequency.QUARTERLY, true);
            ExpenseEntity yearly = buildExpense(3L, "Insurance", new BigDecimal("1200"), EnumBaseFrequency.YEARLY, true);

            when(expenseRepository.findAll()).thenReturn(List.of(monthly, quarterly, yearly));

            Map<EnumBaseFrequency, BigDecimal> result = expenseService.getRunRateExpenses();

            // Monthly = 600 (no weekly in expenses)
            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo("600");
            // Quarterly = 300 + 600*3 = 2100
            assertThat(result.get(EnumBaseFrequency.QUARTERLY)).isEqualByComparingTo("2100");
            // Semi-annual = 0 + 2100*2 = 4200
            assertThat(result.get(EnumBaseFrequency.BIYEARLY)).isEqualByComparingTo("4200");
            // Annual = 1200 + 4200*2 = 9600
            assertThat(result.get(EnumBaseFrequency.YEARLY)).isEqualByComparingTo("9600");
        }

        @Test
        @DisplayName("excludes inactive expenses from run-rate")
        void excludesInactive() {
            ExpenseEntity active = buildExpense(1L, "Netflix", new BigDecimal("600"), EnumBaseFrequency.MONTHLY, true);
            ExpenseEntity inactive = buildExpense(2L, "Old sub", new BigDecimal("999"), EnumBaseFrequency.MONTHLY, false);

            when(expenseRepository.findAll()).thenReturn(List.of(active, inactive));

            Map<EnumBaseFrequency, BigDecimal> result = expenseService.getRunRateExpenses();

            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo("600");
        }

        @Test
        @DisplayName("returns zeros when no active expenses")
        void returnsZerosWhenEmpty() {
            when(expenseRepository.findAll()).thenReturn(Collections.emptyList());

            Map<EnumBaseFrequency, BigDecimal> result = expenseService.getRunRateExpenses();

            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.get(EnumBaseFrequency.YEARLY)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        private ExpenseEntity buildExpense(long id, String name, BigDecimal amount,
                                           EnumBaseFrequency frequency, boolean active) {
            ExpenseEntity e = new ExpenseEntity();
            e.setId(id);
            e.setName(name);
            e.setAmount(amount);
            e.setFrequency(frequency);
            e.setActive(active);
            e.setCategory(EnumBaseCategory.OTHER);
            return e;
        }
    }
}
