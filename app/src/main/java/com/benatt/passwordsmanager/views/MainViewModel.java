package com.benatt.passwordsmanager.views;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.utils.Encryptor;
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
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private final UserRepository userRepo;
    private PasswordRepository passwordRepo;
    private SecretKey secretKey;

    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<List<Password>> liveData = new MutableLiveData<>();
    public MutableLiveData<String> encryptedString = new MutableLiveData<>();

    private Disposable disposable;

    @Inject
    public MainViewModel(UserRepository userRepo, PasswordRepository passwordRepo, SecretKey secretKey) {
        this.userRepo = userRepo;
        this.passwordRepo = passwordRepo;
        this.secretKey = secretKey;
    }

    public void getAllPasswords() {
        disposable = passwordRepo.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passwords -> {
                    liveData.setValue(passwords);
                }, throwable -> {
                    message.setValue("Error getting passwords");
                    Log.e(TAG, "getAllPasswords: Error " + throwable.getMessage(), throwable);
                });
    }

    @Override
    protected void onCleared() {
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        super.onCleared();
    }

    public void encryptPasswordData(List<Password> passwords) {
        try {
            String cipher = Encryptor.encrypt(secretKey, new Gson().toJson(passwords));
            encryptedString.setValue(cipher);
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
