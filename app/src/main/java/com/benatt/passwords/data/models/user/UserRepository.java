package com.benatt.passwords.data.models.user;

import com.benatt.passwords.data.models.Dao;
import com.benatt.passwords.data.models.passwords.model.Password;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class UserRepository extends Dao<Password> {

    @Inject
    public UserRepository() {}

}
