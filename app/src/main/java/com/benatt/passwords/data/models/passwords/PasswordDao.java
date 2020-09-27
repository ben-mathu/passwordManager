package com.benatt.passwords.data.models.passwords;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.benatt.passwords.data.models.passwords.model.Password;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author bernard
 */
@Dao
public interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(Password password);

    @Query("SELECT * FROM passwords")
    Observable<List<Password>> getAll();
}
