package com.idnaheim.lifem.password;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PasswordRequest(
        String platform,
        String username,
        String password,
        @JsonProperty("hasMFA") boolean hasMFA,
        String remarks
) {
    public PasswordEntity toEntity() {
        PasswordEntity entity = new PasswordEntity();
        entity.setPlatform(platform);
        entity.setUsername(username);
        entity.setPassword(password);
        entity.setHasMFA(hasMFA);
        entity.setRemarks(remarks);
        return entity;
    }
}
