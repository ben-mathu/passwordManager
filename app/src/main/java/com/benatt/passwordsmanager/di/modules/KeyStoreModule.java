package com.benatt.passwordsmanager.di.modules;

import static com.benatt.passwordsmanager.utils.Constants.ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.PREV_ALIAS;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author bernard
 */
@Module
public class KeyStoreModule {
    public static final String TAG = KeyStore.class.getSimpleName();

    @Singleton
    @Provides
    public PublicKey providePrivateKey() {
        PublicKey publicKey = null;
        try {
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                KeyStore.PrivateKeyEntry privateKeyEntry =
                        (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);
                if (privateKeyEntry != null)
                    publicKey = privateKeyEntry.getCertificate().getPublicKey();
                else
                    publicKey = createKeys();
            } else {
                publicKey = createKeys();
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException |
                 InvalidAlgorithmParameterException | NoSuchProviderException |
                 KeyStoreException e) {

            if (e instanceof NoSuchAlgorithmException)
                throw new IllegalStateException("The algorithm specified is not correct");
            else if (e instanceof UnrecoverableEntryException)
                throw new IllegalStateException("No KeyStore for this application");
            else if (e instanceof InvalidAlgorithmParameterException)
                throw new IllegalStateException("Invalid algorithm parameter");
            else if (e instanceof NoSuchProviderException)
                throw new IllegalStateException("No Such Provider");
            else throw new IllegalStateException("Key store exception.");
        }

        return publicKey;
    }

    @Singleton
    @Provides
    public SecretKey providesSecretKey() {
        SecretKey secretKey = null;
        try {
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                KeyStore.SecretKeyEntry secretKeyEntry =
                        (KeyStore.SecretKeyEntry) keyStore.getEntry(PREV_ALIAS, null);
                if (secretKeyEntry != null)
                    secretKey = secretKeyEntry.getSecretKey();
                else
                    secretKey = createSecretKey();
            } else {
                secretKey = createSecretKey();
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException |
                 InvalidAlgorithmParameterException | NoSuchProviderException |
                 KeyStoreException e) {

            if (e instanceof NoSuchAlgorithmException)
                throw new IllegalStateException("The algorithm specified is not correct");
            else if (e instanceof UnrecoverableEntryException)
                throw new IllegalStateException("No KeyStore for this application");
            else if (e instanceof InvalidAlgorithmParameterException)
                throw new IllegalStateException("Invalid algorithm parameter");
            else if (e instanceof NoSuchProviderException)
                throw new IllegalStateException("No Such Provider");
            else throw new IllegalStateException("Key store exception.");
        }

        return secretKey;
    }

    private PublicKey createKeys() throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore");

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(2048)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();
        kpg.initialize(keyGenParameterSpec);

        return kpg.generateKeyPair().getPublic();
    }

    private SecretKey createSecretKey() throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidAlgorithmParameterException {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        KeyGenerator kpg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore");

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                PREV_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();
        kpg.init(keyGenParameterSpec);

        return kpg.generateKey();
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
