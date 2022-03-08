package com.benatt.passwordsmanager.utils;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

/**
 * @author bernard
 */
public class BindingAdapters {

    private BindingAdapters() {
        // Finders keepers
    }

    @BindingAdapter("mutableText")
    public static void setMutableText(TextView view, String text) {
        view.setText(text);
    }
}
