package com.benatt.passwordsmanager.data.models;

import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author bernard
 */
public interface Dao<T> {
    Observable<String> save(T item);
    Observable<List<T>> getAll();

    Completable delete(Password password);

    Observable<String> saveAll(List<T> items);
}
