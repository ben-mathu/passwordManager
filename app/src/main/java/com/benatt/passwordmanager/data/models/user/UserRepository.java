package com.benatt.passwordmanager.data.models.user;

import com.benatt.passwordmanager.data.models.Dao;
import com.benatt.passwordmanager.data.models.passwords.model.Password;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class UserRepository extends Dao<Password> {

    @Inject
    public UserRepository() {}

}
