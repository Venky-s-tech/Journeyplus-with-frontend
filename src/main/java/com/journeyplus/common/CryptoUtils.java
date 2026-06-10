package com.journeyplus.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtils {

    private static SecretKeySpec secretKey;

    @Value("${app.encryption.secret-key:JourneyPlusSuperSecretKey32Bytes!}")
    public void setSecretKey(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            // Adjust to exactly 32 bytes for AES-256 if misconfigured
            byte[] adjustedBytes = new byte[32];
            System.arraycopy(keyBytes, 0, adjustedBytes, 0, Math.min(keyBytes.length, 32));
            secretKey = new SecretKeySpec(adjustedBytes, "AES");
        } else {
            secretKey = new SecretKeySpec(keyBytes, "AES");
        }
    }

    public static String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error during AES encryption: " + e.getMessage(), e);
        }
    }

    public static String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error during AES decryption: " + e.getMessage(), e);
        }
    }
}
