package com.benatt.passwordsmanager.views.passwords;

import static com.benatt.passwordsmanager.utils.Constants.USER_PASSPHRASE;

import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.utils.Encryptor;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

/**
 * @author bernard
 */
public class PasswordsViewModel extends ViewModel {
    private final SharedPreferences preferences;
    public MutableLiveData<String> encryptedString = new MutableLiveData<>();

    @Inject
    public PasswordsViewModel() {
        this.preferences = MainApp.getPreferences();
    }

    public void encryptPasswordData(String passwords) {
        try {
            String passphrase = preferences.getString(USER_PASSPHRASE, "");
            String cipher = Encryptor.encrypt(passwords, passphrase);
            encryptedString.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 NoSuchAlgorithmException | InvalidKeyException | KeyStoreException |
                 UnrecoverableEntryException | CertificateException | IOException |
                 NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
}
