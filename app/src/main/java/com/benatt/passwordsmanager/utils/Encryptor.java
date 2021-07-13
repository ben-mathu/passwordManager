package com.benatt.passwordsmanager.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import static com.benatt.passwordsmanager.utils.Constants.DELIMITER;

/**
 * @author bernard
 */
public class Encryptor {

    public static final int TAG_LENGTH_BYTES = 12;

    public static String encrypt(SecretKey secretKey, String plainText) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();
        String encodediv = Base64.encodeToString(iv, Base64.DEFAULT);
//        MainApp.getPreferences().edit()
//                .putString(INITIALIZATION_VECTOR, encodediv)
//                .apply();
        return encodediv + DELIMITER + Base64.encodeToString(
                cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)),
                Base64.DEFAULT);
    }

    public static AlgorithmParameterSpec getParameter(byte[] iv) {
        return getParams(iv, 0, iv.length);
    }

    public static AlgorithmParameterSpec getParams(byte[] buff, int offset, int length) {
        return new GCMParameterSpec(TAG_LENGTH_BYTES * 8, buff);
    }
}
