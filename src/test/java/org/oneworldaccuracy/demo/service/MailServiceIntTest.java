package org.oneworldaccuracy.demo.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.oneworldaccuracy.demo.DemoApplication;
import org.oneworldaccuracy.demo.config.ApplicationProperties;
import org.oneworldaccuracy.demo.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
public class MailServiceIntTest {

    @Autowired
    private ApplicationProperties properties;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @MockBean
    private SendGrid sendGrid;

    private MailService mailService;

    @Captor
    private ArgumentCaptor<Request> messageCaptor;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
        doReturn(null).when(sendGrid).api(any(Request.class));
        mailService = new MailService(templateEngine, sendGrid, properties);
    }

    @Test
    public void shouldSendPlainTextEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false);
        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("testSubject");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.content[0].value", String.class)).isEqualTo("testContent");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/plain");
    }

    @Test
    public void shouldSendHtmlEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", true);
        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("testSubject");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/html");
        assertThat(ctx.read("$.content[0].value", String.class)).isEqualTo("testContent");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");
    }

    @Test
    public void shouldSendEmailFromTemplate() throws Exception {
        User user = new User();
        user.setTitle("Mr");
        user.setFirstName("John");
        user.setEmail("john.doe@example.com");
        mailService.sendEmailFromTemplate(user, "mail/testEmail", "testSubject");
        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("testSubject");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/html");
        assertThat(ctx.read("$.content[0].value", String.class)).isEqualTo("<html>http://localhost:8080/api, Mr John</html>\n");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");

    }

    @Test
    public void testSendActivationEmail() throws Exception {
        User user = new User();
        user.setTitle("Mr");
        user.setFirstName("John");
        user.setEmail("john.doe@example.com");
        user.setActivationKey("activation-key");
        mailService.sendVerifyEmail(user);
        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("Verify Email");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/html");

        String emailBody = ctx.read("$.content[0].value", String.class);
        assertThat(emailBody).contains("<title>1WA Verification</title>");
        assertThat(emailBody).contains("Dear Mr John,");
        assertThat(emailBody).contains("Your 1WA account has been created, please click on the URL below to activate it:");
        assertThat(emailBody).contains("<a href=\"http://localhost:8080/api/api/user/activate/activation-key\">http://localhost:8080/api/api/user/activate/activation-key</a>");

    }

    @Test
    public void testSendOnboardingEmail() throws Exception {
        User user = new User();
        user.setTitle("Mr");
        user.setFirstName("John");
        user.setEmail("john.doe@example.com");
        mailService.sendOnboardingEmail(user);
        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("Onboarding");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/html");

        String emailBody = ctx.read("$.content[0].value", String.class);
        assertThat(emailBody).contains("<title>1WA Onboarding</title>");
        assertThat(emailBody).contains("Dear Mr John,");
        assertThat(emailBody).contains("This is an onboarding email");
    }

    @Test
    public void shouldSendDeactivatedEmail() throws Exception {
        User user = new User();
        user.setTitle("Mr");
        user.setFirstName("John");
        user.setEmail("john.doe@example.com");
        user.setActivationKey("activation-key");

        mailService.sendDeactivationEmail(user);

        verify(sendGrid).api(messageCaptor.capture());
        Request message = messageCaptor.getValue();
        ReadContext ctx = JsonPath.parse(message.getBody());
        assertThat(ctx.read("$.from.email", String.class)).isEqualTo("test-challenge@1wa.org");
        assertThat(ctx.read("$.reply_to.email", String.class)).isEqualTo("test-reply-to-challenge@1wa.org");
        assertThat(ctx.read("$.personalizations[0].to[0].email", String.class)).isEqualTo("john.doe@example.com");
        assertThat(ctx.read("$.subject", String.class)).isEqualTo("Account Deactivated");
        assertThat(ctx.read("$.content[0].type", String.class)).isEqualTo("text/html");

        String emailBody = ctx.read("$.content[0].value", String.class);
        assertThat(emailBody).contains("<title>Account Deactivated</title>");
        assertThat(emailBody).contains("Dear Mr John,");
        assertThat(emailBody).contains("Your 1WA account has been deactivated.");
    }

    @Test
    public void testSendEmailFailsWithException() throws Exception {
        doThrow(IOException.class).when(sendGrid).api(any(Request.class));
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false);
    }
}
