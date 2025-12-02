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

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import io.github.cdimascio.dotenv.Dotenv;


/**
 * EncryptKey class used to encrypt the keys before storing in cloud.
 */
public final class EncryptKeys{
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
        String keyString = dotenv.get("SECRET_KEY");
        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Generates an IV.
     * @return IV
     */
    private static byte[] generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Method used to encrypt the keys
     * @param key key
     * @return encrypted key
     * @throws Exception
     */
    public static String encrypt(String key) throws Exception {
        byte[] iv = generateIv();
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY, new GCMParameterSpec(128, iv));
        byte[] encryptedBytes = cipher.doFinal(key.getBytes());
        ByteBuffer buffer = ByteBuffer.allocate(iv.length + encryptedBytes.length);
        buffer.put(iv);
        buffer.put(encryptedBytes);
        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Method used to decrypt the keys
     * @param key key
     * @return decrypted key
     * @throws Exception
     */
    public static String decrypt (String key) throws Exception {
        byte[] base = Base64.getDecoder().decode(key);
        ByteBuffer buffer = ByteBuffer.wrap(base);

        byte[] iv = new byte[12];
        buffer.get(iv);

        byte[] keyCipherText = new byte[buffer.remaining()];
        buffer.get(keyCipherText);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY, new GCMParameterSpec(128, iv));
        return new String(cipher.doFinal(keyCipherText));
    }
}