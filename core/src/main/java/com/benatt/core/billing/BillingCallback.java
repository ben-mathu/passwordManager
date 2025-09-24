package com.benatt.core.billing;

import com.android.billingclient.api.BillingResult;

import org.jetbrains.annotations.NotNull;

public interface BillingCallback {
    void onPurchasesUpdated(@NotNull BillingResult result);

    void productPurchased();

    void notifyPendingPurchase();

    void onPurchaseFailed();
}