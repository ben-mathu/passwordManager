package com.benatt.passwordsmanager.views.home;

import static com.benatt.passwordsmanager.utils.Constants.APP_PURCHASED;
import static com.benatt.passwordsmanager.utils.Constants.PASSWORD_LIMIT;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentHomeBinding;
import com.benatt.passwordsmanager.views.SharedViewModel;

import java.util.Collections;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * @author bernard
 */
@AndroidEntryPoint
public class HomeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private NavController controller;
    private SharedViewModel sharedViewModel;

    @Inject
    SharedPreferences preferences;

    private boolean isPaid;
    private FragmentHomeBinding binding;
    private int count;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        controller = NavHostFragment.findNavController(this);

        viewModel.countLiveData.observe(getViewLifecycleOwner(), count -> this.count = count);

        binding.tvVersion.setText(String.format("Version %s", BuildConfig.VERSION_NAME));
        binding.btnShowPassword.setOnClickListener(this);
        binding.btnBackup.setOnClickListener(this);
        binding.btnRestore.setOnClickListener(this);
        binding.btnAbout.setOnClickListener(this);
        binding.btnProMode.setOnClickListener(this);
        binding.btnLearnMore.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        //    public static final int RESULT_CODE = 1101;
        KeyguardManager keyguardManager = (KeyguardManager) requireActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            showDialog();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        isPaid = preferences.getBoolean(APP_PURCHASED, false);
        String format = "%d";
        if (!isPaid) format = "%d/%d";

        if (isPaid) {
            binding.btnProMode.setVisibility(View.GONE);
            binding.btnLearnMore.setVisibility(View.VISIBLE);
        } else {
            binding.btnProMode.setVisibility(View.VISIBLE);
            binding.btnLearnMore.setVisibility(View.GONE);
        }

        viewModel.getPasswordsCount();
        binding.passwordCountTv.setText(String.format(format, count, PASSWORD_LIMIT));
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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_show_password) {
            controller.navigate(R.id.action_homeFragment_to_passwordFragment);
        } else if (view.getId() == R.id.btn_backup) {
            sharedViewModel.createBackup(requireActivity(), Collections.emptyList());
        } else if (view.getId() == R.id.btn_restore) {
            restorePasswords();
        } else if (view.getId() == R.id.btn_about || view.getId() == R.id.btn_learn_more) {
            controller.navigate(R.id.action_homeFragment_to_aboutFragment);
        } else if (view.getId() == R.id.btn_pro_mode) {
            controller.navigate(R.id.action_homeFragment_to_proFragment);
        } else {
            throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    public void restorePasswords() {
        sharedViewModel.showLoader.postValue(true);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        retrieveFileLauncher.launch(Intent.createChooser(intent, "Select a file"));
    }

    private final ActivityResultLauncher<Intent> retrieveFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Log.d(TAG, "Successfully retrieved file -> ");
                    Uri contentUri = result.getData().getData();
                    if (contentUri != null) {
                        sharedViewModel.restorePasswords(requireActivity(), contentUri);
                    }
                } else Log.e(TAG, "Could not retrieve file -> ");
            });
}
