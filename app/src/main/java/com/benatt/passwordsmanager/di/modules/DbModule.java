package com.benatt.passwordsmanager.di.modules;

import static com.benatt.passwordsmanager.utils.Constants.DB_NAME;

import android.app.Application;

import androidx.room.Room;

import com.benatt.passwordsmanager.data.db.PasswordsDatabase;
import com.benatt.passwordsmanager.data.models.passwords.PasswordDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * @author bernard
 */
@Module
@InstallIn(SingletonComponent.class)
public class DbModule {

    @Singleton
    @Provides
    public PasswordsDatabase provideDb(Application application) {
        return Room.databaseBuilder(application, PasswordsDatabase.class, DB_NAME)
                .build();
    }

    @Provides
    @Reusable
    public PasswordDao providePasswordDao(PasswordsDatabase passwordsDatabase) {
        return passwordsDatabase.passwordDao();
    }
}
