package com.benatt.passwordsmanager.data.models.user;

import com.benatt.passwordsmanager.data.models.Dao;
import com.benatt.passwordsmanager.data.models.user.model.User;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * @author bernard
 */
public class UserRepository extends Dao<User> {

    @Inject
    public UserRepository() {}

    @Override
    public Observable<String> saveAll(List<User> items) {
        return null;
    }
}
