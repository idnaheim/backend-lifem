package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.utilities.ApiResponse;
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
    public ResponseEntity<ApiResponse<?>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getAllTransactions()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getTransactionById(@PathVariable long id) {
        return transactionService.getTransactionById(id)
                .<ResponseEntity<ApiResponse<?>>>map(transaction -> ResponseEntity.ok(ApiResponse.success(transaction)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createTransaction(@RequestBody TransactionEntity transaction) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(transactionService.createTransaction(transaction)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTransaction(@PathVariable long id, @RequestBody TransactionRequest request) {
        return transactionService.updateTransaction(id, request)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteTransaction(@PathVariable long id) {
        if (transactionService.deleteTransaction(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Transaction deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

    @PostMapping("/expense")
    public ResponseEntity<ApiResponse<?>> recordExpense(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(transactionService.recordExpense(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        }
    }

    @PostMapping("/income")
    public ResponseEntity<ApiResponse<?>> recordIncome(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.created(transactionService.recordIncome(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        }
    }

}
