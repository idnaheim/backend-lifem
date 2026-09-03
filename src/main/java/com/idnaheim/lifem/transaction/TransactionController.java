package com.idnaheim.lifem.transaction;

import com.idnaheim.lifem.utilities.CustomResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
@Tag(name = "Transactions", description = "Ledger of all financial movements linked to accounts, expenses, or incomes")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "List all transactions")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<List<TransactionEntity>>> getAllTransactions() {
        return ResponseEntity.ok(CustomResponse.success(transactionService.getAllTransactions()));
    }

    @Operation(summary = "Get transaction by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<TransactionEntity>> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable long id) {
        return transactionService.getTransactionById(id)
                .<ResponseEntity<CustomResponse<TransactionEntity>>>map(transaction -> ResponseEntity.ok(CustomResponse.success(transaction)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Create a raw transaction",
               description = "Directly creates a transaction entry. Prefer /transactions/expense or /transactions/income for domain-specific recording.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<TransactionEntity>> createTransaction(@RequestBody TransactionEntity transaction) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(transactionService.createTransaction(transaction)));
    }

    @Operation(summary = "Update a transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction updated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<TransactionEntity>> updateTransaction(
            @Parameter(description = "Transaction ID") @PathVariable long id,
            @RequestBody TransactionRequest request) {
        return transactionService.updateTransaction(id, request)
                .<ResponseEntity<CustomResponse<TransactionEntity>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete a transaction")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction deleted"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteTransaction(
            @Parameter(description = "Transaction ID") @PathVariable long id) {
        if (transactionService.deleteTransaction(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Transaction deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

    @Operation(summary = "Record an expense transaction",
               description = "Records a debit transaction tied to an expense and debits the linked account.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense transaction recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid data (e.g. account or expense not found)")
    })
    @PostMapping("/expense")
    public ResponseEntity<CustomResponse<TransactionEntity>> recordExpense(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CustomResponse.created(transactionService.recordExpense(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomResponse.badRequest(e.getMessage()));
        }
    }

    @Operation(summary = "Record an income transaction",
               description = "Records a credit transaction tied to an income source and credits the linked account.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Income transaction recorded"),
        @ApiResponse(responseCode = "400", description = "Invalid data (e.g. account or income not found)")
    })
    @PostMapping("/income")
    public ResponseEntity<CustomResponse<TransactionEntity>> recordIncome(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(CustomResponse.created(transactionService.recordIncome(request)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomResponse.badRequest(e.getMessage()));
        }
    }

}
