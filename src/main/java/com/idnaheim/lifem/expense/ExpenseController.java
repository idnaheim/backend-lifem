package com.idnaheim.lifem.expense;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/expenses")
@AllArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/runrate")
    public ResponseEntity getMonthlyExpense(){
        return new ResponseEntity(expenseService.getRunRateExpenses(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getAllExpenses() {
        return new ResponseEntity(expenseService.getAllExpenses(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getExpenseById(@PathVariable long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> new ResponseEntity(expense, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity createExpense(@RequestBody ExpenseEntity expense) {
        return new ResponseEntity(expenseService.createExpense(expense), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateExpense(@PathVariable long id, @RequestBody ExpenseEntity expense) {
        return expenseService.updateExpense(id, expense)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteExpense(@PathVariable long id) {
        if (expenseService.deleteExpense(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity payExpense(@PathVariable long id, @RequestParam long accountId, @RequestParam BigDecimal amount, @RequestParam(required = false) String remarks) {
        try {
            return new ResponseEntity(expenseService.payExpense(id, accountId, amount, remarks), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
