package com.benatt.passwords.views.passwords.adapter;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwords.data.models.passwords.model.Password;

/**
 * @author bernard
 */
public class PasswordItemViewModel extends ViewModel {
    public static final String TAG = PasswordItemViewModel.class.getSimpleName();

    public MutableLiveData<String> passwordText = new MutableLiveData<>();
    public MutableLiveData<String> accountName = new MutableLiveData<>();

    public void bind(Password password) {
        passwordText.setValue("Password encrypted");
        accountName.setValue(password.getAccountName());
    }
}
