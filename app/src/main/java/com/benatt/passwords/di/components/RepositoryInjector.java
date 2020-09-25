package com.benatt.passwords.di.components;

import com.benatt.passwords.data.models.user.UserRepository;
import com.benatt.passwords.di.modules.DbModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author bernard
 */
@Singleton
@Component(modules = {DbModule.class})
public interface RepositoryInjector {
    void inject(UserRepository userRepo);
}
