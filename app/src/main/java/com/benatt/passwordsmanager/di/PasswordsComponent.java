package com.benatt.passwordsmanager.di;

import com.benatt.passwordsmanager.di.modules.DbModule;
import com.benatt.passwordsmanager.di.modules.KeyStoreModule;
import com.benatt.passwordsmanager.views.MainActivity;
import com.benatt.passwordsmanager.views.addpassword.AddPasswordFragment;
import com.benatt.passwordsmanager.views.passwords.PasswordsFragment;

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
