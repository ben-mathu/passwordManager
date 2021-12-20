
package com.benatt.passwordsmanager.utils;

import android.util.Base64;
import android.util.Log;

import com.benatt.passwordsmanager.MainApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import static com.benatt.passwordsmanager.utils.Constants.ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.DELIMITER;
import static com.benatt.passwordsmanager.utils.Constants.INITIALIZATION_VECTOR;

/**
 * @time 23/11/20
 */
public class Decryptor {
    public static final String TAG = Decryptor.class.getSimpleName();

    public static String decryptPassword(String cipherText) {
        String plainPassword = "";
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null);
            SecretKey secretKey = secretKeyEntry.getSecretKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            String[] cipherProps;
            String actualCipher;
            String ivString;
            if (cipherText.contains(DELIMITER)) {
                cipherProps = cipherText.split(DELIMITER);
                actualCipher = cipherProps[1];
                ivString = cipherProps[0];
            } else {
                // for those passwords that used previous technique to encrypt passwords
                // encryption used a static variable for the initialization vector
                actualCipher = cipherText;
                ivString = MainApp.getPreferences().getString(INITIALIZATION_VECTOR, "");
            }

            byte[] passwordStr = Base64.decode(actualCipher, Base64.DEFAULT);
//            String ivStr = MainApp.getPreferences().getString(INITIALIZATION_VECTOR, "");
            byte[] iv = Base64.decode(ivString, Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            plainPassword = new String(cipher.doFinal(passwordStr), StandardCharsets.UTF_8);
        } catch (KeyStoreException | UnrecoverableEntryException | BadPaddingException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | IllegalBlockSizeException e) {
//            Log.e(TAG, "decryptPassword: ", e);
        }

        return plainPassword;
    }
}
