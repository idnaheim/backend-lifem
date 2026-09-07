package com.idnaheim.lifem.income;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.enums.EnumTransactionType;
import com.idnaheim.lifem.transaction.TransactionEntity;
import com.idnaheim.lifem.transaction.TransactionRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

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

        List<IncomeEntity> result = new ArrayList<>();

        for (IncomeEntity incomeEntity : incomeRepository.findAll()) {
            List<TransactionEntity> transactionsList = transactionRepository.findByIncomeId(incomeEntity.getId());
            incomeEntity.setTransactions(transactionsList);
            result.add(incomeEntity);
        }

        return result.stream().map(IncomeResponse::fromEntity).toList();
    }

    public List<IncomeResponse> getActiveIncomes() {

        List<IncomeEntity> result = new ArrayList<>();

        for (IncomeEntity incomeEntity : incomeRepository.findByIsActiveTrue()) {
            List<TransactionEntity> transactionsList = transactionRepository.findByIncomeId(incomeEntity.getId());
            incomeEntity.setTransactions(transactionsList);
            result.add(incomeEntity);
        }

        return result.stream().map(IncomeResponse::fromEntity).toList();
    }


    public Optional<IncomeResponse> getIncomeById(long id) {

        Optional<IncomeEntity> incomeEntity = incomeRepository.findById(id);

        if(incomeEntity.isPresent()) {
            IncomeEntity result = incomeEntity.get();
            List<TransactionEntity> transactionsList = transactionRepository.findByIncomeId(result.getId());
            result.setTransactions(transactionsList);
            return Optional.of(IncomeResponse.fromEntity(result));
        }

        return Optional.empty();
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
