package com.benatt.passwords.di;

import com.benatt.passwords.di.modules.DbModule;
import com.benatt.passwords.di.modules.KeyStoreModule;
import com.benatt.passwords.views.MainActivity;
import com.benatt.passwords.views.addpassword.AddPasswordFragment;
import com.benatt.passwords.views.passwords.PasswordsFragment;

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
