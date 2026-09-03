package com.idnaheim.lifem.account;

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

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
@Tag(name = "Accounts", description = "Financial account management and inter-account transfers")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "List all accounts", description = "Returns all financial accounts.")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<?>> getAllAccounts() {
        return ResponseEntity.ok(CustomResponse.success(accountService.getAllAccounts()));
    }

    @Operation(summary = "Get account by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> getAccountById(
            @Parameter(description = "Account ID") @PathVariable long id) {
        return accountService.getAccountById(id)
                .<ResponseEntity<CustomResponse<?>>>map(account -> ResponseEntity.ok(CustomResponse.success(account)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Create a new account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<?>> createAccount(@RequestBody AccountEntity account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(accountService.createAccount(account)));
    }

    @Operation(summary = "Update an existing account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account updated"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> updateAccount(
            @Parameter(description = "Account ID") @PathVariable long id,
            @RequestBody AccountEntity account) {
        return accountService.updateAccount(id, account)
                .<ResponseEntity<CustomResponse<?>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete an account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account deleted"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> deleteAccount(
            @Parameter(description = "Account ID") @PathVariable long id) {
        if (accountService.deleteAccount(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Account deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

    @Operation(summary = "Transfer funds between accounts",
               description = "Moves an amount from one account to another and records debit/credit transactions.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "400", description = "Invalid transfer (e.g. insufficient balance)")
    })
    @PostMapping("/transfer")
    public ResponseEntity<CustomResponse<?>> transfer(@RequestBody TransferRequest request) {
        try {
            accountService.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
            return ResponseEntity.ok(CustomResponse.success(200, "Transfer successful", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomResponse.badRequest(e.getMessage()));
        }
    }

}
