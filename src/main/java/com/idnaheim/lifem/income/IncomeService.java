package com.idnaheim.lifem.income;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.enums.EnumTransactionType;
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
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Map<EnumBaseFrequency, BigDecimal> getRunRateIncomes() {
        List<IncomeEntity> incomes = incomeRepository.findAll();
        Map<EnumBaseFrequency, BigDecimal> result = new HashMap<>();

        BigDecimal weekly = BigDecimal.ZERO;
        BigDecimal monthly = BigDecimal.ZERO;
        BigDecimal quarterly = BigDecimal.ZERO;
        BigDecimal annual = BigDecimal.ZERO;
        BigDecimal oneTime = BigDecimal.ZERO;

        for (IncomeEntity income : incomes) {
            if (!income.isActive()) continue;

            BigDecimal amount = income.getAmount();
            switch (income.getFrequency()) {
                case WEEKLY:
                    weekly = weekly.add(amount);
                    break;
                case MONTHLY:
                    monthly = monthly.add(amount);
                    break;
                case QUARTERLY:
                    quarterly = quarterly.add(amount);
                    break;
                case YEARLY:
                    annual = annual.add(amount);
                    break;
                case ONCE:
                    oneTime = oneTime.add(amount);
                    break;
            }
        }

        BigDecimal aggregatedMonthly = monthly.add(weekly.multiply(BigDecimal.valueOf(4)));
        BigDecimal aggregatedQuarterly = quarterly.add(aggregatedMonthly.multiply(BigDecimal.valueOf(3)));
        BigDecimal aggregatedAnnual = annual.add(aggregatedQuarterly.multiply(BigDecimal.valueOf(4)).add(oneTime));

        result.put(EnumBaseFrequency.WEEKLY, weekly);
        result.put(EnumBaseFrequency.MONTHLY, aggregatedMonthly);
        result.put(EnumBaseFrequency.QUARTERLY, aggregatedQuarterly);
        result.put(EnumBaseFrequency.YEARLY, aggregatedAnnual);

        return result;
    }

    public List<IncomeResponse> getAllIncomes() {
        List<IncomeEntity> incomes = incomeRepository.findAll();
        incomes.forEach(this::populateIsReceived);
        return incomes.stream().map(IncomeResponse::fromEntity).toList();
    }

    public List<IncomeResponse> getActiveIncomes() {
        List<IncomeEntity> incomes = incomeRepository.findByIsActiveTrue();
        incomes.forEach(this::populateIsReceived);
        return incomes.stream().map(IncomeResponse::fromEntity).toList();
    }

    public Optional<IncomeResponse> getIncomeById(long id) {
        return incomeRepository.findById(id).map(income -> {
            populateIsReceived(income);
            return IncomeResponse.fromEntity(income);
        });
    }

    private void populateIsReceived(IncomeEntity income) {
        LocalDateTime start;
        LocalDateTime end;
        LocalDate now = LocalDate.now();

        switch (income.getFrequency()) {
            case WEEKLY:
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                start = startOfWeek.atStartOfDay();
                end = startOfWeek.plusDays(6).atTime(LocalTime.MAX);
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
            case ONCE:
            case UNPLANNED:
                start = LocalDateTime.of(2000, 1, 1, 0, 0);
                end = LocalDateTime.of(2099, 12, 31, 23, 59, 59);
                break;
            default:
                // MONTHLY
                start = now.withDayOfMonth(1).atStartOfDay();
                end = now.withDayOfMonth(now.lengthOfMonth()).atTime(LocalTime.MAX);
                break;
        }

        boolean receivedThisPeriod = transactionRepository.existsByIncomeIdAndCreatedDateBetween(
                income.getId(), start, end);
        income.setReceived(receivedThisPeriod);

        income.setTransactions(transactionRepository.findByIncomeIdAndCreatedDateBetween(
                income.getId(), start, end));

        // Calculate missed payments from the income's creation date
        if (income.getFrequency() != EnumBaseFrequency.ONCE) {
            LocalDateTime incomeStart = income.getCreatedDate() != null
                    ? income.getCreatedDate().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : null;
            if (incomeStart != null) {
                long expectedPayments = calculateExpectedPayments(incomeStart, now, income.getFrequency());
                long actualPayments = transactionRepository.countByIncomeIdAndCreatedDateAfter(
                        income.getId(), incomeStart);
                long missed = expectedPayments - actualPayments;
                income.setMissedPayments(Math.max(0, missed));
            } else {
                income.setMissedPayments(0);
            }
        } else {
            income.setMissedPayments(0);
        }
    }

    private long calculateExpectedPayments(LocalDateTime startDate, LocalDate now, EnumBaseFrequency frequency) {
        LocalDate start = startDate.toLocalDate();
        if (start.isAfter(now)) {
            return 0;
        }

        switch (frequency) {
            case WEEKLY:
                return ChronoUnit.WEEKS.between(start, now) + 1;
            case MONTHLY:
                return ChronoUnit.MONTHS.between(start, now) + 1;
            case QUARTERLY:
                return ChronoUnit.MONTHS.between(start, now) / 3 + 1;
            case YEARLY:
                return ChronoUnit.YEARS.between(start, now) + 1;
            default:
                return 0;
        }
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

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setIncome(income);
        transaction.setType(EnumTransactionType.INCOME);
        transaction.setAmount(amount);
        transaction.setRemarks(remarks != null && !remarks.isBlank() ? remarks : "Received from: " + income.getName());

        return transactionRepository.save(transaction);
    }

}
