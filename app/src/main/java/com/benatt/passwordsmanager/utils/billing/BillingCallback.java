package com.benatt.passwordsmanager.utils.billing;

import com.android.billingclient.api.BillingResult;

public interface BillingCallback {
    void onPurchasesUpdated(BillingResult billingResult);
}