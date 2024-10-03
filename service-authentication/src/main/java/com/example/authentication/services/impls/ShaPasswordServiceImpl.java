package com.example.authentication.services.impls;

import com.example.authentication.services.EncryptService;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service(value = "shaPasswordService")
public class ShaPasswordServiceImpl implements EncryptService {

    // Static variables
    // ------------------------------------------------------------------------
    private static final String ALGORITHM = "SHA-512";
    private static final String SALT = "demo";

    // Override methods
    // ------------------------------------------------------------------------
    @Override
    public String encrypt(String rawValue) {

        var messageDigest = getMessageDigest();
        messageDigest.update(Utf8.encode(rawValue));
        var bytes = messageDigest.digest(Utf8.encode(SALT));

        return convertToHex(bytes);
    }

    @Override
    public boolean matches(String rawValue, String encryptedValue) {

        if (rawValue == null || encryptedValue == null) {
            return false;
        }

        return encryptedValue.equals(encrypt(rawValue));
    }

    // Private methods
    // ------------------------------------------------------------------------

    /**
     * Get a MessageDigest instance for the given algorithm. Throws an IllegalArgumentException if
     * <i>algorithm</i> is unknown
     *
     * @return MessageDigest instance
     * @throws IllegalArgumentException if NoSuchAlgorithmException is thrown
     */
    private MessageDigest getMessageDigest() throws IllegalArgumentException {
        try {
            return MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm [" + ALGORITHM + "]");
        }
    }

    /**
     * Converts the given byte[] to a hex string.
     *
     * @param raw the byte[] to convert
     * @return the string the given byte[] represents
     */
    private String convertToHex(byte[] raw) {
        var sb = new StringBuilder();
        for (var b : raw) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
