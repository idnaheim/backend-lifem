package com.idnaheim.lifem.income;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.IncomeFrequency;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public List<IncomeResponse> getAllIncomes() {
        List<IncomeEntity> incomes = incomeRepository.findAll();
        incomes.forEach(this::populateTransactions);
        return incomes.stream()
                .map(IncomeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<IncomeResponse> getActiveIncomes() {
        List<IncomeEntity> incomes = incomeRepository.findByIsActiveTrue();
        incomes.forEach(this::populateTransactions);
        return incomes.stream()
                .map(IncomeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<IncomeResponse> getIncomeById(long id) {
        return incomeRepository.findById(id).map(income -> {
            populateTransactions(income);
            return IncomeResponse.fromEntity(income);
        });
    }

    private void populateTransactions(IncomeEntity income) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate now = LocalDate.now();

        switch (income.getFrequency()) {
            case WEEKLY:
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                start = startOfWeek.atStartOfDay();
                end = startOfWeek.plusDays(6).atTime(LocalTime.MAX);
                break;
            case BI_WEEKLY:
                // Use current two-week window from start of month
                int dayOfMonth = now.getDayOfMonth();
                if (dayOfMonth <= 15) {
                    start = now.withDayOfMonth(1).atStartOfDay();
                    end = now.withDayOfMonth(15).atTime(LocalTime.MAX);
                } else {
                    start = now.withDayOfMonth(16).atStartOfDay();
                    end = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
                }
                break;
            case QUARTERLY:
                int quarterStartMonth = ((now.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate startOfQuarter = now.withMonth(quarterStartMonth).withDayOfMonth(1);
                LocalDate endOfQuarter = startOfQuarter.plusMonths(2)
                        .withDayOfMonth(startOfQuarter.plusMonths(2).lengthOfMonth());
                start = startOfQuarter.atStartOfDay();
                end = endOfQuarter.atTime(LocalTime.MAX);
                break;
            case YEARLY:
                start = now.withDayOfYear(1).atStartOfDay();
                end = now.withDayOfYear(now.lengthOfYear()).atTime(LocalTime.MAX);
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

        income.setTransactions(transactionRepository.findByIncomeIdAndCreatedDateBetween(
                income.getId(), start, end));
    }

    public IncomeResponse createIncome(IncomeRequest request) {
        IncomeEntity income = new IncomeEntity();
        mapRequestToEntity(request, income);
        return IncomeResponse.fromEntity(incomeRepository.save(income));
    }

    public Optional<IncomeResponse> updateIncome(long id, IncomeRequest request) {
        return incomeRepository.findById(id).map(existing -> {
            mapRequestToEntity(request, existing);
            return IncomeResponse.fromEntity(incomeRepository.save(existing));
        });
    }

    public boolean deleteIncome(long id) {
        if (incomeRepository.existsById(id)) {
            incomeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void mapRequestToEntity(IncomeRequest request, IncomeEntity entity) {
        entity.setName(request.name());
        entity.setSource(request.source());
        entity.setAmount(request.amount());
        entity.setCategory(request.category());
        entity.setFrequency(request.frequency());
        entity.setActive(request.isActive());
        entity.setRemarks(request.remarks());

        if (request.accountId() != null) {
            AccountEntity account = accountRepository.findById(request.accountId())
                    .orElseThrow(() -> new RuntimeException("Account not found: " + request.accountId()));
            entity.setAccount(account);
        } else {
            entity.setAccount(null);
        }
    }

    @Transactional
    public TransactionEntity receiveIncome(long incomeId, long accountId, BigDecimal amount, String remarks) {
        IncomeEntity income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        // Add to account balance
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // Create transaction record
        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setIncome(income);
        transaction.setType(TransactionType.INCOME);
        transaction.setAmount(amount);
        transaction.setRemarks(remarks != null && !remarks.isBlank() ? remarks : "Received from: " + income.getName());

        return transactionRepository.save(transaction);
    }

}
