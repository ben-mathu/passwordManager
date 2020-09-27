package com.benatt.passwords.views.passwords.adapter;

import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwords.R;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.databinding.PasswordItemBinding;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class PasswordsViewHolder extends RecyclerView.ViewHolder {

    @Inject
    PasswordItemViewModel passwordItemViewModel;
    private PasswordItemBinding binding;

    private boolean isDecrypted = false;

    public PasswordsViewHolder(PasswordItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Password password) {
        binding.setPasswordItemViewModel(passwordItemViewModel);
//        binding.btnDecrypt.setOnClickListener(view -> {
//            if (isDecrypted) {
//                passwordItemViewModel.hide();
//                binding.btnDecrypt.setText(R.string.decrypt);
//            } else {
//                passwordItemViewModel.decrypt(password.getCipher());
//                binding.btnDecrypt.setText(R.string.encrypt);
//            }
//        });
        passwordItemViewModel.bind(password);
    }
}
