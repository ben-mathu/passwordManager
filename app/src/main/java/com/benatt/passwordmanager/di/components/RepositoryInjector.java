package com.benatt.passwordmanager.di.components;

import com.benatt.passwordmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordmanager.data.models.user.UserRepository;
import com.benatt.passwordmanager.di.modules.DbModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author bernard
 */
@Singleton
@Component(modules = {DbModule.class})
public interface RepositoryInjector {
    void inject(UserRepository userRepo);
    void inject(PasswordRepository passwordRepository);
}
