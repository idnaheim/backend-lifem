package com.idnaheim.lifem.password;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PasswordService {

    private final PasswordRepository passwordRepository;

    public List<PasswordEntity> getAllPasswords() {
        return passwordRepository.findAll();
    }
}
