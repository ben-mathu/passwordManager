package com.benatt.passwordmanager.di;

import com.benatt.passwordmanager.di.modules.DbModule;
import com.benatt.passwordmanager.di.modules.KeyStoreModule;
import com.benatt.passwordmanager.views.MainActivity;
import com.benatt.passwordmanager.views.addpassword.AddPasswordFragment;
import com.benatt.passwordmanager.views.passwords.PasswordsFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author bernard
 */
@Singleton
@Component(modules = {DbModule.class, KeyStoreModule.class})
public interface PasswordsComponent {
    void inject(MainActivity mainActivity);
    void inject(PasswordsFragment passwordsFragment);
    void inject(AddPasswordFragment addPasswordFragment);
}
