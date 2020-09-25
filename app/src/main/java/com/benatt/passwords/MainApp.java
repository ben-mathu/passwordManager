package com.benatt.passwords;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.benatt.passwords.di.DaggerPasswordsComponent;
import com.benatt.passwords.di.PasswordsComponent;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author bernard
 */
public class MainApp extends Application {
    public static final String TAG = MainApp.class.getSimpleName();

    private SharedPreferences preferences;
    private static KeyStore keyStore;

    private PasswordsComponent passwordsComponent;

    private List<String> aliases;

    @Override
    public void onCreate() {
        super.onCreate();
        passwordsComponent = DaggerPasswordsComponent.create();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        keyStore = getKeyStore();
    }

    public static KeyStore getKeyStore() {
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

    public PasswordsComponent getPasswordsComponent() {
        return passwordsComponent;
    }
}
