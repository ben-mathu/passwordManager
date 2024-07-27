package com.benatt.passwordsmanager.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.views.MainViewModel;
import com.benatt.passwordsmanager.views.SharedViewModel;
import com.benatt.passwordsmanager.views.addpassword.AddPasswordViewModel;
import com.benatt.passwordsmanager.views.passwords.PasswordsViewModel;

import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author bernard
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private UserRepository userRepository;
    private PublicKey publicKey;
    private PublicKey prevPublicKey;
    private PasswordRepository passwordRepository;

    @Inject
    public ViewModelFactory(PasswordRepository passwordRepository,
                            UserRepository userRepository,
                            PublicKey publicKey,
                            @Named("PREV_ALIAS") PublicKey prevPublicKey) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.publicKey = publicKey;
        this.prevPublicKey = prevPublicKey;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {

            MainViewModel viewModel = new MainViewModel(userRepository, passwordRepository,
                    publicKey, prevPublicKey);
            return (T) viewModel;
        } else if (modelClass.isAssignableFrom(PasswordsViewModel.class)) {

            PasswordsViewModel passwordsViewModel = new PasswordsViewModel(publicKey);
            return (T) passwordsViewModel;
        } else if (modelClass.isAssignableFrom(AddPasswordViewModel.class)) {

            AddPasswordViewModel addPasswordViewModel = new AddPasswordViewModel(publicKey, passwordRepository);
            return (T) addPasswordViewModel;
        } else if (modelClass.isAssignableFrom(SharedViewModel.class)) {
            SharedViewModel sharedViewModel = new SharedViewModel(passwordRepository,
                    prevPublicKey, publicKey);
            return (T) sharedViewModel;
        }

        throw new IllegalArgumentException("Unknown class");
    }
}
