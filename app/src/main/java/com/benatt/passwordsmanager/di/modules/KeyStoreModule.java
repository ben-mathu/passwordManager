package com.benatt.passwordsmanager.di.modules;

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

import static com.benatt.passwordsmanager.utils.Constants.ALIAS;

/**
 * @author bernard
 */
@Module
public class KeyStoreModule {
    public static final String TAG = KeyStore.class.getSimpleName();

    @Singleton
    @Provides
    public SecretKey provideSecretKey() {
        SecretKey secretKey;
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
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("The algorithm specified is not correct");
        } catch (UnrecoverableEntryException e) {
            throw new IllegalStateException("No KeyStore for this application");
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Invalid algorithm parameter");
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("No Such Provider");
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Key store exception.");
        }

        return secretKey;
    }

    private SecretKey createKeys() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
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
