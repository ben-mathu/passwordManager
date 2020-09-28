package com.benatt.passwords.views.addpassword;

import android.util.Log;

import androidx.arch.core.internal.SafeIterableMap;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwords.data.models.passwords.PasswordRepository;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.utils.Encryptor;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author bernard
 */
public class AddPasswordViewModel extends ViewModel {
    public static final String TAG = AddPasswordViewModel.class.getSimpleName();

    MutableLiveData<String> msgView = new MutableLiveData<>();
    MutableLiveData<Boolean> goToPasswordsFragments = new MutableLiveData<>();

    private SecretKey secretKey;
    private PasswordRepository passwordRepository;

    private Disposable disposable;

    @Inject
    public AddPasswordViewModel(SecretKey secretKey, PasswordRepository passwordRepository) {
        this.secretKey = secretKey;
        this.passwordRepository = passwordRepository;
    }

    public void savePassword(String password, String accountName) {
        if (password.isEmpty())
            msgView.setValue("Password is empty");
        else if (accountName.isEmpty())
            msgView.setValue("Please provide the account name");
        else {
            try {
                String cipher = Encryptor.encryptPassword(secretKey, password);
                Password passwd = new Password();
                passwd.setAccountName(accountName);
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
            } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                Log.e(TAG, "savePassword: Error", e);
            }
        }
    }

    public void unsubscribe() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }
}
