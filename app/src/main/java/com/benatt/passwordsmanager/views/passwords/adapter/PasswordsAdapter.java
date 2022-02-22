package com.benatt.passwordsmanager.views.passwords.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.benatt.passwordsmanager.R;
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

    private boolean isDecrypted = false;

    @Inject
    ViewModelFactory viewModelFactory;

    private PasswordsViewHolder viewHolder;

    private PasswordItemViewModel passwordItemViewModel;
    private final OnItemClick onItemClick;
    private Activity context;

    public PasswordsAdapter(OnItemClick onItemClick, Activity context) {
        this.onItemClick = onItemClick;
        this.context = context;
        passwords = new ArrayList<>();
    }

    @NonNull
    @Override
    public PasswordsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = PasswordItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent, false);

        Typeface typeface = ResourcesCompat.getFont(context, R.font.roboto_serif_bold);
        binding.passwordKey.setTypeface(typeface);

        typeface = ResourcesCompat.getFont(context, R.font.roboto_serif_regular);
        binding.passwordValue.setTypeface(typeface);

        typeface = ResourcesCompat.getFont(context, R.font.roboto_serif_regular);
        binding.btnDecrypt.setTypeface(typeface);
        return this.viewHolder = new PasswordsViewHolder(binding, context);
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

    public void showPassword() {
        this.viewHolder.getOnActivityResult().onResultReturned();
    }
}
