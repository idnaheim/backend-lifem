package com.idnaheim.lifem.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordRepository passwordRepository;

    @Mock
    private TextEncryptor encryptor;

    @InjectMocks
    private PasswordService passwordService;

    private PasswordEntity sampleEntity;

    @BeforeEach
    void setUp() {
        sampleEntity = new PasswordEntity();
        sampleEntity.setId(1L);
        sampleEntity.setPlatform("GitHub");
        sampleEntity.setUsername("dev@example.com");
        sampleEntity.setPassword("ENCRYPTED_VALUE");
        sampleEntity.setHasMFA(true);
        sampleEntity.setRemarks("Work account");
    }

    // -------------------------------------------------------------------------
    // getAllPasswords
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getAllPasswords")
    class GetAllPasswords {

        @Test
        @DisplayName("returns all passwords with decrypted values")
        void returnsDecrypted() {
            when(passwordRepository.findAll()).thenReturn(List.of(sampleEntity));
            when(encryptor.decrypt("ENCRYPTED_VALUE")).thenReturn("plaintext123");

            List<PasswordEntity> result = passwordService.getAllPasswords();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPassword()).isEqualTo("plaintext123");
            verify(encryptor).decrypt("ENCRYPTED_VALUE");
        }

        @Test
        @DisplayName("returns empty list when no passwords exist")
        void returnsEmpty() {
            when(passwordRepository.findAll()).thenReturn(List.of());

            assertThat(passwordService.getAllPasswords()).isEmpty();
            verifyNoInteractions(encryptor);
        }

        @Test
        @DisplayName("decrypts each password when multiple records exist")
        void decryptsAll() {
            PasswordEntity second = new PasswordEntity();
            second.setId(2L);
            second.setPassword("ENCRYPTED_2");

            when(passwordRepository.findAll()).thenReturn(List.of(sampleEntity, second));
            when(encryptor.decrypt("ENCRYPTED_VALUE")).thenReturn("pass1");
            when(encryptor.decrypt("ENCRYPTED_2")).thenReturn("pass2");

            List<PasswordEntity> result = passwordService.getAllPasswords();

            assertThat(result.get(0).getPassword()).isEqualTo("pass1");
            assertThat(result.get(1).getPassword()).isEqualTo("pass2");
            verify(encryptor, times(2)).decrypt(anyString());
        }
    }

    // -------------------------------------------------------------------------
    // getPasswordById
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("getPasswordById")
    class GetPasswordById {

        @Test
        @DisplayName("returns present Optional with decrypted password when found")
        void foundAndDecrypted() {
            when(passwordRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(encryptor.decrypt("ENCRYPTED_VALUE")).thenReturn("plaintext123");

            Optional<PasswordEntity> result = passwordService.getPasswordById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getPassword()).isEqualTo("plaintext123");
        }

        @Test
        @DisplayName("returns empty Optional when password not found")
        void notFound() {
            when(passwordRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(passwordService.getPasswordById(99L)).isEmpty();
            verifyNoInteractions(encryptor);
        }
    }

    // -------------------------------------------------------------------------
    // createPassword
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("createPassword")
    class CreatePassword {

        @Test
        @DisplayName("encrypts the password before saving")
        void encryptsBeforeSave() {
            PasswordRequest request = new PasswordRequest("GitHub", "dev@example.com", "plaintext123", true, "Work");

            when(encryptor.encrypt("plaintext123")).thenReturn("ENCRYPTED_VALUE");
            when(passwordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PasswordEntity result = passwordService.createPassword(request);

            assertThat(result.getPassword()).isEqualTo("ENCRYPTED_VALUE");
            assertThat(result.getPlatform()).isEqualTo("GitHub");
            assertThat(result.getUsername()).isEqualTo("dev@example.com");
            assertThat(result.isHasMFA()).isTrue();
            verify(encryptor).encrypt("plaintext123");
        }

        @Test
        @DisplayName("saves entity and returns the persisted result")
        void savesEntity() {
            PasswordRequest request = new PasswordRequest("Jira", "user@work.com", "secret", false, null);

            when(encryptor.encrypt("secret")).thenReturn("ENC_SECRET");
            when(passwordRepository.save(any())).thenAnswer(inv -> {
                PasswordEntity e = inv.getArgument(0);
                e.setId(5L);
                return e;
            });

            PasswordEntity result = passwordService.createPassword(request);

            assertThat(result.getId()).isEqualTo(5L);
            verify(passwordRepository).save(any(PasswordEntity.class));
        }
    }

    // -------------------------------------------------------------------------
    // updatePassword
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("updates all fields including re-encrypted password when found")
        void updatesWhenFound() {
            PasswordRequest request = new PasswordRequest("GitLab", "new@example.com", "newPass", false, "Updated");

            when(passwordRepository.findById(1L)).thenReturn(Optional.of(sampleEntity));
            when(encryptor.encrypt("newPass")).thenReturn("NEW_ENCRYPTED");
            when(passwordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Optional<PasswordEntity> result = passwordService.updatePassword(1L, request);

            assertThat(result).isPresent();
            assertThat(result.get().getPlatform()).isEqualTo("GitLab");
            assertThat(result.get().getUsername()).isEqualTo("new@example.com");
            assertThat(result.get().getPassword()).isEqualTo("NEW_ENCRYPTED");
            assertThat(result.get().isHasMFA()).isFalse();
            assertThat(result.get().getRemarks()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("returns empty Optional when password not found")
        void returnsEmptyWhenMissing() {
            PasswordRequest request = new PasswordRequest("X", "x", "x", false, null);
            when(passwordRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(passwordService.updatePassword(99L, request)).isEmpty();
            verify(passwordRepository, never()).save(any());
            verifyNoInteractions(encryptor);
        }
    }

    // -------------------------------------------------------------------------
    // deletePassword
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("deletePassword")
    class DeletePassword {

        @Test
        @DisplayName("returns true and deletes when password exists")
        void deletesExisting() {
            when(passwordRepository.existsById(1L)).thenReturn(true);

            assertThat(passwordService.deletePassword(1L)).isTrue();
            verify(passwordRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when password does not exist")
        void returnsFalseWhenMissing() {
            when(passwordRepository.existsById(99L)).thenReturn(false);

            assertThat(passwordService.deletePassword(99L)).isFalse();
            verify(passwordRepository, never()).deleteById(anyLong());
        }
    }
}
