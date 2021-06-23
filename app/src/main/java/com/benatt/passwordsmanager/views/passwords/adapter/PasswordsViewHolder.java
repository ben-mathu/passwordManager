package com.benatt.passwordsmanager.views.passwords.adapter;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.PasswordItemBinding;
import com.benatt.passwordsmanager.utils.OnActivityResult;
import com.benatt.passwordsmanager.views.passwords.OnItemClick;

import static com.benatt.passwordsmanager.utils.Constants.DELIMITER;
import static com.benatt.passwordsmanager.utils.Decryptor.decryptPassword;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder implements OnActivityResult {
    public static final int RESULT_CODE = 1101;
    public static final String TAG = PasswordsViewHolder.class.getSimpleName();

    private KeyguardManager keyguardManager;

    private OnActivityResult onActivityResult;

    private PasswordItemViewModel passwordItemViewModel = new PasswordItemViewModel();
    private PasswordItemBinding binding;
    private Activity context;

    private Password password;

    private boolean isDecrypted = false;
    private boolean isShowingPassword = false;

    public PasswordsViewHolder(PasswordItemBinding binding, Activity context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
        this.onActivityResult = this;
    }

    public void bind(Password password, OnItemClick onItemClick) {
        this.password = password;

        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setText(R.string.show_password);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isDecrypted) {
                onItemClick.startKeyguardActivity();
            } else {
                onResultReturned();
            }
        });

        binding.getRoot().setOnClickListener(view -> onItemClick.onItemClick(password));

        passwordItemViewModel.bind(password);
    }

    @Override
    public void onResultReturned() {
        if (isDecrypted) {
            binding.passwordValue.setText(R.string.password_encrypted);
            isDecrypted = false;
            isShowingPassword = false;
        } else {
            binding.passwordValue.setText(decryptPassword(password));
            binding.btnDecrypt.setText(R.string.hide_password);
            isDecrypted = true;
        }
    }

    public OnActivityResult getOnActivityResult() {
        return onActivityResult;
    }
}
