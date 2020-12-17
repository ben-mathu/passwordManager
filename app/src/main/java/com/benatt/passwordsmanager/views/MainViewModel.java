package com.benatt.passwordsmanager.views;

import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.user.UserRepository;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class MainViewModel extends ViewModel {
    private final UserRepository userRepo;

    @Inject
    public MainViewModel(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
}
