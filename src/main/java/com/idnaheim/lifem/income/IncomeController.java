package com.idnaheim.lifem.income;

import com.idnaheim.lifem.utilities.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/incomes")
@AllArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllIncomes() {
        return ResponseEntity.ok(ApiResponse.success(incomeService.getAllIncomes()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<?>> getActiveIncomes() {
        return ResponseEntity.ok(ApiResponse.success(incomeService.getActiveIncomes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getIncomeById(@PathVariable long id) {
        return incomeService.getIncomeById(id)
                .<ResponseEntity<ApiResponse<?>>>map(income -> ResponseEntity.ok(ApiResponse.success(income)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createIncome(@RequestBody IncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(incomeService.createIncome(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateIncome(@PathVariable long id, @RequestBody IncomeRequest request) {
        return incomeService.updateIncome(id, request)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteIncome(@PathVariable long id) {
        if (incomeService.deleteIncome(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Income deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<?>> receiveIncome(
            @PathVariable long id,
            @RequestParam long accountId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remarks) {
        try {
            return ResponseEntity.ok(ApiResponse.success(incomeService.receiveIncome(id, accountId, amount, remarks)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        }
    }

}
