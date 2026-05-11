package com.idnaheim.lifem.income;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/incomes")
@AllArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity getAllIncomes() {
        return new ResponseEntity(incomeService.getAllIncomes(), HttpStatus.OK);
    }

    @GetMapping("/active")
    public ResponseEntity getActiveIncomes() {
        return new ResponseEntity(incomeService.getActiveIncomes(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getIncomeById(@PathVariable long id) {
        return incomeService.getIncomeById(id)
                .map(income -> new ResponseEntity(income, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity createIncome(@RequestBody IncomeRequest request) {
        return new ResponseEntity(incomeService.createIncome(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateIncome(@PathVariable long id, @RequestBody IncomeRequest request) {
        return incomeService.updateIncome(id, request)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteIncome(@PathVariable long id) {
        if (incomeService.deleteIncome(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

}
