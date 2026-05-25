package com.insightfin.coreapi.domain.port.out;

public interface EmailSender {
    void sendPasswordResetEmail(String to, String recipientName, String resetLink);
    void sendEmailVerificationEmail(String to, String recipientName, String verifyLink, String pin);
    void sendEmailChangeConfirmation(String toNewEmail, String recipientName, String confirmLink, String pin);
    void sendEmailChangeNotice(String toOldEmail, String recipientName, String newEmail);
}
