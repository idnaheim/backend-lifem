package com.idnaheim.lifem.income;

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
class IncomeServiceTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private IncomeService incomeService;

    private IncomeEntity sampleIncome;
    private AccountEntity sampleAccount;

    @BeforeEach
    void setUp() {
        sampleAccount = new AccountEntity();
        sampleAccount.setId(10L);
        sampleAccount.setName("BDO Savings");
        sampleAccount.setBalance(new BigDecimal("20000.00"));
        sampleAccount.setCategory(EnumAccountCategory.BANK);
        sampleAccount.setType(EnumAccountType.SAVINGS);

        sampleIncome = new IncomeEntity();
        sampleIncome.setId(1L);
        sampleIncome.setName("Salary");
        sampleIncome.setSource("Employer");
        sampleIncome.setAmount(new BigDecimal("50000.00"));
        sampleIncome.setCategory(EnumBaseCategory.SALARY);
        sampleIncome.setFrequency(EnumBaseFrequency.MONTHLY);
        sampleIncome.setActive(true);
        sampleIncome.setRemarks("Monthly salary");
        sampleIncome.setAccount(sampleAccount);
    }

    // -------------------------------------------------------------------------
    // getAllIncomes
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllIncomes")
    class GetAllIncomes {

        @Test
        @DisplayName("returns all incomes mapped to response DTOs")
        void returnsAll() {
            when(incomeRepository.findAll()).thenReturn(List.of(sampleIncome));
            when(transactionRepository.findByIncomeId(1L)).thenReturn(Collections.emptyList());

            List<IncomeResponse> result = incomeService.getAllIncomes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("Salary");
            assertThat(result.get(0).accountId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("returns empty list when no incomes exist")
        void returnsEmpty() {
            when(incomeRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(incomeService.getAllIncomes()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getActiveIncomes
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getActiveIncomes")
    class GetActiveIncomes {

        @Test
        @DisplayName("returns only active incomes")
        void returnsActive() {
            when(incomeRepository.findByIsActiveTrue()).thenReturn(List.of(sampleIncome));
            when(transactionRepository.findByIncomeId(1L)).thenReturn(Collections.emptyList());

            List<IncomeResponse> result = incomeService.getActiveIncomes();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isActive()).isTrue();
        }

        @Test
        @DisplayName("returns empty list when no active incomes")
        void returnsEmptyWhenNoneActive() {
            when(incomeRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

            assertThat(incomeService.getActiveIncomes()).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // getIncomeById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getIncomeById")
    class GetIncomeById {

        @Test
        @DisplayName("returns mapped response when income exists")
        void found() {
            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(transactionRepository.findByIncomeId(1L)).thenReturn(Collections.emptyList());

            Optional<IncomeResponse> result = incomeService.getIncomeById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo(1L);
            assertThat(result.get().name()).isEqualTo("Salary");
        }

        @Test
        @DisplayName("returns empty Optional when income not found")
        void notFound() {
            when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(incomeService.getIncomeById(99L)).isEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // createIncome
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createIncome")
    class CreateIncome {

        @Test
        @DisplayName("saves and returns income response with account linked")
        void savesWithAccount() {
            IncomeRequest request = new IncomeRequest(
                    "Salary", "Employer", new BigDecimal("50000.00"),
                    EnumBaseCategory.SALARY, EnumBaseFrequency.MONTHLY,
                    10L, true, "Monthly salary"
            );

            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(incomeRepository.save(any())).thenAnswer(inv -> {
                IncomeEntity saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            IncomeResponse result = incomeService.createIncome(request);

            assertThat(result.name()).isEqualTo("Salary");
            assertThat(result.accountId()).isEqualTo(10L);
            verify(incomeRepository).save(any(IncomeEntity.class));
        }

        @Test
        @DisplayName("saves income with null account when accountId is null")
        void savesWithoutAccount() {
            IncomeRequest request = new IncomeRequest(
                    "Freelance", "Client", new BigDecimal("10000.00"),
                    EnumBaseCategory.WORK, EnumBaseFrequency.ONCE,
                    null, true, null
            );

            when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            IncomeResponse result = incomeService.createIncome(request);

            assertThat(result.name()).isEqualTo("Freelance");
            assertThat(result.accountId()).isNull();
            verify(accountRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("throws when accountId provided but account not found")
        void throwsWhenAccountMissing() {
            IncomeRequest request = new IncomeRequest(
                    "Salary", "Employer", new BigDecimal("50000.00"),
                    EnumBaseCategory.SALARY, EnumBaseFrequency.MONTHLY,
                    99L, true, null
            );

            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incomeService.createIncome(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    // -------------------------------------------------------------------------
    // updateIncome
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updateIncome")
    class UpdateIncome {

        @Test
        @DisplayName("updates fields and returns updated response when found")
        void updatesWhenFound() {
            IncomeRequest request = new IncomeRequest(
                    "Updated Salary", "New Employer", new BigDecimal("60000.00"),
                    EnumBaseCategory.SALARY, EnumBaseFrequency.MONTHLY,
                    10L, true, "Updated"
            );

            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(incomeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<IncomeResponse> result = incomeService.updateIncome(1L, request);

            assertThat(result).isPresent();
            assertThat(result.get().name()).isEqualTo("Updated Salary");
            assertThat(result.get().amount()).isEqualByComparingTo("60000.00");
        }

        @Test
        @DisplayName("returns empty Optional when income not found")
        void returnsEmptyWhenMissing() {
            IncomeRequest request = new IncomeRequest(
                    "X", "X", BigDecimal.ONE,
                    EnumBaseCategory.OTHER, EnumBaseFrequency.ONCE,
                    null, false, null
            );

            when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(incomeService.updateIncome(99L, request)).isEmpty();
            verify(incomeRepository, never()).save(any());
        }
    }

    // -------------------------------------------------------------------------
    // deleteIncome
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deleteIncome")
    class DeleteIncome {

        @Test
        @DisplayName("returns true and deletes when income exists")
        void deletesExisting() {
            when(incomeRepository.existsById(1L)).thenReturn(true);

            assertThat(incomeService.deleteIncome(1L)).isTrue();
            verify(incomeRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when income does not exist")
        void returnsFalseWhenMissing() {
            when(incomeRepository.existsById(99L)).thenReturn(false);

            assertThat(incomeService.deleteIncome(99L)).isFalse();
            verify(incomeRepository, never()).deleteById(anyLong());
        }
    }

    // -------------------------------------------------------------------------
    // receiveIncome
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("receiveIncome")
    class ReceiveIncome {

        @Test
        @DisplayName("credits account balance and saves transaction")
        void creditsBalance() {
            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            incomeService.receiveIncome(1L, 10L, new BigDecimal("50000.00"), "May salary");

            assertThat(sampleAccount.getBalance()).isEqualByComparingTo("70000.00");

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            TransactionEntity captured = txCaptor.getValue();
            assertThat(captured.getAmount()).isEqualByComparingTo("50000.00");
            assertThat(captured.getType()).isEqualTo(EnumTransactionType.INCOME);
            assertThat(captured.getRemarks()).isEqualTo("May salary");
        }

        @Test
        @DisplayName("uses default remarks when remarks is null")
        void usesDefaultRemarksWhenNull() {
            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            incomeService.receiveIncome(1L, 10L, new BigDecimal("50000.00"), null);

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getRemarks()).contains("Salary");
        }

        @Test
        @DisplayName("uses default remarks when remarks is blank")
        void usesDefaultRemarksWhenBlank() {
            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(accountRepository.findById(10L)).thenReturn(Optional.of(sampleAccount));
            when(accountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            incomeService.receiveIncome(1L, 10L, new BigDecimal("50000.00"), "   ");

            ArgumentCaptor<TransactionEntity> txCaptor = ArgumentCaptor.forClass(TransactionEntity.class);
            verify(transactionRepository).save(txCaptor.capture());
            assertThat(txCaptor.getValue().getRemarks()).contains("Salary");
        }

        @Test
        @DisplayName("throws when income not found")
        void throwsWhenIncomeMissing() {
            when(incomeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incomeService.receiveIncome(99L, 10L, new BigDecimal("100"), ""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Income not found");
        }

        @Test
        @DisplayName("throws when account not found")
        void throwsWhenAccountMissing() {
            when(incomeRepository.findById(1L)).thenReturn(Optional.of(sampleIncome));
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incomeService.receiveIncome(1L, 99L, new BigDecimal("100"), ""))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Account not found");
        }
    }

    // -------------------------------------------------------------------------
    // getRunRateIncomes
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getRunRateIncomes")
    class GetRunRateIncomes {

        @Test
        @DisplayName("aggregates run-rate correctly across frequencies")
        void aggregatesCorrectly() {
            IncomeEntity weekly  = buildIncome(1L, "Part-time",  new BigDecimal("2000"),  EnumBaseFrequency.WEEKLY,    true);
            IncomeEntity monthly = buildIncome(2L, "Salary",     new BigDecimal("50000"), EnumBaseFrequency.MONTHLY,   true);
            IncomeEntity yearly  = buildIncome(3L, "Bonus",      new BigDecimal("60000"), EnumBaseFrequency.YEARLY,    true);

            when(incomeRepository.findAll()).thenReturn(List.of(weekly, monthly, yearly));

            Map<EnumBaseFrequency, BigDecimal> result = incomeService.getRunRateIncomes();

            // Weekly = 2000
            assertThat(result.get(EnumBaseFrequency.WEEKLY)).isEqualByComparingTo("2000");
            // Monthly = 50000 + 2000*4 = 58000
            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo("58000");
            // Quarterly = 0 + 58000*3 = 174000
            assertThat(result.get(EnumBaseFrequency.QUARTERLY)).isEqualByComparingTo("174000");
            // Annual = 60000 + 174000*4 = 756000
            assertThat(result.get(EnumBaseFrequency.YEARLY)).isEqualByComparingTo("756000");
        }

        @Test
        @DisplayName("excludes inactive incomes from run-rate")
        void excludesInactive() {
            IncomeEntity active   = buildIncome(1L, "Salary",    new BigDecimal("50000"), EnumBaseFrequency.MONTHLY, true);
            IncomeEntity inactive = buildIncome(2L, "Old job",   new BigDecimal("30000"), EnumBaseFrequency.MONTHLY, false);

            when(incomeRepository.findAll()).thenReturn(List.of(active, inactive));

            Map<EnumBaseFrequency, BigDecimal> result = incomeService.getRunRateIncomes();

            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo("50000");
        }

        @Test
        @DisplayName("returns zeros when no active incomes")
        void returnsZerosWhenEmpty() {
            when(incomeRepository.findAll()).thenReturn(Collections.emptyList());

            Map<EnumBaseFrequency, BigDecimal> result = incomeService.getRunRateIncomes();

            assertThat(result.get(EnumBaseFrequency.WEEKLY)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.get(EnumBaseFrequency.MONTHLY)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.get(EnumBaseFrequency.QUARTERLY)).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(result.get(EnumBaseFrequency.YEARLY)).isEqualByComparingTo(BigDecimal.ZERO);
        }

        private IncomeEntity buildIncome(long id, String name, BigDecimal amount,
                                         EnumBaseFrequency frequency, boolean active) {
            IncomeEntity e = new IncomeEntity();
            e.setId(id);
            e.setName(name);
            e.setAmount(amount);
            e.setFrequency(frequency);
            e.setActive(active);
            e.setCategory(EnumBaseCategory.SALARY);
            return e;
        }
    }
}
