package com.benatt.core.billing;

import static com.benatt.core.utils.Constants.APP_PURCHASED;
import static com.benatt.core.utils.Constants.PRODUCT_ID;
import static com.benatt.core.utils.Constants.UI_CONTENT;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.benatt.core.R;
import com.benatt.core.databinding.FragmentProModeBinding;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProModeFragment extends Fragment implements OnClickListener {
    private static final String TAG = ProModeFragment.class.getSimpleName();

    private String productId;

    @Inject
    BillingManager billingManager;

    @Inject
    SharedPreferences preferences;
    private String htmlText;

    public static ProModeFragment newInstance(String productId, String text) {
        if ((productId == null || productId.isBlank()) && text == null || text.isBlank())
            throw new IllegalArgumentException("Product ID cannot be null");

        ProModeFragment proModeFragment = new ProModeFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PRODUCT_ID, productId);
        bundle.putString(UI_CONTENT, text);
        proModeFragment.setArguments(bundle);
        return proModeFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(PRODUCT_ID);
            htmlText = getArguments().getString(UI_CONTENT);
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        FragmentProModeBinding binding =
            FragmentProModeBinding.inflate(inflater, container, false);
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
            billingManager.launchBillingFlow(requireActivity(), billingCallback, productId);
        }
    }

    BillingCallback billingCallback = new BillingCallback() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult) {
            handleBillingResult(billingResult);
        }

        @Override
        public void productPurchased() {
            preferences.edit().putBoolean(APP_PURCHASED, true).apply();
        }

        @Override
        public void notifyPendingPurchase() {
            showDialogMessage();
        }

        @Override
        public void onPurchaseFailed() {
            showSnackBarMessage();
        }
    };

    private void showSnackBarMessage() {
        if (requireActivity().getCurrentFocus() == null) return;
        Snackbar snackbar = Snackbar.make(requireActivity().getCurrentFocus(),
                "Your purchase failed.", Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Dismiss", v -> snackbar.dismiss());
        snackbar.show();
    }

    private void showDialogMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage("You purchase is pending. Press Complete to continue.");
        builder.setPositiveButton("Complete", (dialog, which) ->
                billingManager.launchBillingFlow(requireActivity(), billingCallback, productId));

        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void handleBillingResult(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK || billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            preferences.edit().putBoolean(APP_PURCHASED, true).apply();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                android.util.Log.e(TAG, "handleBillingResult -> ", e);
            }
            requireActivity().onBackPressed();
        }
    }
}
