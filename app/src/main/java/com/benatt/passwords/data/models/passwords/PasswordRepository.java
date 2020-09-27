package com.benatt.passwords.data.models.passwords;

import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.data.models.Dao;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * @author bernard
 */
public class PasswordRepository extends Dao<Password> {

    private PasswordDao passwordDao;

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
}
