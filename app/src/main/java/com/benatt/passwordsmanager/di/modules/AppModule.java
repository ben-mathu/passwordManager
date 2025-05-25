package com.benatt.passwordsmanager.di.modules;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.benatt.passwordsmanager.utils.BillingManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    @Provides
    public SharedPreferences providePreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    public BillingManager provideBillingManager(Application application) {
        return new BillingManager(application);
    }
}