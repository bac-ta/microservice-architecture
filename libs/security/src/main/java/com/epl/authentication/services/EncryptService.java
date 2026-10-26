package com.epl.authentication.services;

public interface EncryptService {

    String encrypt(String rawValue);

    boolean matches(String rawValue, String encryptedValue);
}
