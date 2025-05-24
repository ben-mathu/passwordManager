package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.BuildConfig.PREV_ALIAS;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.benatt.passwordsmanager.utils.Encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private static final String TAG = SharedViewModel.class.getSimpleName();

    public MutableLiveData<Boolean> refreshList = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> msgEmpty = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLogin = new MutableLiveData<>();
    public MutableLiveData<Boolean> showLoader = new MutableLiveData<>();
    public MutableLiveData<Boolean> bottomNavLiveData = new MutableLiveData<>();
    public MutableLiveData<String> completeMsg = new MutableLiveData<>();

    private Disposable disposable;
    private final PasswordRepository passwordRepository;
    private final PublicKey publicKey;

    @Inject
    public SharedViewModel(PasswordRepository passwordRepository,
                           PublicKey publicKey) {
        this.passwordRepository = passwordRepository;
        this.publicKey = publicKey;
    }

    public void refreshList() {
        refreshList.setValue(true);
    }

    public void getPasswords() {
        disposable = passwordRepository.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passwordsList -> {
                    if (passwordsList.isEmpty())
                        msgEmpty.postValue("No saved passwords.");
                    else
                        passwords.postValue(passwordsList);
                }, throwable -> msgEmpty.setValue("An error occurred."));
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    public void showProgressBar() {
        showLoader.setValue(true);
    }

    public void hideProgressBar() {
        showLoader.setValue(false);
    }

    public void showBottomNav() {
        bottomNavLiveData.setValue(true);
    }

    public void migratePasswords() {
        //                            prevList.setValue(list);
        disposable = passwordRepository.getAllForMigration()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list -> {
                            if (!list.isEmpty())
                                useCurrentEncryptionScheme(list);
                        },
                        throwable ->
                                Log.e(TAG, "migratePasswords: Error retreiving passwords",
                                        throwable)
                );
    }

    public void useCurrentEncryptionScheme(List<Password> list) {
        List<Password> passwords = new ArrayList<>();
        for (Password password : list) {
            try {
                String passwordStr = Decryptor.decryptPassword(password.getCipher(), null, PREV_ALIAS);
                if (passwordStr != null && !passwordStr.isEmpty()) {
                    password.setCipher(Encryptor.encrypt(publicKey, passwordStr));
                    passwords.add(password);
                }
            } catch (Exception e) {
                completeMsg.setValue(e.getMessage());
            } catch (IllegalBlockSizeException | NoSuchPaddingException | BadPaddingException |
                     NoSuchAlgorithmException | InvalidKeyException e) {

                Log.e(TAG, "useCurrentEncryptionScheme: Error", e);
            }
        }

        disposable = passwordRepository.saveAll(passwords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> {
                            completeMsg.setValue(msg);
                            refreshList.setValue(true);
                        },
                        throwable -> completeMsg.setValue(throwable.getMessage())
                );
    }
}