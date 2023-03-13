package com.benatt.passwordsmanager.data.models.passwords;

import com.benatt.passwordsmanager.data.models.Dao;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * @author bernard
 */
public class PasswordRepository extends Dao<Password> {

    private final PasswordDao passwordDao;

    @Inject
    public PasswordRepository(PasswordDao passwordDao) {
        this.passwordDao = passwordDao;
    }

    @Override
    public Observable<String> save(Password item) {
        return Observable.create(emitter -> {
            passwordDao.save(item);
            emitter.onNext("Password saved.");
        });
    }

    @Override
    public Observable<List<Password>> getAll() {
        return passwordDao.getAll();
    }

    @Override
    public Completable delete(Password password) {
        return passwordDao.delete(password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<String> saveAll(List<Password> items) {
        return Observable.create(emitter -> {
            passwordDao.saveAll(items);
            emitter.onNext("Passwords Saved successfully");
        });
    }
}
