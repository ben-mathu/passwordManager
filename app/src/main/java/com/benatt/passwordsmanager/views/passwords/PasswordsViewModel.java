package com.benatt.passwordsmanager.views.passwords;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.benatt.passwordsmanager.utils.Encryptor;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;

/**
 * @author bernard
 */
public class PasswordsViewModel extends ViewModel {
    private PublicKey publicKey;
    public MutableLiveData<String> encryptedString = new MutableLiveData<>();

    public PasswordsViewModel(
            PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void encryptPasswordData(String passwords) {
        try {
            String cipher = Encryptor.encrypt(publicKey, passwords);
            encryptedString.setValue(cipher);
        } catch (BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
