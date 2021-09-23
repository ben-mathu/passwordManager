package com.benatt.passwordmanager.views.passwords;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.utils.Encryptor;
import com.google.gson.Gson;

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
    private PasswordRepository passwordRepository;

    private Disposable disposable;

    MutableLiveData<String> msgEmpty = new MutableLiveData<>();
    MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> encryptedString = new MutableLiveData<>();

    @Inject
    public PasswordsViewModel(
            SecretKey secretKey,
            PasswordRepository passwordRepository) {
        this.secretKey = secretKey;
        this.passwordRepository = passwordRepository;
    }

    public void getPasswords() {
        disposable  = passwordRepository.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passwordsList -> {
                    if (passwordsList.isEmpty())
                        msgEmpty.setValue("No saved passwords.");
                    else
                        passwords.setValue(passwordsList);
                }, throwable -> msgEmpty.setValue("An error occurred."));
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

    public void unsubscribe() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }
}
