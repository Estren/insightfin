package com.insightfin.coreapi.adapter.out.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.insightfin.coreapi.domain.exception.DomainException;
import com.insightfin.coreapi.domain.port.out.GoogleTokenVerifier;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@ApplicationScoped
public class GoogleTokenVerifierAdapter implements GoogleTokenVerifier {

    @ConfigProperty(name = "google.client-id")
    Optional<String> clientId;

    private volatile GoogleIdTokenVerifier verifier;

    @Override
    public GoogleUserInfo verify(String idToken, String expectedNonce) {
        String configuredClientId = clientId.filter(id -> !id.isBlank()).orElse(null);
        if (configuredClientId == null) {
            throw new DomainException("Google Sign-In is not configured");
        }
        if (expectedNonce == null || expectedNonce.isBlank()) {
            throw new DomainException("Missing nonce");
        }
        try {
            GoogleIdToken token = verifier(configuredClientId).verify(idToken);
            if (token == null) {
                throw new DomainException("Invalid Google token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            Object tokenNonce = payload.get("nonce");
            if (tokenNonce == null || !expectedNonce.equals(tokenNonce.toString())) {
                throw new DomainException("Nonce mismatch");
            }
            Boolean emailVerified = payload.getEmailVerified();
            if (emailVerified == null || !emailVerified) {
                throw new DomainException("Google account email is not verified");
            }
            String name = (String) payload.get("name");
            return new GoogleUserInfo(payload.getSubject(), payload.getEmail(), true, name);
        } catch (GeneralSecurityException | IOException e) {
            throw new DomainException("Failed to verify Google token");
        }
    }

    private GoogleIdTokenVerifier verifier(String configuredClientId) {
        GoogleIdTokenVerifier local = verifier;
        if (local == null) {
            synchronized (this) {
                local = verifier;
                if (local == null) {
                    local = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                            .setAudience(Collections.singletonList(configuredClientId))
                            .build();
                    verifier = local;
                }
            }
        }
        return local;
    }
}
