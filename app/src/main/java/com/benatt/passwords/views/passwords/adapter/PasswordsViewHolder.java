package com.benatt.passwords.views.passwords.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwords.R;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.databinding.PasswordItemBinding;
import com.benatt.passwords.views.passwords.OnItemClick;

import static com.benatt.passwords.utils.Constants.DELIMITER;
import static com.benatt.passwords.utils.Decryptor.decryptPassword;

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

    public void bind(Password password, OnItemClick onItemClick) {
        binding.setPasswordItemViewModel(passwordItemViewModel);
        binding.btnDecrypt.setText(R.string.show_password);
        binding.btnDecrypt.setOnClickListener(view -> {
            if (!isShowingPassword) {
                String cipher =
                        password.getCipher().contains(DELIMITER) ?
                        password.getCipher().split(DELIMITER)[1] :
                        password.getCipher();
                binding.passwordValue.setText(cipher);
                binding.btnDecrypt.setText(R.string.decrypt);
                isShowingPassword = true;
            } else if (isDecrypted) {
                binding.passwordValue.setText(R.string.password_encrypted);
                isDecrypted = false;
                isShowingPassword = false;
            } else {
                binding.passwordValue.setText(decryptPassword(password));
                binding.btnDecrypt.setText(R.string.hide_password);
                isDecrypted = true;
            }
        });

        binding.getRoot().setOnClickListener(view -> {
            onItemClick.onItemClick(password);
        });

        passwordItemViewModel.bind(password);
    }
}
