/*
 * -----------------------------------------------------------------------------
 *  File: EncryptKeys.java
 *  Owner: Nandhana Sunil
 *  Roll Number : 112201008
 *  Module : com.swe.aiinsights.getkeys
 * -----------------------------------------------------------------------------
 */

/**
 * <p>
 *     Used to encrypt keys before storing in cloud.
 *     References:
 *      1. https://www.geeksforgeeks.org/java/
 *          symmetric-encryption-cryptography-in-java/
 *      2. https://www.baeldung.com/java-aes-encryption-decryption
 * </p>
 * @author : Nandhana Sunil
 */

package com.swe.aiinsights.getkeys;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;


/**
 * EncryptKey class used to encrypt the keys before storing in cloud.
 */
public final class EncryptKeys {
    /**
     * Loads environment variables from the .env file.
     */
    private static Dotenv dotenv = Dotenv.load();

    /**
     * gets the SECRET_KEY from the .env file.
     */
    private static final SecretKey SECRET_KEY = loadKey();

    /**
     * Change the key type from string to base64 encoding.
     * @return SecretKey
     */
    private static SecretKey loadKey() {
        final String keyString = dotenv.get("SECRET_KEY");
        final byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generates an IV.
     * @return IV
     */
    private static byte[] generateIv() {
        final int numBytes = 12;
        final byte[] iv = new byte[numBytes];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Method used to encrypt the keys.
     * @param key key
     * @return encrypted key is returned.
     * @throws Exception when the algorithm is invalid.
     */
    public static String encrypt(final String key) throws Exception {
        final byte[] iv = generateIv();
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final int paramLen = 128;
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(paramLen, iv));
        final byte[] encryptedBytes = cipher.doFinal(key.getBytes());
        final ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        buffer.put(iv);
        buffer.put(encryptedBytes);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Method used to decrypt the keys.
     * @param key key
     * @return decrypted key is returned.
     * @throws Exception when algorithm is invalid.
     */
    public static String decrypt(final String key) throws Exception {
        final byte[] base = Base64.getDecoder().decode(key);
        final ByteBuffer buffer = ByteBuffer.wrap(base);

        final int numBytes = 12;
        final byte[] iv = new byte[numBytes];
        buffer.get(iv);

        final byte[] keyCipherText = new byte[buffer.remaining()];
        buffer.get(keyCipherText);

        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final int bytesLen = 128;
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(bytesLen, iv));
        return new String(cipher.doFinal(keyCipherText));
    }
}