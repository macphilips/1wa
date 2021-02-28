package org.oneworldaccuracy.demo.config;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfiguration {

    private final ApplicationProperties properties;

    @Autowired
    public SendGridConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(properties.getMail().getSendGridKey());
    }
}
