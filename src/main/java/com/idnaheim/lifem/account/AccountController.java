package com.idnaheim.lifem.account;

import com.idnaheim.lifem.utilities.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllAccounts() {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAllAccounts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getAccountById(@PathVariable long id) {
        return accountService.getAccountById(id)
                .<ResponseEntity<ApiResponse<?>>>map(account -> ResponseEntity.ok(ApiResponse.success(account)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAccount(@RequestBody AccountEntity account) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(accountService.createAccount(account)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateAccount(@PathVariable long id, @RequestBody AccountEntity account) {
        return accountService.updateAccount(id, account)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteAccount(@PathVariable long id) {
        if (accountService.deleteAccount(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Account deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<?>> transfer(@RequestBody TransferRequest request) {
        try {
            accountService.transfer(request.fromAccountId(), request.toAccountId(), request.amount());
            return ResponseEntity.ok(ApiResponse.success(200, "Transfer successful", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(e.getMessage()));
        }
    }

}
