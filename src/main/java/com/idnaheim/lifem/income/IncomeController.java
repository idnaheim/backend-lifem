package com.idnaheim.lifem.income;


import com.idnaheim.lifem.enums.EnumBaseFrequency;
import com.idnaheim.lifem.transaction.TransactionResponse;
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
@RequestMapping("/incomes")
@AllArgsConstructor
@Tag(name = "Incomes", description = "Income source tracking")
public class IncomeController {

    private final IncomeService incomeService;

    @Operation(summary = "Get income run-rate",
               description = "Returns projected income totals grouped by frequency (WEEKLY, MONTHLY, QUARTERLY, YEARLY).")
    @ApiResponse(responseCode = "200", description = "Run-rate calculated successfully")
    @GetMapping("/runrate")
    public ResponseEntity<CustomResponse<Map<EnumBaseFrequency, BigDecimal>>> getRunRateIncomes() {
        return ResponseEntity.ok(CustomResponse.success(incomeService.getRunRateIncomes()));
    }

    @Operation(summary = "List all income sources")
    @ApiResponse(responseCode = "200", description = "Incomes retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<List<IncomeResponse>>> getAllIncomes() {
        return ResponseEntity.ok(CustomResponse.success(incomeService.getAllIncomes()));
    }

    @Operation(summary = "List active income sources")
    @ApiResponse(responseCode = "200", description = "Active incomes retrieved")
    @GetMapping("/active")
    public ResponseEntity<CustomResponse<List<IncomeResponse>>> getActiveIncomes() {
        return ResponseEntity.ok(CustomResponse.success(incomeService.getActiveIncomes()));
    }

    @Operation(summary = "Get income by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Income found"),
        @ApiResponse(responseCode = "404", description = "Income not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<IncomeResponse>> getIncomeById(
            @Parameter(description = "Income ID") @PathVariable long id) {
        return incomeService.getIncomeById(id)
                .<ResponseEntity<CustomResponse<IncomeResponse>>>map(income -> ResponseEntity.ok(CustomResponse.success(income)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Create a new income source")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Income created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<IncomeResponse>> createIncome(@RequestBody IncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(incomeService.createIncome(request)));
    }

    @Operation(summary = "Update an existing income source")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Income updated"),
        @ApiResponse(responseCode = "404", description = "Income not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<IncomeResponse>> updateIncome(
            @Parameter(description = "Income ID") @PathVariable long id,
            @RequestBody IncomeRequest request) {
        return incomeService.updateIncome(id, request)
                .<ResponseEntity<CustomResponse<IncomeResponse>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete an income source")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Income deleted"),
        @ApiResponse(responseCode = "404", description = "Income not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<Void>> deleteIncome(
            @Parameter(description = "Income ID") @PathVariable long id) {
        if (incomeService.deleteIncome(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Income deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

    @Operation(summary = "Record income receipt",
               description = "Records an income payment received into a specified account.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Income receipt recorded"),
        @ApiResponse(responseCode = "400", description = "Receipt failed"),
        @ApiResponse(responseCode = "404", description = "Income not found")
    })
    @PostMapping("/{id}/receive")
    public ResponseEntity<CustomResponse<TransactionResponse>> receiveIncome(

            @Parameter(description = "Income ID") @PathVariable long id,
            @Parameter(description = "Account ID to credit") @RequestParam long accountId,
            @Parameter(description = "Amount received") @RequestParam BigDecimal amount,
            @Parameter(description = "Optional remarks") @RequestParam(required = false) String remarks) {
        try {
            return ResponseEntity.ok(CustomResponse.success(
                    TransactionResponse.fromEntity(incomeService.receiveIncome(id, accountId, amount, remarks))));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomResponse.badRequest(e.getMessage()));
        }
    }

}
