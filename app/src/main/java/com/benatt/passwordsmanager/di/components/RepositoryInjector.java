package com.benatt.passwordsmanager.di.components;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.di.modules.DbModule;

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
