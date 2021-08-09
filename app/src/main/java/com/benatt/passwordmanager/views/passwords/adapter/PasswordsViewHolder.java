package com.benatt.passwordmanager.views.passwords.adapter;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.CountDownTimer;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordmanager.R;
import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.databinding.PasswordItemBinding;
import com.benatt.passwordmanager.utils.OnActivityResult;
import com.benatt.passwordmanager.views.passwords.OnItemClick;

import static com.benatt.passwordmanager.utils.Decryptor.decryptPassword;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder{
    public static final int REQUEST_CODE = 1101;
    public static final String TAG = PasswordsViewHolder.class.getSimpleName();

    private KeyguardManager keyguardManager;

    private OnActivityResult onActivityResult;

    private PasswordItemViewModel passwordItemViewModel = new PasswordItemViewModel();
    private PasswordItemBinding binding;
    private Activity context;

    private Password password;

    private boolean isDecrypted = false;

    public PasswordsViewHolder(PasswordItemBinding binding, Activity context) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
    }

    public void bind(Password password, OnItemClick onItemClick) {
        this.password = password;

        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setText(R.string.show_password);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isDecrypted) {
                onItemClick.startKeyguardActivity(new OnActivityResult() {
                    @Override
                    public void onResultReturned() {
                        binding.passwordValue.setText(decryptPassword(password.getCipher()));
                        binding.btnDecrypt.setText(R.string.hide_password);
                        isDecrypted = true;


                        startTimer();
                    }
                });
            } else {
                binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                binding.btnDecrypt.setText(context.getString(R.string.show_password));
                isDecrypted = false;
            }
        });

        binding.getRoot().setOnClickListener(view -> onItemClick.onItemClick(password));

        passwordItemViewModel.bind(password);
    }

    private void startTimer() {
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG, "onTick: Intentionally left blank");
            }

            @Override
            public void onFinish() {
                binding.passwordValue.setText(context.getString(R.string.password_encrypted));
                binding.btnDecrypt.setText(context.getString(R.string.show_password));
                isDecrypted = false;
            }
        }.start();
    }

    public OnActivityResult getOnActivityResult() {
        return onActivityResult;
    }
}
