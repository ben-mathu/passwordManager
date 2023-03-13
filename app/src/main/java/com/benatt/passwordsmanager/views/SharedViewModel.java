package com.benatt.passwordsmanager.views;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SharedViewModel extends ViewModel {
    public MutableLiveData<Boolean> refreshList = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> msgEmpty = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLogin = new MutableLiveData<>();
    private Disposable disposable;
    private final PasswordRepository passwordRepository;

    @Inject
    public SharedViewModel(PasswordRepository passwordRepository) {
        this.passwordRepository = passwordRepository;
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
                        msgEmpty.setValue("No saved passwords.");
                    else
                        passwords.setValue(passwordsList);
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
}
