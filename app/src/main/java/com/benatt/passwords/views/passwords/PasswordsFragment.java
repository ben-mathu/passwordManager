package com.benatt.passwords.views.passwords;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.benatt.passwords.MainApp;
import com.benatt.passwords.R;
import com.benatt.passwords.databinding.FragmentPasswordsBinding;
import com.benatt.passwords.utils.ViewModelFactory;
import com.benatt.passwords.views.passwords.adapter.PasswordsAdapter;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class PasswordsFragment extends Fragment {
    private PasswordsViewModel passwordsViewModel;

    @Inject
    ViewModelFactory factory;

    private FragmentPasswordsBinding binding;

    private PasswordsAdapter adapter;

    @Override
    public void onStart() {
        super.onStart();

        passwordsViewModel.getPasswords();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainApp) getActivity().getApplicationContext()).getPasswordsComponent().inject(this);
        binding = FragmentPasswordsBinding.inflate(inflater, container, false);

        passwordsViewModel = new ViewModelProvider(this, factory).get(PasswordsViewModel.class);

        passwordsViewModel.msgEmpty.observe(getViewLifecycleOwner(), s -> {
            showMessage(s, binding.getRoot());
            binding.rvPasswordList.setVisibility(View.GONE);
            binding.llPlaceholder.setVisibility(View.VISIBLE);
        });

        adapter = new PasswordsAdapter();
        binding.rvPasswordList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPasswordList.setAdapter(adapter);

        passwordsViewModel.passwords.observe(getViewLifecycleOwner(), passwords -> {
            adapter.setPasswords(passwords);
        });

        binding.btnAddPassword.setOnClickListener(view -> {
            NavHostFragment.findNavController(this).navigate(R.id.fragment_add_password);
        });

        binding.setPasswordsViewModel(passwordsViewModel);
        return binding.getRoot();
    }

    private void showMessage(String s, View rootView) {
        assert getActivity() != null;
        assert getActivity().getCurrentFocus() != null;
        Snackbar.make(rootView, s, Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        passwordsViewModel.unsubscribe();
    }
}
