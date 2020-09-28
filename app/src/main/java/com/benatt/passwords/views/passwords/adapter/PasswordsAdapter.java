package com.benatt.passwords.views.passwords.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwords.R;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.databinding.PasswordItemBinding;
import com.benatt.passwords.utils.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsViewHolder> {
    private PasswordItemBinding binding;
    private List<Password> passwords;

    @Inject
    ViewModelFactory viewModelFactory;

    private PasswordItemViewModel passwordItemViewModel;

    public PasswordsAdapter() {
        passwords = new ArrayList<>();
    }

    @NonNull
    @Override
    public PasswordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.password_item,
                parent, false);
        return new PasswordsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordsViewHolder holder, int position) {
        holder.bind(passwords.get(position));
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public void setPasswords(List<Password> passwords) {
        this.passwords = passwords;
        notifyDataSetChanged();
    }
}
