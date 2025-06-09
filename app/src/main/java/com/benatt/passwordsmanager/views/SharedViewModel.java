package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.BuildConfig.ALIAS;
import static com.benatt.passwordsmanager.BuildConfig.PREV_ALIAS;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.benatt.passwordsmanager.utils.Encryptor;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@HiltViewModel
public class SharedViewModel extends ViewModel {
    private static final String TAG = SharedViewModel.class.getSimpleName();

    public MutableLiveData<Boolean> refreshList = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> errorMsg = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLogin = new MutableLiveData<>();
    public MutableLiveData<Boolean> showLoader = new MutableLiveData<>();
    public MutableLiveData<Boolean> bottomNavLiveData = new MutableLiveData<>();
    public MutableLiveData<String> completeMsg = new MutableLiveData<>();
    public MutableLiveData<String> fileNameLiveData = new MutableLiveData<>();

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
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
        Disposable disposable = passwordRepository.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(passwordsList -> {
                    if (passwordsList.isEmpty())
                        errorMsg.postValue("No saved passwords.");
                    else
                        passwords.postValue(passwordsList);
                }, throwable -> errorMsg.setValue("An error occurred."));
        compositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public void showBottomNav() {
        bottomNavLiveData.setValue(true);
    }

    public void migratePasswords() {
        //                            prevList.setValue(list);
        Disposable disposable = passwordRepository.getAllForMigration()
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
        compositeDisposable.add(disposable);
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

        Disposable disposable = passwordRepository.saveAll(passwords)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        msg -> {
                            completeMsg.setValue(msg);
                            refreshList.setValue(true);
                        },
                        throwable -> completeMsg.setValue(throwable.getMessage())
                );
        compositeDisposable.add(disposable);
    }

    public void createBackup(Activity context, final List<Password> passwordList) {
        showLoader.postValue(true);

        Disposable disposable = Single.create((SingleEmitter<String> emitter) -> {
                    SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());
                    String fileName = sp.format(new Date()) + ".txt";

                    if (passwordList.isEmpty()) {
                        passwordList.addAll(passwordRepository.getAllPasswords());
                    }

                    try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {

                        for (Password password : passwordList) {
                            password.setCipher(Decryptor.decryptPassword(password.getCipher(), null, ALIAS));
                        }

                        String json = new Gson().toJson(passwordList);
                        if (fos != null) fos.write(json.getBytes(Charset.defaultCharset()));
                        else {
                            Log.e(TAG, "Error uploading file -> important");
                            emitter.onError(new Throwable("Error uploading file -> important"));
                            return;
                        }

                        emitter.onSuccess(fileName);
                    } catch (IOException | Exception e) {
                        Log.e(TAG, "createBackup -> Error", e);
                        emitter.onError(new Throwable("Error compiling file -> important"));
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileNameLiveData::postValue,
                        throwable -> errorMsg.postValue(throwable.getMessage()));
        compositeDisposable.add(disposable);
    }

    public void restorePasswords(Activity context, Uri contentUri) {
        Disposable disposable = Single.create(emitter -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(contentUri)) {
                if (inputStream != null) {
                    StringBuilder sb = new StringBuilder();
                    byte[] buffer = new byte[2046];
                    long read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        sb.append(new String(buffer, 0, (int) read));
                    }

                    Gson gson = new Gson();
                    List<Password> passwordsList = gson.fromJson(sb.toString(), new TypeToken<List<Password>>() {
                    }.getType());
                    savedAndDecrypt(passwordsList);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> showLoader.postValue(false),
                        throwable -> showLoader.postValue(false));
        compositeDisposable.add(disposable);
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
        compositeDisposable.add(passwordRepository.save(password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> Log.d(TAG, "savePasswords: " + msg), throwable ->
                        Log.e(TAG, "savePasswords: Error" + throwable.getLocalizedMessage(), throwable)
                )
        );
    }
}