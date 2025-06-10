package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.utils.AppUtil.readAppDescription;
import static com.benatt.passwordsmanager.utils.Constants.APP_PURCHASED;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentProModeBinding;
import com.benatt.passwordsmanager.utils.billing.BillingCallback;
import com.benatt.passwordsmanager.utils.billing.BillingManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProModeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = ProModeFragment.class.getSimpleName();

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
            billingManager.launchBillingFlow(requireActivity(), new BillingCallback() {
                @Override
                public void onPurchasesUpdated(BillingResult billingResult) {
                    handleBillingResult(billingResult);
                }

                @Override
                public void productPurchased() {
                    preferences.edit().putBoolean(APP_PURCHASED, true).apply();
                }
            });
        }
    }

    private void handleBillingResult(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            preferences.edit().putBoolean(APP_PURCHASED, true).apply();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "handleBillingResult -> ", e);
            }
            NavHostFragment.findNavController(this).navigateUp();
        }
    }
}
