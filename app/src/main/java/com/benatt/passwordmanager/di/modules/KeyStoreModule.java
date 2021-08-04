package com.benatt.passwordmanager.di.modules;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.benatt.passwordmanager.utils.Constants.ALIAS;

/**
 * @author bernard
 */
@Module
public class KeyStoreModule {
    public static final String TAG = KeyStore.class.getSimpleName();

    @Singleton
    @Provides
    public SecretKey provideSecretKey() {
        SecretKey secretKey = null;
        try {
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                KeyStore.SecretKeyEntry secretKeyEntry =
                        (KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null);
                if (secretKeyEntry != null)
                    secretKey = secretKeyEntry.getSecretKey();
                else
                    secretKey = createKeys();
            } else {
                secretKey = createKeys();
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException e) {
            if (e instanceof NoSuchAlgorithmException)
                throw new IllegalStateException("The algorithm specified is not correct");
            else if (e instanceof UnrecoverableEntryException)
                throw new IllegalStateException("No KeyStore for this application");
            else if (e instanceof InvalidAlgorithmParameterException)
                throw new IllegalStateException("Invalid algorithm parameter");
            else if (e instanceof NoSuchProviderException)
                throw new IllegalStateException("No Such Provider");
            else if (e instanceof KeyStoreException)
                throw new IllegalStateException("Key store exception.");
        }

        return secretKey;
    }

    private SecretKey createKeys() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
        );
        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT |
                        KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();

        keyGenerator.init(keyGenParameterSpec);

        return keyGenerator.generateKey();
    }

    private KeyStore getKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException e) {
            Log.e(TAG, "getKeyStore: ", e);
        } catch (NoSuchAlgorithmException | IOException | CertificateException e) {
            e.printStackTrace();
        }
        return keyStore;
    }
}
