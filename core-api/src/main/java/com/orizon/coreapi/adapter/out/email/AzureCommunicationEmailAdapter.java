package com.orizon.coreapi.adapter.out.email;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import com.orizon.coreapi.domain.port.out.EmailSender;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class AzureCommunicationEmailAdapter implements EmailSender {

    private static final Logger LOG = Logger.getLogger(AzureCommunicationEmailAdapter.class);

    @ConfigProperty(name = "acs.email.connection-string")
    Optional<String> connectionString;

    @ConfigProperty(name = "acs.email.sender")
    Optional<String> senderAddress;

    @ConfigProperty(name = "acs.email.enabled", defaultValue = "false")
    boolean enabled;

    private EmailClient emailClient;

    @PostConstruct
    void init() {
        if (!enabled) {
            LOG.info("ACS email disabled — password reset emails will be logged only.");
            return;
        }
        if (connectionString.isEmpty() || senderAddress.isEmpty()) {
            LOG.warn("ACS email enabled but connection-string or sender is missing — falling back to log only.");
            enabled = false;
            return;
        }
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString.get())
                .buildClient();
    }

    @Override
    public void sendPasswordResetEmail(String to, String recipientName, String resetLink) {
        if (!enabled || emailClient == null) {
            LOG.infof("[email-dev] Password reset link for %s: %s", to, resetLink);
            return;
        }

        String safeName = recipientName == null || recipientName.isBlank() ? "there" : recipientName;
        String subject = "Reset your InsightFin password";
        String html = buildHtml(safeName, resetLink);
        String plain = buildPlainText(safeName, resetLink);

        CompletableFuture.runAsync(() -> {
            try {
                EmailMessage message = new EmailMessage()
                        .setSenderAddress(senderAddress.get())
                        .setToRecipients(to)
                        .setSubject(subject)
                        .setBodyHtml(html)
                        .setBodyPlainText(plain);

                SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
                EmailSendResult result = poller.waitForCompletion().getValue();
                LOG.infof("ACS email sent to %s, messageId=%s, status=%s",
                        to, result.getId(), result.getStatus());
            } catch (Exception ex) {
                LOG.errorf(ex, "Failed to send password reset email to %s", to);
            }
        });
    }

    private String buildHtml(String name, String link) {
        return """
                <!doctype html>
                <html>
                  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color:#f6f8fb; padding:32px;">
                    <table align="center" width="480" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; padding:32px;">
                      <tr><td>
                        <h2 style="color:#1f2937; margin-top:0;">Reset your password</h2>
                        <p style="color:#374151; line-height:1.5;">Hi %s,</p>
                        <p style="color:#374151; line-height:1.5;">We received a request to reset your InsightFin password. Click the button below to choose a new one. This link expires in 30 minutes.</p>
                        <p style="text-align:center; margin:32px 0;">
                          <a href="%s" style="background:#2490FF; color:#ffffff; padding:12px 24px; border-radius:8px; text-decoration:none; font-weight:600; display:inline-block;">Reset password</a>
                        </p>
                        <p style="color:#6b7280; font-size:13px; line-height:1.5;">If you didn't ask for this, you can safely ignore this email — your password won't change.</p>
                        <p style="color:#6b7280; font-size:13px; line-height:1.5;">If the button doesn't work, copy and paste this link into your browser:<br><a href="%s" style="color:#2490FF; word-break:break-all;">%s</a></p>
                      </td></tr>
                    </table>
                  </body>
                </html>
                """.formatted(name, link, link, link);
    }

    private String buildPlainText(String name, String link) {
        return """
                Hi %s,

                We received a request to reset your InsightFin password. Use the link below to set a new one. This link expires in 30 minutes.

                %s

                If you didn't request this, you can safely ignore this email.
                """.formatted(name, link);
    }

    @Override
    public void sendEmailVerificationEmail(String to, String recipientName, String verifyLink, String pin) {
        if (!enabled || emailClient == null) {
            LOG.infof("[email-dev] Email verification link for %s: %s | PIN: %s", to, verifyLink, pin);
            return;
        }

        String safeName = recipientName == null || recipientName.isBlank() ? "there" : recipientName;
        String subject = "Confirm your email — InsightFin";
        String html = buildVerificationHtml(safeName, verifyLink, pin);
        String plain = buildVerificationPlainText(safeName, verifyLink, pin);

        CompletableFuture.runAsync(() -> {
            try {
                EmailMessage message = new EmailMessage()
                        .setSenderAddress(senderAddress.get())
                        .setToRecipients(to)
                        .setSubject(subject)
                        .setBodyHtml(html)
                        .setBodyPlainText(plain);

                SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
                EmailSendResult result = poller.waitForCompletion().getValue();
                LOG.infof("ACS verification email sent to %s, messageId=%s, status=%s",
                        to, result.getId(), result.getStatus());
            } catch (Exception ex) {
                LOG.errorf(ex, "Failed to send verification email to %s", to);
            }
        });
    }

    private String buildVerificationHtml(String name, String link, String pin) {
        return """
                <!doctype html>
                <html>
                  <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color:#f6f8fb; padding:32px;">
                    <table align="center" width="480" cellpadding="0" cellspacing="0" style="background:#ffffff; border-radius:12px; padding:32px;">
                      <tr><td>
                        <h2 style="color:#1f2937; margin-top:0;">Welcome to InsightFin</h2>
                        <p style="color:#374151; line-height:1.5;">Hi %s,</p>
                        <p style="color:#374151; line-height:1.5;">Confirm your email to finish your registration and unlock all features. This link and code expire in 24 hours.</p>
                        <p style="text-align:center; margin:32px 0;">
                          <a href="%s" style="background:#2490FF; color:#ffffff; padding:12px 24px; border-radius:8px; text-decoration:none; font-weight:600; display:inline-block;">Confirm email</a>
                        </p>
                        <p style="color:#374151; line-height:1.5; text-align:center;">Or enter this code in the app:</p>
                        <p style="text-align:center; margin:8px 0 32px 0;">
                          <span style="display:inline-block; background:#f3f4f6; padding:14px 24px; border-radius:8px; font-family:'SF Mono', Consolas, monospace; font-size:24px; letter-spacing:8px; color:#1f2937; font-weight:600;">%s</span>
                        </p>
                        <p style="color:#6b7280; font-size:13px; line-height:1.5;">If you didn't sign up, you can safely ignore this email.</p>
                      </td></tr>
                    </table>
                  </body>
                </html>
                """.formatted(name, link, pin);
    }

    private String buildVerificationPlainText(String name, String link, String pin) {
        return """
                Hi %s,

                Welcome to InsightFin! Confirm your email to finish your registration.

                Click the link below:
                %s

                Or enter this code in the app: %s

                This link and code expire in 24 hours.

                If you didn't sign up, you can safely ignore this email.
                """.formatted(name, link, pin);
    }
}
