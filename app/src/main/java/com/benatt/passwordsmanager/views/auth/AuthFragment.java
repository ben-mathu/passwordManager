package com.benatt.passwordsmanager.views.auth;

import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN_WITH_GOOGLE;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentAuthBinding;
import com.benatt.passwordsmanager.views.SharedViewModel;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class AuthFragment extends Fragment {
//    public static final int RESULT_CODE = 1101;
    private KeyguardManager keyguardManager;

    private FragmentAuthBinding binding;

    private SharedViewModel sharedViewModel;
    private NavController controller;

    @Override
    public void onStart() {
        super.onStart();

        this.keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            showDialog(binding.getRoot());
            return;
        }

        boolean isLoggedIn = MainApp.getPreferences().getBoolean(SIGNED_IN_WITH_GOOGLE, false);
        if (isLoggedIn) controller.navigate(R.id.action_authentication_to_password_list);
    }

    private void showDialog(View rootView) {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getActivity(),
                android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle("Attention");
        builder.setMessage("Please secure your device before using this app");
        builder.setCancelable(false);
        builder.setPositiveButton("ok", ((dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            getActivity().startActivity(intent);
        }));
        builder.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        binding = FragmentAuthBinding.inflate(inflater, container, false);

        controller = NavHostFragment.findNavController(this);

        binding.googleSignIn.setOnClickListener(view -> {
            sharedViewModel.isLogin.setValue(true);
        });

        binding.tvSkip.setOnClickListener(view -> {
            controller.navigate(R.id.fragment_passwords);
        });
        return binding.getRoot();
    }
}
