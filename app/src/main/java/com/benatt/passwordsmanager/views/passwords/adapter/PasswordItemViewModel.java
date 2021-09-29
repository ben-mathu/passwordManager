package com.benatt.passwordsmanager.views.passwords.adapter;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;

/**
 * @author bernard
 */
public class PasswordItemViewModel extends ViewModel {
    public static final String TAG = PasswordItemViewModel.class.getSimpleName();

    public MutableLiveData<String> passwordText = new MutableLiveData<>();
    public MutableLiveData<String> accountName = new MutableLiveData<>();

    public void bind(Password password, Context context) {
        passwordText.setValue(context.getString(R.string.password_encrypted));
        accountName.setValue(password.getAccountName());
    }
}
