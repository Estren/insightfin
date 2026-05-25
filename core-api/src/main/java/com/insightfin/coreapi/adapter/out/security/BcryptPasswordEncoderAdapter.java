package com.insightfin.coreapi.adapter.out.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.insightfin.coreapi.domain.port.out.PasswordEncoder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {

    @Override
    public String encode(String rawPassword) {
        return BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified;
    }
}
