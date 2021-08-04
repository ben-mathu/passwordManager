package com.benatt.passwordmanager.views.passwords;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.benatt.passwordmanager.MainApp;
import com.benatt.passwordmanager.R;
import com.benatt.passwordmanager.data.models.passwords.model.Password;
import com.benatt.passwordmanager.databinding.FragmentPasswordsBinding;
import com.benatt.passwordmanager.utils.OnActivityResult;
import com.benatt.passwordmanager.utils.ViewModelFactory;
import com.benatt.passwordmanager.views.passwords.adapter.PasswordsAdapter;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.benatt.passwordmanager.utils.Constants.EDIT_PASSWORD;
import static com.benatt.passwordmanager.views.passwords.adapter.PasswordsViewHolder.REQUEST_CODE;

/**
 * @author bernard
 */
public class PasswordsFragment extends Fragment implements OnItemClick {
    private static final String TAG = PasswordsFragment.class.getSimpleName();
    private static final String PASSWORD_POS = "position";

    private PasswordsViewModel passwordsViewModel;

    @Inject
    ViewModelFactory factory;

    private FragmentPasswordsBinding binding;

    private PasswordsAdapter adapter;
    private OnActivityResult onActivityResult;

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

        adapter = new PasswordsAdapter(this, getActivity());
        binding.rvPasswordList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPasswordList.setAdapter(adapter);

        passwordsViewModel.passwords.observe(getViewLifecycleOwner(), passwords -> adapter.setPasswords(passwords));

        binding.btnAddPassword.setOnClickListener(
                view -> NavHostFragment.findNavController(this)
                        .navigate(R.id.fragment_add_password));

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

    @Override
    public void onItemClick(Password password) {
        Bundle args = new Bundle();
        args.putParcelable(EDIT_PASSWORD, password);
        NavHostFragment.findNavController(this).navigate(R.id.fragment_add_password, args);
    }

    @Override
    public void startKeyguardActivity(OnActivityResult onActivityResult) {
        this.onActivityResult = onActivityResult;
        KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent =  keyguardManager.createConfirmDeviceCredentialIntent(
                    getActivity().getString(R.string.auth_key_guard),
                    getActivity().getString(R.string.auth_msg)
            );
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.onActivityResult.onResultReturned();
            }
        }
    }
}
