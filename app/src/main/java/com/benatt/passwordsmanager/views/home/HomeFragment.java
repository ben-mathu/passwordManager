package com.benatt.passwordsmanager.views.home;

import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN_WITH_GOOGLE;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentHomeBinding;
import com.benatt.passwordsmanager.views.SharedViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * @author bernard
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private NavController controller;

    @Inject
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        FragmentHomeBinding binding = FragmentHomeBinding.inflate(inflater, container, false);

        controller = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        //    public static final int RESULT_CODE = 1101;
        KeyguardManager keyguardManager = (KeyguardManager) requireActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            showDialog();
            return;
        }

        boolean isLoggedIn = preferences.getBoolean(SIGNED_IN_WITH_GOOGLE, false);
        if (isLoggedIn) controller.navigate(R.id.action_authentication_to_password_list);
    }

    private void showDialog() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(requireActivity(),
                android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle("Attention");
        builder.setMessage("Please secure your device before using this app");
        builder.setCancelable(false);
        builder.setPositiveButton("ok", ((dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            requireActivity().startActivity(intent);
        }));
        builder.show();
    }
}
