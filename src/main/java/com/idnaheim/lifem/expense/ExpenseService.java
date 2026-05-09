package com.idnaheim.lifem.expense;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public List<ExpenseEntity> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Optional<ExpenseEntity> getExpenseById(long id) {
        return expenseRepository.findById(id);
    }

    public ExpenseEntity createExpense(ExpenseEntity expense) {
        return expenseRepository.save(expense);
    }

    public Optional<ExpenseEntity> updateExpense(long id, ExpenseEntity updatedExpense) {
        return expenseRepository.findById(id).map(existing -> {
            existing.setName(updatedExpense.getName());
            existing.setFrequency(updatedExpense.getFrequency());
            existing.setAmount(updatedExpense.getAmount());
            existing.setDescription(updatedExpense.getDescription());
            existing.setPaymentStartDate(updatedExpense.getPaymentStartDate());
            existing.setPaymentStartDate(updatedExpense.getPaymentStartDate());
            existing.setCategory(updatedExpense.getCategory());
            return expenseRepository.save(existing);
        });
    }

    public boolean deleteExpense(long id) {
        if (expenseRepository.existsById(id)) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
