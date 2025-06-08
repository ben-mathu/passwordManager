package com.benatt.passwordsmanager.views;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.utils.Encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author bernard
 */
@HiltViewModel
public class MainViewModel extends ViewModel {
    private static final String TAG = MainViewModel.class.getSimpleName();
    private final PasswordRepository passwordRepo;
    private final PublicKey publicKey;

    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isNotHome = new MutableLiveData<>(false);

    private Disposable disposable;
    private final CompositeDisposable compositeDisposable;

    @Inject
    public MainViewModel(PasswordRepository passwordRepo,
                         PublicKey publicKey) {
        this.passwordRepo = passwordRepo;
        this.publicKey = publicKey;
        this.compositeDisposable = new CompositeDisposable();
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

    public void savedAndDecrypt(List<Password> passwordList) {
        try {
            for (Password password : passwordList) {
                password.setCipher(Encryptor.encrypt(publicKey, password.getCipher()));

                savePassword(password);
            }
        } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                 NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e(TAG, "savedAndDecrypt: Error", e);
        }
    }

    private void savePassword(Password password) {
        compositeDisposable.add(passwordRepo.save(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> Log.d(TAG, "savePasswords: " + msg), throwable ->
                        Log.e(TAG, "savePasswords: Error" + throwable.getLocalizedMessage(), throwable)
                )
        );
    }

    public void savePasswords(List<Password> passwords) {
        disposable = passwordRepo.saveAll(passwords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> Log.d(TAG, "savePasswords: " + msg), throwable ->
                        Log.e(TAG, "savePasswords: Error" + throwable.getLocalizedMessage(), throwable)
                );
    }

    public MutableLiveData<Boolean> isNotHome() {
        return isNotHome;
    }

    public void setNotHome(boolean isHome) {
        this.isNotHome.postValue(isHome);
    }
}
