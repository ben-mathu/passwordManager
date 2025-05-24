package com.benatt.passwordsmanager.di.modules;

import static com.benatt.passwordsmanager.BuildConfig.ALIAS;
import static com.benatt.passwordsmanager.BuildConfig.PREV_ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.NAMED_PREV_KEY_ALIAS;

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
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Calendar;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ViewModelComponent;

/**
 * @author bernard
 */
@Module
@InstallIn(ViewModelComponent.class)
public class KeyStoreModule {
    public static final String TAG = KeyStore.class.getSimpleName();

    @Provides
    public PublicKey providePublicKey() {
        return getPublicKey(ALIAS);
    }

    @Provides
    @Named(NAMED_PREV_KEY_ALIAS)
    public PublicKey providesPrevPublicKey() {
        return getPublicKey(PREV_ALIAS);
    }

    private PublicKey getPublicKey(String alias) {
        PublicKey publicKey;
        try {
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                KeyStore.PrivateKeyEntry privateKeyEntry =
                        (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
                if (privateKeyEntry != null)
                    publicKey = privateKeyEntry.getCertificate().getPublicKey();
                else
                    publicKey = createKeys(alias);
            } else {
                publicKey = createKeys(alias);
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

    private PublicKey createKeys(String alias) throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA,
                "AndroidKeyStore");

        KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setKeySize(2048)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .build();
        kpg.initialize(keyGenParameterSpec);

        return kpg.generateKeyPair().getPublic();
    }

    private KeyStore getKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException e) {
            Log.e(TAG, "getKeyStore: ", e);
        } catch (NoSuchAlgorithmException | IOException | CertificateException e) {
            Log.e(TAG, "getKeyStore -> ", e);
        }
        return keyStore;
    }
}
