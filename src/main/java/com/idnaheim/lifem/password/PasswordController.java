package com.idnaheim.lifem.password;

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
@RequestMapping("/passwords")
@AllArgsConstructor
@Tag(name = "Passwords", description = "Encrypted credential vault — stored passwords are AES-encrypted at rest")
public class PasswordController {

    private final PasswordService passwordService;

    @Operation(summary = "List all vault entries",
               description = "Returns all password vault entries. Passwords are decrypted before being returned.")
    @ApiResponse(responseCode = "200", description = "Vault entries retrieved successfully")
    @GetMapping
    public ResponseEntity<CustomResponse<?>> getAllPasswords() {
        return ResponseEntity.ok(CustomResponse.success(passwordService.getAllPasswords()));
    }

    @Operation(summary = "Get vault entry by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry found"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> getPasswordById(
            @Parameter(description = "Password entry ID") @PathVariable long id) {
        return passwordService.getPasswordById(id)
                .<ResponseEntity<CustomResponse<?>>>map(password -> ResponseEntity.ok(CustomResponse.success(password)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Create a new vault entry",
               description = "Stores a new credential. The password field is encrypted before persistence.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Entry created"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping
    public ResponseEntity<CustomResponse<?>> createPassword(@RequestBody PasswordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomResponse.created(passwordService.createPassword(request)));
    }

    @Operation(summary = "Update a vault entry")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry updated"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> updatePassword(
            @Parameter(description = "Password entry ID") @PathVariable long id,
            @RequestBody PasswordRequest request) {
        return passwordService.updatePassword(id, request)
                .<ResponseEntity<CustomResponse<?>>>map(updated -> ResponseEntity.ok(CustomResponse.success(updated)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound()));
    }

    @Operation(summary = "Delete a vault entry")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Entry deleted"),
        @ApiResponse(responseCode = "404", description = "Entry not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<CustomResponse<?>> deletePassword(
            @Parameter(description = "Password entry ID") @PathVariable long id) {
        if (passwordService.deletePassword(id)) {
            return ResponseEntity.ok(CustomResponse.success(204, "Password deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomResponse.notFound());
    }

}
