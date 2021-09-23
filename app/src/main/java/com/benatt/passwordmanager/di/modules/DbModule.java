package com.benatt.passwordmanager.di.modules;

import android.app.Application;

import androidx.room.Room;

import com.benatt.passwordmanager.data.db.PasswordsDatabase;
import com.benatt.passwordmanager.data.models.passwords.PasswordDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;

import static com.benatt.passwordmanager.utils.Constants.DB_NAME;

/**
 * @author bernard
 */
@Module
public class DbModule {
    private Application application;

    public DbModule(Application application) {
        this.application = application;
    }

    @Provides
    @Reusable
    public PasswordDao providePasswordDao(PasswordsDatabase passwordsDatabase) {
        return passwordsDatabase.passwordDao();
    }

    @Singleton
    @Provides
    public PasswordsDatabase provideDb() {
        return Room.databaseBuilder(application, PasswordsDatabase.class, DB_NAME)
                .build();
    }
}
