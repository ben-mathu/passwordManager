package com.benatt.passwords.views.passwords;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author bernard
 */
public class PasswordsViewModel extends ViewModel {
    private KeyStore keyStore;
    private List<String> aliases;

    MutableLiveData<String> msgEmpty = new MutableLiveData<>();
    MutableLiveData<List<String>> keyAliases = new MutableLiveData<>();

    public PasswordsViewModel(KeyStore keyStore) {

        this.keyStore = keyStore;
    }

    public void refreshKeys() {
        aliases = new ArrayList<>();
        try {
            Enumeration<String> aliasKeys = keyStore.aliases();
            while (aliasKeys.hasMoreElements()) {
                aliases.add(aliasKeys.nextElement());
            }

            if (aliases.isEmpty()) {
                msgEmpty.setValue("Empty List");
            } else {
                keyAliases.setValue(aliases);
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}
