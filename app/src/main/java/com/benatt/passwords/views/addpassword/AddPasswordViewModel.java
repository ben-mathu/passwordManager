package com.benatt.passwords.views.addpassword;

import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyPairGeneratorSpi;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Calendar;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

/**
 * @author bernard
 */
public class AddPasswordViewModel extends ViewModel {
    public static final String TAG = AddPasswordViewModel.class.getSimpleName();
    private KeyStore keyStore;

    private final MutableLiveData<String> msgView= new MutableLiveData<>();

    @Inject
    public AddPasswordViewModel(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void savePassword(String password, String alias, KeyPairGeneratorSpec spec) {
        if (password.isEmpty())
            msgView.setValue("Password is empty");
        else {
            try {
                if (!keyStore.containsAlias(alias)) {
                    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                    generator.initialize(spec);

                    KeyPair keyPair = generator.generateKeyPair();
                }
            } catch (KeyStoreException e) {
                Log.e(TAG, "savePassword: Error " + e.getMessage() + "\n", e);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
    }
}
