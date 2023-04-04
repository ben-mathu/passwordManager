package com.benatt.passwordsmanager.views;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.benatt.passwordsmanager.utils.Encryptor;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author bernard
 */
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private final UserRepository userRepo;
    private PasswordRepository passwordRepo;
    private PublicKey publicKey;
    private SecretKey secretKey;

    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> encipheredPasswords = new MutableLiveData<>();
    public MutableLiveData<String> decryptedPasswords = new MutableLiveData<>();

    private Disposable disposable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public MainViewModel(UserRepository userRepo, PasswordRepository passwordRepo,
                         PublicKey publicKey, SecretKey secretKey) {
        this.userRepo = userRepo;
        this.passwordRepo = passwordRepo;
        this.publicKey = publicKey;
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
            String cipher = Encryptor.encrypt(publicKey, json);
            encipheredPasswords.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public void decrypt(String jsonCipher, PrivateKey pKey) {
        List<Password> passwordList = new Gson().fromJson(jsonCipher,
                new TypeToken<List<Password>>(){}.getType());

        try {
            for (Password password : passwordList) {
                String passwordStr = Decryptor.decryptPassword(password.getCipher().trim(), pKey);
                password.setCipher(Encryptor.encrypt(publicKey, passwordStr));

                savePassword(password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        decryptedPasswords.setValue(json);
    }

    private void savePassword(Password password) {
        compositeDisposable.add(passwordRepo.save(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> { Log.d(TAG, "savePasswords: " + msg); },
                        throwable -> {
                            Log.e(TAG, "savePasswords: Error" + throwable.getLocalizedMessage(), throwable);
                        }
                )
        );
    }

    public void savePasswords(List<Password> passwords) {
        disposable = passwordRepo.saveAll(passwords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> {
                            Log.d(TAG, "savePasswords: " + msg);
                        },
                        throwable -> {
                            Log.e(TAG, "savePasswords: Error" + throwable.getLocalizedMessage(), throwable);
                        }
                );
    }
}
