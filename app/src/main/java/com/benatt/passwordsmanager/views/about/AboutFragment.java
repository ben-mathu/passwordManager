package com.benatt.passwordsmanager.views.about;

import static com.benatt.passwordsmanager.utils.Constants.APP_PURCHASED;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentAboutBinding;
import com.benatt.passwordsmanager.utils.billing.BillingManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AboutFragment extends Fragment implements View.OnClickListener {

    @Inject
    BillingManager billingManager;

    @Inject
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);

        binding.btnUnlockPremium.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_unlock_premium) {
            billingManager.launchBillingFlow(requireActivity(), this::handleBillingResult);
        }
    }

    private void handleBillingResult(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            preferences.edit().putBoolean(APP_PURCHASED, true).apply();
        }
    }
}
