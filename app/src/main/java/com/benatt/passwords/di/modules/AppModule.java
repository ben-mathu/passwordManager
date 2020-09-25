package com.benatt.passwords.di.modules;

import com.benatt.passwords.MainApp;

import java.security.KeyStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author bernard
 */
@Module
public class AppModule {

    @Singleton
    @Provides
    public KeyStore provideKeyStore() {
        return MainApp.getKeyStore();
    }
}
