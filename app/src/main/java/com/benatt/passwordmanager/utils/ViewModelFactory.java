package com.benatt.passwordmanager.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benatt.passwordmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordmanager.views.MainViewModel;
import com.benatt.passwordmanager.data.models.user.UserRepository;
import com.benatt.passwordmanager.views.addpassword.AddPasswordViewModel;
import com.benatt.passwordmanager.views.passwords.PasswordsViewModel;

import javax.crypto.SecretKey;
import javax.inject.Inject;

/**
 * @author bernard
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private UserRepository userRepository;
    private SecretKey secretKey;
    private PasswordRepository passwordRepository;

    @Inject
    public ViewModelFactory(PasswordRepository passwordRepository,
                            UserRepository userRepository,
                            SecretKey secretKey) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.secretKey = secretKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {

            MainViewModel viewModel = new MainViewModel(userRepository, passwordRepository, secretKey);
            return (T) viewModel;
        } else if (modelClass.isAssignableFrom(PasswordsViewModel.class)) {

            PasswordsViewModel passwordsViewModel = new PasswordsViewModel(secretKey, passwordRepository);
            return (T) passwordsViewModel;
        } else if (modelClass.isAssignableFrom(AddPasswordViewModel.class)) {

            AddPasswordViewModel addPasswordViewModel = new AddPasswordViewModel(secretKey, passwordRepository);
            return (T) addPasswordViewModel;
        }

        throw new IllegalArgumentException("Unknown class");
    }
}