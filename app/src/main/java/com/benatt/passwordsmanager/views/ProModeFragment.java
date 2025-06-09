package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.utils.AppUtil.readAppDescription;
import static com.benatt.passwordsmanager.utils.Constants.APP_PURCHASED;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentProModeBinding;
import com.benatt.passwordsmanager.utils.billing.BillingManager;

import javax.inject.Inject;

public class ProModeFragment extends Fragment implements View.OnClickListener {
    @Inject
    BillingManager billingManager;

    @Inject
    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentProModeBinding binding = FragmentProModeBinding.inflate(inflater, container, false);
        String htmlText = readAppDescription(requireActivity(), R.raw.pro_mode);
        if (htmlText != null) {
            Spanned spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY);
            binding.proModeText.setText(spanned);
        }
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
