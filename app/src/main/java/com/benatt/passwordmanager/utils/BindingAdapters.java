package com.benatt.passwordmanager.utils;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

/**
 * @author bernard
 */
public class BindingAdapters {
    @BindingAdapter("mutableText")
    public static void setMutableText(TextView view, String text) {
        view.setText(text);
    }
}
