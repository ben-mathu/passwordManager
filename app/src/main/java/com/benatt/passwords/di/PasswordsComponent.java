package com.benatt.passwords.di;

import com.benatt.passwords.di.modules.AppModule;
import com.benatt.passwords.views.MainActivity;
import com.benatt.passwords.views.addpassword.AddPasswordFragment;
import com.benatt.passwords.views.passwords.PasswordsFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author bernard
 */
@Singleton
@Component(modules = {AppModule.class})
public interface PasswordsComponent {
    void inject(MainActivity mainActivity);
    void inject(PasswordsFragment passwordsFragment);
    void inject(AddPasswordFragment addPasswordFragment);
}
