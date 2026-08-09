package com.idnaheim.lifem.password;

import com.idnaheim.lifem.utilities.ApiResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/passwords")
@AllArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllPasswords() {
        return ResponseEntity.ok(ApiResponse.success(passwordService.getAllPasswords()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getPasswordById(@PathVariable long id) {
        return passwordService.getPasswordById(id)
                .<ResponseEntity<ApiResponse<?>>>map(password -> ResponseEntity.ok(ApiResponse.success(password)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createPassword(@RequestBody PasswordEntity password) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(passwordService.createPassword(password)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updatePassword(@PathVariable long id, @RequestBody PasswordEntity password) {
        return passwordService.updatePassword(id, password)
                .<ResponseEntity<ApiResponse<?>>>map(updated -> ResponseEntity.ok(ApiResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePassword(@PathVariable long id) {
        if (passwordService.deletePassword(id)) {
            return ResponseEntity.ok(ApiResponse.success(204, "Password deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound());
    }

}
