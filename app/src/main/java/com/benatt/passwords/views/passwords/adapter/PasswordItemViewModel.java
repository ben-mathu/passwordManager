package com.benatt.passwords.views.passwords.adapter;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwords.data.models.passwords.model.Password;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.inject.Inject;

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
