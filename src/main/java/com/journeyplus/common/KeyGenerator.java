package com.journeyplus.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyGenerator {

    public static void main(String[] args) {
        try {
            System.out.println("Generating RSA 2048-bit key pair...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair pair = keyGen.generateKeyPair();

            // Create keys directory in resources
            File keysDir = new File("src/main/resources/keys");
            if (!keysDir.exists()) {
                boolean created = keysDir.mkdirs();
                if (created) {
                    System.out.println("Created directory: " + keysDir.getAbsolutePath());
                }
            }

            // Write Private Key (PKCS#8 PEM format)
            File privateKeyFile = new File(keysDir, "private.pem");
            String privateKeyPem = convertToPem(pair.getPrivate().getEncoded(), "PRIVATE KEY");
            try (FileWriter writer = new FileWriter(privateKeyFile, StandardCharsets.UTF_8)) {
                writer.write(privateKeyPem);
            }
            System.out.println("Private key written to: " + privateKeyFile.getAbsolutePath());

            // Write Public Key (X.509 PEM format)
            File publicKeyFile = new File(keysDir, "public.pem");
            String publicKeyPem = convertToPem(pair.getPublic().getEncoded(), "PUBLIC KEY");
            try (FileWriter writer = new FileWriter(publicKeyFile, StandardCharsets.UTF_8)) {
                writer.write(publicKeyPem);
            }
            System.out.println("Public key written to: " + publicKeyFile.getAbsolutePath());

            System.out.println("Key pair generation completed successfully.");

        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("Error generating keys: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String convertToPem(byte[] encodedKey, String keyType) {
        String base64 = Base64.getEncoder().encodeToString(encodedKey);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN ").append(keyType).append("-----\n");
        
        // Wrap at 64 characters
        int index = 0;
        while (index < base64.length()) {
            int end = Math.min(index + 64, base64.length());
            pem.append(base64, index, end).append("\n");
            index = end;
        }
        
        pem.append("-----END ").append(keyType).append("-----\n");
        return pem.toString();
    }
}
