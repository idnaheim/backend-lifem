package com.idnaheim.lifem.password;

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
    public ResponseEntity getAllPasswords() {
        return new ResponseEntity(passwordService.getAllPasswords(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity getPasswordById(@PathVariable long id) {
        return passwordService.getPasswordById(id)
                .map(password -> new ResponseEntity(password, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity createPassword(@RequestBody PasswordEntity password) {
        return new ResponseEntity(passwordService.createPassword(password), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity updatePassword(@PathVariable long id, @RequestBody PasswordEntity password) {
        return passwordService.updatePassword(id, password)
                .map(updated -> new ResponseEntity(updated, HttpStatus.OK))
                .orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deletePassword(@PathVariable long id) {
        if (passwordService.deletePassword(id)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

}
