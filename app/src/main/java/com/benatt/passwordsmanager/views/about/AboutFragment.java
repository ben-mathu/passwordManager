package com.benatt.passwordsmanager.views.about;

import static com.benatt.core.utils.AppUtil.readAppDescription;

import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.FragmentAboutBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
        binding.appName.setText(String.format("%s version %s", getString(R.string.app_name), BuildConfig.VERSION_NAME));

        String htmlText = readAppDescription(requireActivity(), R.raw.about);
        if (htmlText != null) {
            Spanned spanned = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY);
            binding.appDescription.setText(spanned);
        }
        return binding.getRoot();
    }
}
