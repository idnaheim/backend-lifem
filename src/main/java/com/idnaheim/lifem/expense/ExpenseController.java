package com.idnaheim.lifem.expense;

import com.idnaheim.lifem.enums.ExpenseFrequency;
import com.idnaheim.lifem.utilities.ApiResponse;
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
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/runrate")
    public ResponseEntity<ApiResponse<Map<ExpenseFrequency, BigDecimal>>> getRunRateExpenses() {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getRunRateExpenses()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {
        return ResponseEntity.ok(ApiResponse.success(expenseService.getAllExpenses()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(@PathVariable long id) {
        return expenseService.getExpenseById(id)
                .<ResponseEntity<ApiResponse<ExpenseResponse>>>map(expense -> ResponseEntity.ok(ApiResponse.success(expense)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(@RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(expenseService.createExpense(request.toEntity())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(@PathVariable long id, @RequestBody ExpenseRequest request) {
        return expenseService.updateExpense(id, request.toEntity())
                .<ResponseEntity<ApiResponse<ExpenseResponse>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(@PathVariable long id) {
        if (expenseService.deleteExpense(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Expense deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<?>> payExpense(
            @PathVariable long id,
            @RequestParam long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remarks) {
        try {
            return ResponseEntity.ok(ApiResponse.success(expenseService.payExpense(id, accountId, amount, remarks)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        }
    }

}
