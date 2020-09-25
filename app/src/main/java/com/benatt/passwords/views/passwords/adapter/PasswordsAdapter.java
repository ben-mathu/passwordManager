package com.benatt.passwords.views.passwords.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * @author bernard
 */
public class PasswordsAdapter extends RecyclerView.Adapter<PasswordsViewHolder> {
    private List<String> keyAliases;

    public PasswordsAdapter(List<String> keyAliases) {
        this.keyAliases = keyAliases;
    }

    @NonNull
    @Override
    public PasswordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordsViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return keyAliases.size();
    }
}
