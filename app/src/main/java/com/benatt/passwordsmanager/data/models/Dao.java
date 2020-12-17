package com.benatt.passwordsmanager.data.models;

import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author bernard
 */
public abstract class Dao<T> {
    public Observable<String> save(T item) { return Observable.just(""); }
    public Observable<List<T>> getAll() { return Observable.just(null); }
    public Observable<T> get(int value) { return Observable.just(null); }

    public Completable delete(Password password) { return Completable.complete(); }
}
