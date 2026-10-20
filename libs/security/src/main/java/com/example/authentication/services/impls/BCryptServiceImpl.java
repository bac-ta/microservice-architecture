package com.example.authentication.services.impls;

import com.example.authentication.services.EncryptService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service(value = "bCryptService")
public class BCryptServiceImpl implements EncryptService {

    private final PasswordEncoder encoder;

    public BCryptServiceImpl() {
        this(new BCryptPasswordEncoder());
    }

    public BCryptServiceImpl(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override public String encrypt(String rawValue) {
        return encoder.encode(rawValue);
    }

    @Override public boolean matches(String rawValue, String encryptedValue) {
        return encoder.matches(rawValue, encryptedValue);
    }
}
