package com.benatt.passwordsmanager.data.db;

import static com.benatt.passwordsmanager.utils.Constants.DB_VERSION;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.benatt.passwordsmanager.data.models.passwords.PasswordDao;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.data.models.user.UserDao;
import com.benatt.passwordsmanager.data.models.user.model.User;

/**
 * @author bernard
 */
@Database(entities = {User.class, Password.class}, version = DB_VERSION, exportSchema = false)
public abstract class PasswordsDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract PasswordDao passwordDao();
}
