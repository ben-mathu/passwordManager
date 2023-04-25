package com.benatt.passwordsmanager;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.benatt.passwordsmanager.di.DaggerPasswordsComponent;
import com.benatt.passwordsmanager.di.PasswordsComponent;
import com.benatt.passwordsmanager.di.modules.DbModule;
import com.benatt.passwordsmanager.di.modules.KeyStoreModule;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author bernard
 */
public class MainApp extends Application {
    public static final String TAG = MainApp.class.getSimpleName();

    private static SharedPreferences preferences;

    private PasswordsComponent passwordsComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        passwordsComponent = DaggerPasswordsComponent.builder()
                .dbModule(new DbModule(this))
                .build();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static SharedPreferences getPreferences() {
        return preferences;
    }

    public PasswordsComponent getPasswordsComponent() {
        return passwordsComponent;
    }
}
