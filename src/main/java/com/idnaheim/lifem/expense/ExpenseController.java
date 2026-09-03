package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.enums.ExpenseFrequency;
import com.idnaheim.lifem.transaction.TransactionEntity;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/expenses")
@AllArgsConstructor
@Tag(name = "Expenses", description = "Recurring and one-time expense tracking")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Get expense run-rate",
               description = "Returns projected expense totals grouped by frequency (DAILY, WEEKLY, MONTHLY, etc.).")
    @ApiResponse(responseCode = "200", description = "Run-rate calculated successfully")
    @GetMapping("/runrate")
    public ResponseEntity<CustomResponse<Map<ExpenseFrequency, BigDecimal>>> getRunRateExpenses() {
        return ResponseEntity.ok(CustomResponse.success(expenseService.getRunRateExpenses()));
    }

    @Operation(summary = "List all expenses")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<List<ExpenseResponse>>> getAllExpenses() {
        return ResponseEntity.ok(CustomResponse.success(expenseService.getAllExpenses()));
    }

    @Operation(summary = "Get expense by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense found"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<ExpenseResponse>> getExpenseById(
            @Parameter(description = "Expense ID") @PathVariable long id) {
        return expenseService.getExpenseById(id)
                .<ResponseEntity<CustomResponse<ExpenseResponse>>>map(expense -> ResponseEntity.ok(CustomResponse.success(expense)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Create a new expense")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Expense created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<ExpenseResponse>> createExpense(@RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(expenseService.createExpense(request.toEntity())));
    }

    @Operation(summary = "Update an existing expense")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense updated"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<ExpenseResponse>> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable long id,
            @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(id, request.toEntity())
                .<ResponseEntity<CustomResponse<ExpenseResponse>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete an expense")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expense deleted"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteExpense(
            @Parameter(description = "Expense ID") @PathVariable long id) {
        if (expenseService.deleteExpense(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Expense deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

    @Operation(summary = "Pay an expense",
               description = "Records a payment against an expense from a specified account.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment recorded"),
        @ApiResponse(responseCode = "400", description = "Payment failed (e.g. insufficient balance)"),
        @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    @PostMapping("/{id}/pay")
    public ResponseEntity<CustomResponse<TransactionEntity>> payExpense(
            @Parameter(description = "Expense ID") @PathVariable long id,
            @Parameter(description = "Account ID to debit") @RequestParam long accountId,
            @Parameter(description = "Payment amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Optional remarks") @RequestParam(required = false) String remarks) {
        try {
            return ResponseEntity.ok(CustomResponse.success(expenseService.payExpense(id, accountId, amount, remarks)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomResponse.badRequest(e.getMessage()));
        }
    }

}
