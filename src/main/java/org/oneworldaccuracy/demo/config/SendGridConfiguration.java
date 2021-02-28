package org.oneworldaccuracy.demo.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SendGridConfiguration {

    private final ApplicationProperties properties;
    private final Environment env;

    @Autowired
    public SendGridConfiguration(ApplicationProperties properties, Environment env) {
        this.properties = properties;
        this.env = env;
    }

    @Bean
    SendGrid sendGrid() {
        boolean isTest = Arrays.asList(env.getActiveProfiles()).contains("test");
        return new SendGrid(properties.getMail().getSendGridKey(), isTest);
    }
}
