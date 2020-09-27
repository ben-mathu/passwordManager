package com.benatt.passwords;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.benatt.passwords.di.DaggerPasswordsComponent;
import com.benatt.passwords.di.PasswordsComponent;
import com.benatt.passwords.di.modules.DbModule;

/**
 * @author bernard
 */
public class MainApp extends Application {
    public static final String TAG = MainApp.class.getSimpleName();

    private SharedPreferences preferences;

    private PasswordsComponent passwordsComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        passwordsComponent = DaggerPasswordsComponent.builder()
                .dbModule(new DbModule(this))
                .build();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public PasswordsComponent getPasswordsComponent() {
        return passwordsComponent;
    }
}
