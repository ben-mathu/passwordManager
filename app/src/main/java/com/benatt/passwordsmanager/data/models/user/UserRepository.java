package com.benatt.passwordsmanager.data.models.user;

import com.benatt.passwordsmanager.data.models.Dao;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class UserRepository extends Dao<Password> {

    @Inject
    public UserRepository() {}

}
