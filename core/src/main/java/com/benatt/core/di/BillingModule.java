package com.benatt.core.di;

import android.app.Application;

import com.benatt.core.billing.BillingManager;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class BillingModule {
    @Provides
    public BillingManager provideBillingManager(Application application) {
        return new BillingManager(application);
    }
}