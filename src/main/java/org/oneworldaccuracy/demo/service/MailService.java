package org.oneworldaccuracy.demo.service;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import org.oneworldaccuracy.demo.config.ApplicationProperties;
import org.oneworldaccuracy.demo.domain.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;
import java.util.Locale;

/**
 * Service for sending emails.
 * <p>
 * We use the @Async annotation to send emails asynchronously.
 */
@Service
@Slf4j
public class MailService {
    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";

    private final SpringTemplateEngine templateEngine;

    private final SendGrid sendGrid;

    private final ApplicationProperties applicationProperties;

    public MailService(SpringTemplateEngine templateEngine, SendGrid sendGrid, ApplicationProperties applicationProperties) {
        this.templateEngine = templateEngine;
        this.sendGrid = sendGrid;
        this.applicationProperties = applicationProperties;
    }

    @Async
    public void sendEmail(String to, String subject, String content, boolean isHtml) {
        log.debug("Send email[html '{}'] to '{}' with subject '{}' and content={}", isHtml, to, subject, content);

        sendGridMail(to, subject, content, isHtml);
        log.debug("Sent email to User '{}'", to);
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String subject) {
        Locale locale = Locale.forLanguageTag("en");
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, applicationProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        sendGridMail(user.getEmail(), subject, content, true);
    }

    @Async
    public void sendDeactivationEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/deactivateUser", "Account Deactivated");
    }

    @Async
    public void sendOnboardingEmail(User user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/onBoardingEmail", "Onboarding");
    }

    @Async
    public void sendVerifyEmail(User user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/verifyEmail", "Verify Email");
    }

    private void sendGridMail(String to, String subject, String body, boolean isHtml) {
        if (!applicationProperties.getMail().isEnabled()){
            log.warn("SendGrid is not enabled");
            return;
        }
        try {
            String contentType = (isHtml) ? "text/html" : "text/plain";

            String from = applicationProperties.getMail().getFrom();
            String replyTo = applicationProperties.getMail().getReplyTo();

            Mail mail = new Mail(new Email(from), subject, new Email(to), new Content(contentType, body));
            mail.setReplyTo(new Email(replyTo));
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            this.sendGrid.api(request);
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }
}
