package com.benatt.passwordmanager.views.addpassword;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.utils.Encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
public class AddPasswordViewModel extends ViewModel {
    public static final String TAG = AddPasswordViewModel.class.getSimpleName();

    MutableLiveData<String> msgView = new MutableLiveData<>();
    MutableLiveData<Boolean> goToPasswordsFragments = new MutableLiveData<>();
    public MutableLiveData<String> editPassword = new MutableLiveData<>("");
    public MutableLiveData<String> editAccountName = new MutableLiveData<>("");

    private SecretKey secretKey;
    private PasswordRepository passwordRepository;

    private Disposable disposable;

    @Inject
    public AddPasswordViewModel(SecretKey secretKey, PasswordRepository passwordRepository) {
        this.secretKey = secretKey;
        this.passwordRepository = passwordRepository;
    }

    public void savePassword(Password password, String newPassword) {
        try {
            if (password != null) {
                String cipher = Encryptor.encrypt(secretKey, newPassword);
                password.setCipher(cipher);
                disposable = passwordRepository.save(password)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(value -> {
                            msgView.setValue(value);
                            goToPasswordsFragments.setValue(true);
                        }, throwable -> {
                            msgView.setValue("An error occurred, please try again.");
                            Log.e(TAG, "savePassword: ", throwable);
                        });
                return;
            } else {
                String passwordStr = editPassword.getValue();
                String accountNameStr = editAccountName.getValue();
                if (passwordStr.isEmpty())
                    msgView.setValue("Password is empty");
                else if (accountNameStr.isEmpty())
                    msgView.setValue("Please provide the account name");
                else {
                    String cipher = Encryptor.encrypt(secretKey, passwordStr);
                    Password passwd = new Password();
                    passwd.setAccountName(accountNameStr);
                    passwd.setCipher(cipher);
                    disposable = passwordRepository.save(passwd)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(value -> {
                                        msgView.setValue(value);
                                        goToPasswordsFragments.setValue(true);
                                    },
                                    throwable -> {
                                        msgView.setValue("An error occurred, please try again.");
                                        Log.e(TAG, "savePassword: ", throwable);
                                    }
                            );
                }
            }
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "savePassword: Error", e);
        }
    }

    public void unsubscribe() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }

    public void deletePassword(Password password) {
        disposable = passwordRepository.delete(password)
                .subscribe(() -> {
                    msgView.setValue(password.toString() + " has been deleted.");
                    goToPasswordsFragments.setValue(true);
                }, throwable -> msgView.setValue(password.toString() + " was not deleted."));
    }

    @Override
    protected void onCleared() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();

        super.onCleared();
    }
}
