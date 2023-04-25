package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.utils.Constants.ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.DB_REF;
import static com.benatt.passwordsmanager.utils.Constants.USER_PASSPHRASE;

import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.benatt.passwordsmanager.utils.Encryptor;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
    private final SharedPreferences preferences;
    private PasswordRepository passwordRepo;
    private DatabaseReference reference;

    public MutableLiveData<String> message = new MutableLiveData<>();
    public MutableLiveData<List<Password>> passwords = new MutableLiveData<>();
    public MutableLiveData<String> encipheredPasswords = new MutableLiveData<>();
    public MutableLiveData<String> decryptedPasswords = new MutableLiveData<>();
    public MutableLiveData<Boolean> emailExists = new MutableLiveData<>();

    private Disposable disposable;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public MainViewModel(UserRepository userRepo, PasswordRepository passwordRepo,
                         DatabaseReference reference) {
        this.userRepo = userRepo;
        this.passwordRepo = passwordRepo;
        this.reference = reference;
        this.preferences = MainApp.getPreferences();
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
            String passphrase = preferences.getString(USER_PASSPHRASE, "");
            String cipher = Encryptor.encrypt(json, passphrase);
            encipheredPasswords.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 NoSuchAlgorithmException | InvalidKeyException | KeyStoreException |
                 UnrecoverableEntryException | CertificateException | IOException |
                 NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public void decrypt(String jsonCipher, PrivateKey pKey) {
        List<Password> passwordList = new Gson().fromJson(jsonCipher,
                new TypeToken<List<Password>>(){}.getType());

        try {
            String passphrase = preferences.getString(USER_PASSPHRASE, "");
            for (Password password : passwordList) {
                String passwordStr = Decryptor.decryptPassword(password.getCipher().trim(), passphrase);
                password.setCipher(Encryptor.encrypt(passwordStr, passphrase));

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

    public void getAccountDetails(GoogleSignInAccount account) {
        Query emailQuery = reference.child(DB_REF).equalTo(account.getEmail());

        emailQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChildren())
                    emailExists.setValue(true);
                else
                    reference.child(DB_REF).child(account.getEmail()).push();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void createKeyStore(String passphrase) {
        SecretKey key = null;
        try {
            KeyStore keyStore = getKeyStore();
            if (keyStore != null) {
                KeyStore.SecretKeyEntry entry =
                        (KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null);
                if (entry != null)
                    key = entry.getSecretKey();
                else
                    key = createSecretKey(passphrase);
            } else {
                key = createSecretKey(passphrase);
            }
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException |
                 InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException |
                 InvalidKeySpecException e) {

            if (e instanceof NoSuchAlgorithmException)
                throw new IllegalStateException("The algorithm specified is not correct");
            else if (e instanceof UnrecoverableEntryException)
                throw new IllegalStateException("No KeyStore for this application");
            else if (e instanceof InvalidAlgorithmParameterException)
                throw new IllegalStateException("Invalid algorithm parameter");
            else if (e instanceof NoSuchProviderException)
                throw new IllegalStateException("No Such Provider");
            else throw new IllegalStateException("Key store exception.");
        }
    }

    private SecretKey createSecretKey(String passphrase) throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeySpecException {

        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore");
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build();
        keyGenerator.init(spec);

        return keyGenerator.generateKey();

//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        PBEKeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, 1000, 2048);
//        return factory.generateSecret(spec);

//        KeyPairGenerator kpg = null;
//        try {
//            Calendar start = Calendar.getInstance();
//            Calendar end = Calendar.getInstance();
//            end.add(Calendar.YEAR, 1);
//
//            kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
//
//            RSAKeyGenParameterSpec keyGenParameterSpec = new RSAKeyGenParameterSpec().Builder(
//                    ALIAS,
//                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setKeySize(2048)
//                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//                    .build();
//            kpg.initialize(keyGenParameterSpec);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return kpg.generateKeyPair().getPublic();
    }

    private KeyStore getKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException e) {
            Log.e(TAG, "getKeyStore: ", e);
        } catch (NoSuchAlgorithmException | IOException | CertificateException e) {
            e.printStackTrace();
        }
        return keyStore;
    }
}
