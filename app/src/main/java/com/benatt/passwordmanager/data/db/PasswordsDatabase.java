package com.benatt.passwordmanager.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.benatt.passwordmanager.data.models.passwords.PasswordDao;
import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.data.models.user.UserDao;
import com.benatt.passwordmanager.data.models.user.model.User;

import static com.benatt.passwordmanager.utils.Constants.DB_VERSION;

/**
 * @author bernard
 */
@Database(entities = {User.class, Password.class}, version = DB_VERSION, exportSchema = false)
public abstract class PasswordsDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PasswordDao passwordDao();
}
