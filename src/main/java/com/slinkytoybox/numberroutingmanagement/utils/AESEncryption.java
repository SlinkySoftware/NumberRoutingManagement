/*
 *   NumberRoutingManagement - AESEncryption.java
 *   Copyright (c) 2022-2023, Slinky Software
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   A copy of the GNU Affero General Public License is located in the 
 *   AGPL-3.0.md supplied with the source code.
 *
 */

package com.slinkytoybox.numberroutingmanagement.utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
public class AESEncryption {

    private static SecretKeySpec secretKey;
    private static IvParameterSpec ips;
    
    private AESEncryption(){}
    
    private static void setKey(String myKey) {
        final String logPrefix = "setKey() - ";
        log.trace("{}Entering method", logPrefix);
        MessageDigest sha;
        byte[] key;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            for (int i = 0; i < (key.length - 1); i++) {
                if (i % 2 == 0) {
                    key[i] = (byte) (key[i] ^ (byte) 0xC3);
                }
            }
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32);
            for (int i = 0; i < (key.length - 1); i++) {
                if (i % 3 == 0) {
                    key[i] = (byte) (key[i] ^ (byte) 0xA5);
                }
            }
            secretKey = new SecretKeySpec(key, "AES");
        }
        catch (NoSuchAlgorithmException e) {
            log.error("{}Error generating key: SHA-256 not available: ", logPrefix, e);
        }
        log.trace("{}Leaving method", logPrefix);

    }

    private static void setInitValue(String iv) {
        final String logPrefix = "setInitValue() - ";
        log.trace("{}Entering method", logPrefix);

        byte[] ivb = Arrays.copyOf(iv.getBytes(), 16);
        try {
            IvParameterSpec ps = new IvParameterSpec(ivb);
            ips = ps;
        }
        catch (Exception e) {
            log.error("{}Error generating parameter spec: ", logPrefix, e);
            ips = null;
        }
        log.trace("{}Leaving method", logPrefix);

    }

    public static String encrypt(String strToEncrypt, String secret, String initValue) {
        final String logPrefix = "encrypt() - ";
        log.trace("{}Entering method", logPrefix);

        try {
            setKey(secret);
            setInitValue(initValue);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ips);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        }
        catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            log.error("{}Error while encrypting: ", logPrefix, e);
        }
        return null;
    }

    public static String decrypt(String strToDecrypt, String secret, String initValue) {
        final String logPrefix = "decrypt() - ";
        log.trace("{}Entering method", logPrefix);

        try {
            setKey(secret);
            setInitValue(initValue);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ips);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        }
        catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            log.error("{}Error while decrypting: ", logPrefix, e);
        }
        return null;
    }
}
