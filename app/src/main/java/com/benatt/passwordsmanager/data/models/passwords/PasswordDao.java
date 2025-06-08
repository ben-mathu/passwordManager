package com.benatt.passwordsmanager.data.models.passwords;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * @author bernard
 */
@Dao
public interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Password password);

    @Query("SELECT * FROM passwords")
    Observable<List<Password>> getAll();

    @Query("SELECT * FROM passwords")
    List<Password> getAllPasswords();

    @Query("SELECT * FROM passwords")
    List<Password> getAllForMigration();

    @Delete
    Completable delete(Password password);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAll(List<Password> passwords);

    @Query("select count(*) from passwords")
    Single<Integer> count();
}
