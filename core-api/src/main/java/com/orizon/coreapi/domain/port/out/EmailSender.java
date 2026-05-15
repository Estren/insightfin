package com.orizon.coreapi.domain.port.out;

public interface EmailSender {
    void sendPasswordResetEmail(String to, String recipientName, String resetLink);
}
