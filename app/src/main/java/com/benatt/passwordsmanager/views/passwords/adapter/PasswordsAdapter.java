package com.benatt.passwordsmanager.views.passwords.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.PasswordItemBinding;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.benatt.passwordsmanager.views.passwords.OnItemClick;

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
    private final OnItemClick onItemClick;

    public PasswordsAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
        passwords = new ArrayList<>();
    }

    @NonNull
    @Override
    public PasswordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = PasswordItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false);
        return new PasswordsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordsViewHolder holder, int position) {
        holder.bind(passwords.get(position), onItemClick);
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
