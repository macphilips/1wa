package org.oneworldaccuracy.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private Mail mail = new Mail();
    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }
    public static class Mail {
        private String sendGridKey = "apiKey";
        private String from = "test-challenge@1wa.org";
        private String replyTo = "test-challenge@1wa.org";
        private String baseUrl = "http://localhost:8080/api";
        private boolean enabled = false;

        public String getSendGridKey() {
            return sendGridKey;
        }

        public void setSendGridKey(String sendGridKey) {
            this.sendGridKey = sendGridKey;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getReplyTo() {
            return replyTo;
        }

        public void setReplyTo(String replyTo) {
            this.replyTo = replyTo;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }
}
