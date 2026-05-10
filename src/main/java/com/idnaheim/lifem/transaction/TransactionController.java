package com.idnaheim.lifem.transaction;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity getAllTransactions() {
        return new ResponseEntity(transactionService.getAllTransactions(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getTransactionById(@PathVariable long id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> new ResponseEntity(transaction, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity createTransaction(@RequestBody TransactionEntity transaction) {
        return new ResponseEntity(transactionService.createTransaction(transaction), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateTransaction(@PathVariable long id, @RequestBody TransactionRequest request) {
        return transactionService.updateTransaction(id, request)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTransaction(@PathVariable long id) {
        if (transactionService.deleteTransaction(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/expense")
    public ResponseEntity recordExpense(@RequestBody TransactionRequest request) {
        try {
            return new ResponseEntity(transactionService.recordExpense(request), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/income")
    public ResponseEntity recordIncome(@RequestBody TransactionRequest request) {
        try {
            return new ResponseEntity(transactionService.recordIncome(request), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
