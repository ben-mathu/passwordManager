package com.benatt.passwords.views.passwords.adapter;

import android.util.Base64;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwords.MainApp;
import com.benatt.passwords.R;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.databinding.PasswordItemBinding;
import com.benatt.passwords.utils.Encryptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.inject.Inject;

import static com.benatt.passwords.utils.Constants.ALIAS;
import static com.benatt.passwords.utils.Constants.INITIALIZATION_VECTOR;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder {
    public static final String TAG = PasswordsViewHolder.class.getSimpleName();

    private PasswordItemViewModel passwordItemViewModel = new PasswordItemViewModel();
    private PasswordItemBinding binding;

    private boolean isDecrypted = false;
    private boolean isShowingPassword = false;

    public PasswordsViewHolder(PasswordItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Password password) {
        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setText(R.string.show_password);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isShowingPassword) {
                binding.passwordValue.setText(password.getCipher());
                binding.btnDecrypt.setText(R.string.decrypt);
                isShowingPassword = true;
            } else if (isDecrypted) {
                binding.passwordValue.setText(R.string.password_encrypted);
                isDecrypted = false;
                isShowingPassword = false;
            } else {
                decryptPassword(password);
                binding.btnDecrypt.setText(R.string.hide_password);
                isDecrypted = true;
            }
        });
        passwordItemViewModel.bind(password);
    }

    public void decryptPassword(Password password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(ALIAS, null);
            SecretKey secretKey = secretKeyEntry.getSecretKey();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] passwordStr = Base64.decode(password.getCipher(), Base64.DEFAULT);
            String ivStr = MainApp.getPreferences().getString(INITIALIZATION_VECTOR, "");
            byte[] iv = Base64.decode(ivStr, Base64.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            binding.passwordValue.setText(new String(cipher.doFinal(passwordStr), StandardCharsets.UTF_8));
        } catch (KeyStoreException | UnrecoverableEntryException | BadPaddingException | NoSuchAlgorithmException | CertificateException | InvalidKeyException | InvalidAlgorithmParameterException | NoSuchPaddingException | IOException | IllegalBlockSizeException e) {
            Log.e(TAG, "decryptPassword: ", e);
        }
    }
}
