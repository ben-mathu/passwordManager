package com.benatt.passwordmanager.data.models.user;

import com.benatt.passwordmanager.data.models.Dao;
import com.benatt.passwordmanager.data.models.passwords.model.Password;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author bernard
 */
public class UserRepository extends Dao<Password> {

    @Inject
    public UserRepository() {}

    @Override
    public Observable<String> saveAll(List<Password> items) {
        return null;
    }
}
