package org.oneworldaccuracy.demo.config;

import org.oneworldaccuracy.demo.service.MailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@Configuration
public class NoOpMailConfiguration {
    private final MailService mockMailService;

    public NoOpMailConfiguration() {
        mockMailService = mock(MailService.class);
        doNothing().when(mockMailService).sendDeactivationEmail(any());
        doNothing().when(mockMailService).sendVerifyEmail(any());
    }

    @Bean
    @Primary
    public MailService mockMailService() {
        return mockMailService;
    }
}
