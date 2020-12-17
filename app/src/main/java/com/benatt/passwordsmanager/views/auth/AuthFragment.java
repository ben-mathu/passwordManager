package com.benatt.passwordsmanager.views.auth;

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
import androidx.navigation.fragment.NavHostFragment;

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentAuthBinding;

import static android.app.Activity.RESULT_OK;

/**
 * @author bernard
 */
public class AuthFragment extends Fragment {
    public static final int RESULT_CODE = 1101;
    private KeyguardManager keyguardManager;

    private FragmentAuthBinding binding;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                NavHostFragment.findNavController(this).navigate(R.id.action_authentication_to_password_list);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                    getString(R.string.auth_key_guard),
                    getString(R.string.auth_msg)
            );
            startActivityForResult(intent, RESULT_CODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
