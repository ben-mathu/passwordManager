package com.benatt.passwordsmanager.views.passwords.adapter;

import static com.benatt.passwordsmanager.utils.Decryptor.decryptPassword;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.PasswordItemBinding;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.views.passwords.OnItemClick;
import com.google.android.material.button.MaterialButton;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder {
    public static final int REQUEST_CODE = 1101;
    public static final int START_PASSWORD_DETAIL_SCREEN = 1102;
    public static final String TAG = PasswordsViewHolder.class.getSimpleName();

    private final PasswordItemViewModel passwordItemViewModel = new PasswordItemViewModel();
    private final PasswordItemBinding binding;
    private final Activity context;

    private boolean isDecrypted;
    private MaterialButton btnDecrypt;
    Timer timer = new Timer();

    public PasswordsViewHolder(PasswordItemBinding binding, Activity context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
        this.isDecrypted = false;
    }

    public void bind(Password password, OnItemClick onItemClick) {
        btnDecrypt = (MaterialButton) binding.btnDecrypt;

        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isDecrypted) {
                onItemClick.startKeyguardActivity(() -> {
                    try {
                        String cipher = decryptPassword(password.getCipher(), null, BuildConfig.ALIAS);
                        binding.passwordValue.setText(String.format("Password: %s", cipher));
                        btnDecrypt.setIcon(
                                ContextCompat.getDrawable(context, R.drawable.ic_action_encrypted_off));
                        isDecrypted = true;

                        startTimer();
                    } catch (Exception e) {
                        Log.e(TAG, "bind: Error", e);
                    }
                }, REQUEST_CODE);
            } else {
                binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                btnDecrypt.setIcon(
                        ContextCompat.getDrawable(context, R.drawable.ic_action_encrypted_on));
                isDecrypted = false;
            }
        });

        binding.btnCopy.setOnClickListener(v -> {
            try {
                ClipboardManager cm =
                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData
                        .newPlainText("password",
                                decryptPassword(password.getCipher(), null, BuildConfig.ALIAS));
                cm.setPrimaryClip(clip);

                MaterialButton btnCopy = (MaterialButton) binding.btnCopy;
                btnCopy.setIcon(
                        ContextCompat.getDrawable(context, R.drawable.ic_action_check));

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        context.runOnUiThread(() -> btnCopy.setIcon(
                                ContextCompat.getDrawable(context, R.drawable.ic_action_copy)));
                    }
                }, 3000);
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "bind: Error", e);
            }
        });

        binding.getRoot().setOnClickListener(view -> onItemClick.onItemClick(password));

        passwordItemViewModel.bind(password, this.context);
    }

    private void startTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                context.runOnUiThread(() -> {
                    binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                    btnDecrypt.setIcon(
                            ContextCompat.getDrawable(context, R.drawable.ic_action_encrypted_on));
                    isDecrypted = false;
                });
            }
        }, 60000);
    }
}
