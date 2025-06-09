package com.benatt.passwordsmanager.views.passwords;

import static android.app.Activity.RESULT_OK;
import static com.benatt.passwordsmanager.utils.Constants.EDIT_PASSWORD;
import static com.benatt.passwordsmanager.views.passwords.adapter.PasswordsViewHolder.REQUEST_CODE;
import static com.benatt.passwordsmanager.views.passwords.adapter.PasswordsViewHolder.START_PASSWORD_DETAIL_SCREEN;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.FragmentPasswordsBinding;
import com.benatt.passwordsmanager.utils.OnActivityResult;
import com.benatt.passwordsmanager.views.SharedViewModel;
import com.benatt.passwordsmanager.views.passwords.adapter.PasswordsAdapter;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * @author bernard
 */
@AndroidEntryPoint
public class PasswordsFragment extends Fragment implements OnItemClick {
    private NavController controller;

    private SharedViewModel sharedViewModel;

    private FragmentPasswordsBinding binding;

    private PasswordsAdapter adapter;
    private OnActivityResult onActivityResult;
    private Password password;

    @Override
    public void onStart() {
        super.onStart();

        sharedViewModel.getPasswords();
        sharedViewModel.showLoader.postValue(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPasswordsBinding.inflate(inflater, container, false);

        controller = NavHostFragment.findNavController(this);

        PasswordsViewModel passwordsViewModel = new ViewModelProvider(this).get(PasswordsViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        sharedViewModel.errorMsg.observe(getViewLifecycleOwner(), s -> {
            showMessage(s, binding.getRoot());
            binding.rvPasswordList.setVisibility(View.GONE);
            binding.llPlaceholder.setVisibility(View.VISIBLE);
            sharedViewModel.showLoader.postValue(false);
        });

        adapter = new PasswordsAdapter(this, requireActivity());
        binding.rvPasswordList.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.rvPasswordList.setAdapter(adapter);

        sharedViewModel.passwords.observe(getViewLifecycleOwner(), passwords -> {
            adapter.setPasswords(passwords);
            sharedViewModel.showLoader.postValue(false);
            sharedViewModel.showBottomNav();

            if (!passwords.isEmpty() && binding.rvPasswordList.getVisibility() == View.GONE) {
                binding.rvPasswordList.setVisibility(View.VISIBLE);
                binding.llPlaceholder.setVisibility(View.GONE);
            }
        });

        sharedViewModel.refreshList.observe(getViewLifecycleOwner(), isRefreshList -> {
            if (isRefreshList)
                sharedViewModel.getPasswords();
        });

        binding.btnAddPassword.setOnClickListener(
                view -> NavHostFragment.findNavController(this)
                        .navigate(R.id.fragment_add_password));

        binding.setPasswordsViewModel(passwordsViewModel);
        return binding.getRoot();
    }

    private void showMessage(String s, View rootView) {
        Snackbar.make(rootView, s, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Password password) {
        this.password = password;
        startKeyguardActivity(null, START_PASSWORD_DETAIL_SCREEN);
    }

    @Override
    public void startKeyguardActivity(OnActivityResult onActivityResult, int requestCode) {
        this.onActivityResult = onActivityResult;
        KeyguardManager keyguardManager = (KeyguardManager) requireActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent =  keyguardManager.createConfirmDeviceCredentialIntent(
                    requireActivity().getString(R.string.auth_key_guard),
                    requireActivity().getString(R.string.auth_msg)
            );
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK && this.onActivityResult != null) {
                this.onActivityResult.onResultReturned();
            }
        } else if (requestCode == START_PASSWORD_DETAIL_SCREEN) {
            if (resultCode == RESULT_OK) {
                Bundle args = new Bundle();
                args.putParcelable(EDIT_PASSWORD, password);
                controller.navigate(R.id.fragment_add_password, args);
            }
        }
    }
}
