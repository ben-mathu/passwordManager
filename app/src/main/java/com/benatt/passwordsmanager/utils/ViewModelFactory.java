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
import com.google.firebase.database.DatabaseReference;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.inject.Inject;

/**
 * @author bernard
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    private UserRepository userRepository;
    private DatabaseReference databaseReference;
    private PasswordRepository passwordRepository;

    @Inject
    public ViewModelFactory(PasswordRepository passwordRepository,
                            UserRepository userRepository, DatabaseReference databaseReference) {
        this.passwordRepository = passwordRepository;
        this.userRepository = userRepository;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MainViewModel.class)) {

            MainViewModel viewModel = new MainViewModel(userRepository, passwordRepository,
                    databaseReference);
            return (T) viewModel;
        } else if (modelClass.isAssignableFrom(PasswordsViewModel.class)) {

            PasswordsViewModel passwordsViewModel = new PasswordsViewModel();
            return (T) passwordsViewModel;
        } else if (modelClass.isAssignableFrom(AddPasswordViewModel.class)) {

            AddPasswordViewModel addPasswordViewModel = new AddPasswordViewModel(passwordRepository);
            return (T) addPasswordViewModel;
        } else if (modelClass.isAssignableFrom(SharedViewModel.class)) {
            SharedViewModel sharedViewModel = new SharedViewModel(passwordRepository);
            return (T) sharedViewModel;
        }

        throw new IllegalArgumentException("Unknown class");
    }
}
