package com.benatt.passwordsmanager.views.passwords;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.utils.Encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author bernard
 */
public class PasswordsViewModel extends ViewModel {
    private SecretKey secretKey;
    public MutableLiveData<String> encryptedString = new MutableLiveData<>();

    @Inject
    public PasswordsViewModel(
            SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public void encryptPasswordData(String passwords) {
        try {
            String cipher = Encryptor.encrypt(secretKey, passwords);
            encryptedString.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
