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
    private PasswordRepository passwordRepository;

    private Disposable disposable;

    MutableLiveData<String> msgEmpty = new MutableLiveData<>();
    MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public final MutableLiveData<String> encryptedString;

    @Inject
    public PasswordsViewModel(
            SecretKey secretKey,
            PasswordRepository passwordRepository) {
        this.secretKey = secretKey;
        this.passwordRepository = passwordRepository;
        encryptedString = new MutableLiveData<>();
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
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe() {
        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }
}
