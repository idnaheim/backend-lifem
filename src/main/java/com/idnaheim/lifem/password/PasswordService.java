package com.idnaheim.lifem.password;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PasswordService {

    private final PasswordRepository passwordRepository;
    private final TextEncryptor encryptor;

    public PasswordService(PasswordRepository passwordRepository, TextEncryptor encryptor) {
        this.passwordRepository = passwordRepository;
        this.encryptor = encryptor;
    }

    public List<PasswordEntity> getAllPasswords() {
        List<PasswordEntity> passwords = passwordRepository.findAll();
        passwords.forEach(p -> p.setPassword(encryptor.decrypt(p.getPassword())));
        return passwords;
    }

    public Optional<PasswordEntity> getPasswordById(long id) {
        return passwordRepository.findById(id).map(p -> {
            p.setPassword(encryptor.decrypt(p.getPassword()));
            return p;
        });
    }

    public PasswordEntity createPassword(PasswordRequest request) {
        PasswordEntity entity = request.toEntity();
        entity.setPassword(encryptor.encrypt(request.password()));
        return passwordRepository.save(entity);
    }

    public Optional<PasswordEntity> updatePassword(long id, PasswordRequest request) {
        return passwordRepository.findById(id).map(existing -> {
            existing.setPlatform(request.platform());
            existing.setUsername(request.username());
            existing.setPassword(encryptor.encrypt(request.password()));
            existing.setHasMFA(request.hasMFA());
            existing.setRemarks(request.remarks());
            return passwordRepository.save(existing);
        });
    }

    public boolean deletePassword(long id) {
        if (passwordRepository.existsById(id)) {
            passwordRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
