package com.idnaheim.lifem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorAwareConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        // Returns "SYSTEM" as the default auditor.
        // Replace with SecurityContext logic if you add authentication later.
        return () -> Optional.of("SYSTEM");
    }

}
