package com.idnaheim.lifem.account;

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
    public ResponseEntity getAllAccounts() {
        return new ResponseEntity(accountService.getAllAccounts(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getAccountById(@PathVariable long id) {
        return accountService.getAccountById(id)
                .map(account -> new ResponseEntity(account, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity createAccount(@RequestBody AccountEntity account) {
        return new ResponseEntity(accountService.createAccount(account), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateAccount(@PathVariable long id, @RequestBody AccountEntity account) {
        return accountService.updateAccount(id, account)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteAccount(@PathVariable long id) {
        if (accountService.deleteAccount(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/transfer")
    public ResponseEntity transfer(@RequestBody TransferRequest request) {
        try {
            accountService.transfer(request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            return new ResponseEntity("Transfer successful", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
