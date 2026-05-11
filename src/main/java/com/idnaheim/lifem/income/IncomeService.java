package com.idnaheim.lifem.income;

import com.idnaheim.lifem.account.AccountEntity;
import com.idnaheim.lifem.account.AccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final AccountRepository accountRepository;

    public List<IncomeResponse> getAllIncomes() {
        return incomeRepository.findAll().stream()
                .map(IncomeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<IncomeResponse> getActiveIncomes() {
        return incomeRepository.findByIsActiveTrue().stream()
                .map(IncomeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<IncomeResponse> getIncomeById(long id) {
        return incomeRepository.findById(id).map(IncomeResponse::fromEntity);
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
        entity.setName(request.getName());
        entity.setSource(request.getSource());
        entity.setAmount(request.getAmount());
        entity.setCategory(request.getCategory());
        entity.setFrequency(request.getFrequency());
        entity.setActive(request.isActive());
        entity.setRemarks(request.getRemarks());

        if (request.getAccountId() != null) {
            AccountEntity account = accountRepository.findById(request.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found: " + request.getAccountId()));
            entity.setAccount(account);
        } else {
            entity.setAccount(null);
        }
    }

}
