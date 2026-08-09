package com.idnaheim.lifem.password;

import java.time.LocalDateTime;

public record PasswordResponse(
    long id,
    String platform,
    String username,
    boolean hasMFA,
    String remarks,
    String createdBy,
    LocalDateTime createdDate,
    String modifiedBy,
    LocalDateTime modifiedDate
) {
    public static PasswordResponse fromEntity(PasswordEntity entity) {
        return new PasswordResponse(
            entity.getId(),
            entity.getPlatform(),
            entity.getUsername(),
            entity.isHasMFA(),
            entity.getRemarks(),
            entity.getCreatedBy(),
            entity.getCreatedDate(),
            entity.getModifiedBy(),
            entity.getModifiedDate()
        );
    }
}
