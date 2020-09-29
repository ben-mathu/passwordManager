package com.benatt.passwords.views.passwords;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwords.data.models.passwords.PasswordRepository;
import com.benatt.passwords.data.models.passwords.model.Password;

import java.util.List;

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

    public void unsubscribe() {
        if (disposable != null)
            if (!disposable.isDisposed())
                disposable.dispose();
    }
}
