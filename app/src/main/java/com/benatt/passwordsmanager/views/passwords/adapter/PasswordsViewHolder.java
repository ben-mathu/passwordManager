package com.benatt.passwordsmanager.views.passwords.adapter;

import static com.benatt.passwordsmanager.utils.Decryptor.decryptPassword;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.PasswordItemBinding;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.OnActivityResult;
import com.benatt.passwordsmanager.views.passwords.OnItemClick;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder{
    public static final int REQUEST_CODE = 1101;
    public static final int START_PASSWORD_DETAIL_SCREEN = 1102;
    public static final String TAG = PasswordsViewHolder.class.getSimpleName();

    private OnActivityResult onActivityResult;

    private final PasswordItemViewModel passwordItemViewModel = new PasswordItemViewModel();
    private final PasswordItemBinding binding;
    private final Activity context;

    private boolean isDecrypted;

    public PasswordsViewHolder(PasswordItemBinding binding, Activity context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
        this.isDecrypted = false;
    }

    public void bind(Password password, OnItemClick onItemClick) {

        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setText(R.string.show_password);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isDecrypted) {
                onItemClick.startKeyguardActivity(() -> {
                    try {
                        String cipher = decryptPassword(password.getCipher(), null);
                        binding.passwordValue.setText(cipher);
                        binding.btnDecrypt.setText(R.string.hide_password);
                        binding.lockPassword.setImageDrawable(
                                context.getResources().getDrawable(R.drawable.ic_unlocked_password)
                        );
                        binding.lockPassword.setColorFilter(
                                context.getResources().getColor(R.color.colorAccent));
                        isDecrypted = true;

                        startTimer();
                    } catch (Exception e) {
                        Log.e(TAG, "bind: Error", e);
                    }
                }, REQUEST_CODE);
            } else {
                binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                binding.btnDecrypt.setText(context.getString(R.string.show_password));
                binding.lockPassword.setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_locked_password)
                );
                isDecrypted = false;
            }
        });

        binding.btnCopy.setOnClickListener(v -> {
            try {
                ClipboardManager cm =
                        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clip = ClipData
                        .newPlainText("password",
                                decryptPassword(password.getCipher(), null));
                cm.setPrimaryClip(clip);
                Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, "bind: Error", e);
            }
        });

        binding.getRoot().setOnClickListener(view -> onItemClick.onItemClick(password));

        passwordItemViewModel.bind(password, this.context);
    }

    private void startTimer() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // onTick: Intentionally left blank
            }

            @Override
            public void onFinish() {
                binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                binding.btnDecrypt.setText(context.getString(R.string.show_password));
                binding.lockPassword.setImageDrawable(
                        context.getResources().getDrawable(R.drawable.ic_locked_password)
                );
                isDecrypted = false;
            }
        }.start();
    }

    public OnActivityResult getOnActivityResult() {
        return onActivityResult;
    }
}
