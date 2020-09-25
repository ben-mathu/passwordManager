package com.benatt.passwords.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benatt.passwords.views.MainViewModel;
import com.benatt.passwords.data.models.user.UserRepository;
import com.benatt.passwords.views.addpassword.AddPasswordViewModel;
import com.benatt.passwords.views.passwords.PasswordsViewModel;

import java.security.KeyStore;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private UserRepository userRepository;
    private KeyStore keyStore;

    @Inject
    public ViewModelFactory(UserRepository userRepository,
                            KeyStore keyStore) {
        this.userRepository = userRepository;
        this.keyStore = keyStore;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {

            MainViewModel viewModel = new MainViewModel(userRepository);
            return (T) viewModel;
        } else if (modelClass.isAssignableFrom(PasswordsViewModel.class)) {

            PasswordsViewModel passwordsViewModel = new PasswordsViewModel(keyStore);
            return (T) passwordsViewModel;
        } else if (modelClass.isAssignableFrom(AddPasswordViewModel.class)) {
            AddPasswordViewModel addPasswordViewModel = new AddPasswordViewModel(keyStore);
            return (T) addPasswordViewModel;
        }

        throw new IllegalArgumentException("Unknown class");
    }
}
