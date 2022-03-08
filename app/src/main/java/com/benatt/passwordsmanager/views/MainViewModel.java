package com.benatt.passwordsmanager.views;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.utils.Decryptor;
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
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();

    private PasswordRepository passwordRepo;
    private SecretKey secretKey;

    public final MutableLiveData<String> message = new MutableLiveData<>();
    public final MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public final MutableLiveData<String> encipheredPasswords = new MutableLiveData<>();
    public final MutableLiveData<String> decryptedPasswords = new MutableLiveData<>();

    private Disposable disposable;

    @Inject
    public MainViewModel(UserRepository userRepo, PasswordRepository passwordRepo, SecretKey secretKey) {
        this.passwordRepo = passwordRepo;
        this.secretKey = secretKey;
    }

    @Override
    protected void onCleared() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        super.onCleared();
    }

    public void getPasswords() {
        disposable = passwordRepo.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passwordList -> {
                    if (passwordList.isEmpty())
                        message.setValue("There are no saved passwords");
                    else
                        passwords.setValue(passwordList);
                }, throwable -> message.setValue("Error occurred. Please try again"));
    }

    public void encryptPasswords(String json) {
        try {
            String cipher = Encryptor.encrypt(secretKey, json);
            encipheredPasswords.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void decrypt(String jsonCipher) {
        String json = Decryptor.decryptPassword(jsonCipher);
        decryptedPasswords.setValue(json);
    }

    public void savePasswords(List<Password> passwords) {
        disposable = passwordRepo.saveAll(passwords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> Log.d(TAG, "savePasswords: " + msg),
                        throwable -> Log.e(TAG, "savePasswords: Error"
                                + throwable.getLocalizedMessage(), throwable)
                );
    }
}
