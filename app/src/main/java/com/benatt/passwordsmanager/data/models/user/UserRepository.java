package com.benatt.passwordsmanager.data.models.user;

import com.benatt.passwordsmanager.data.models.Dao;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * @author bernard
 */
public class UserRepository implements Dao<Password> {

    @Inject
    public UserRepository() {
        // finders keepers
    }

    @Override
    public Observable<String> save(Password item) {
        return null;
    }

    @Override
    public Observable<List<Password>> getAll() {
        return null;
    }

    @Override
    public Completable delete(Password password) {
        return null;
    }

    @Override
    public Observable<String> saveAll(List<Password> items) {
        return null;
    }
}
