package com.idnaheim.lifem.password;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/passwords")
@AllArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @GetMapping
    public ResponseEntity getAllPasswords() {
        return new ResponseEntity(passwordService.getAllPasswords(), HttpStatus.OK);
    }

}
