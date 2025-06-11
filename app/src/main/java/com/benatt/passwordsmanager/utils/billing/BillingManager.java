package com.benatt.passwordsmanager.utils.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsParams.Product;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = BillingManager.class.getSimpleName();
    private BillingCallback callback;
    private final BillingClient billingClient;

    public BillingManager(Context context) {
        PendingPurchasesParams params = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build();

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(params)
                .build();

        startConnection();
    }

    private void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.e(TAG, "onBillingServiceDisconnected -> Error while connecting to billing client");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK)
                    Log.d(TAG, "onBillingSetupFinished -> Billing Client connection successful");
            }
        });
    }

    public void launchBillingFlow(Activity activity, BillingCallback callback) {
        this.callback = callback;

        Product product = Product.newBuilder()
                .setProductId("com.benatt.passwordsmanager.cryptcode_root_access")
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                Log.e(TAG, "launchBillingFlow -> Error while querying product details");
                return;
            }

            ImmutableList<ProductDetailsParams> productDetailsParams = ImmutableList.of(
                    ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetailsList.get(0))
                            .build()
            );

            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParams)
                    .build();

            billingClient.launchBillingFlow(activity, billingFlowParams);
        });
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            callback.onPurchasesUpdated(billingResult);
        } else if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || list == null) {
            Log.e(TAG, "onPurchasesUpdated -> Error while updating purchases");
        } else {
            acknowledgePurchases(list);
        }
    }

    private void acknowledgePurchases(List<Purchase> list) {
        for (Purchase purchase : list) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged()) {
                    AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
                    billingClient.acknowledgePurchase(params, billingResult ->
                            callback.onPurchasesUpdated(billingResult));
                } else callback.productPurchased();
            }
        }
    }

    public void checkPayment(BillingCallback callback) {
        this.callback = callback;
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();
        billingClient.queryPurchasesAsync(params, (billingResult, list) -> {
            acknowledgePurchases(list);
        });
    }
}
