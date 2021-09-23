package com.benatt.passwordmanager;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.benatt.passwordmanager.di.DaggerPasswordsComponent;
import com.benatt.passwordmanager.di.PasswordsComponent;
import com.benatt.passwordmanager.di.modules.DbModule;

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
