package com.benatt.passwords.views.addpassword;

import android.os.Bundle;
import android.security.KeyPairGeneratorSpec;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benardmathu.tokengeneration.GenerateRandomString;
import com.benatt.passwords.R;
import com.benatt.passwords.databinding.FragmentAddPasswordBinding;
import com.benatt.passwords.utils.ViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Calendar;

import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

/**
 * @author bernard
 */
public class AddPasswordFragment extends Fragment {
    @Inject
    ViewModelFactory viewModelFactory;

    private AddPasswordViewModel addPasswordViewModel;
    private FragmentAddPasswordBinding binding;

    private boolean isShowingPrefs = false;

    private int passwordLength = 8;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddPasswordBinding.inflate(inflater, container, false);

        binding.btnShowPrefs.setOnClickListener(view -> {
            if (isShowingPrefs) {
                binding.btnGenPassword.setVisibility(View.VISIBLE);
                binding.btnShowPrefs.setText(R.string.hide);
                isShowingPrefs = true;
            } else {
                binding.btnGenPassword.setVisibility(View.GONE);
                binding.btnShowPrefs.setText(R.string.gen_password);
            }
        });

        binding.btnGenPassword.setOnClickListener(view -> {
            binding.edtPassword.setText(generatePassword(view));
        });

        binding.btnSubmitPassword.setOnClickListener(view -> savePassword());

        addPasswordViewModel = new ViewModelProvider(this, viewModelFactory).get(AddPasswordViewModel.class);
        binding.setAddPasswordViewModel(addPasswordViewModel);
        return binding.getRoot();
    }

    private void savePassword() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 1);
        assert getActivity() != null;
        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(getActivity().getApplicationContext())
                .setAlias(binding.edtAlias.getText().toString())
                .setSubject(new X500Principal("CN=" + binding.edtPassword.getText().toString() + ", O=Android Authority"))
                .setSerialNumber(BigInteger.ONE)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();
        addPasswordViewModel.savePassword(binding.edtPassword.getText().toString(), binding.edtAlias.getText().toString(), spec);
    }

    private String generatePassword(View view) {
        CheckBox alphabets = binding.cbAlphabets;
        CheckBox digits = binding.cbDigits;
        CheckBox special = binding.cbSpecial;

        GenerateRandomString randomString;
        SecureRandom secureRandom = new SecureRandom();

        if (!binding.edtLength.toString().isEmpty())
            passwordLength = Integer.parseInt(binding.edtLength.toString());

        if (alphabets.isChecked() && !special.isChecked() && !digits.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphas()
            );
        else if (alphabets.isChecked() && digits.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphaNumeric()
            );
        else if (alphabets.isChecked() && special.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphaSpecial()
            );
        else {
            showMessage("Alphabets is required", view);
            return "";
        }

        return randomString.nextString();
    }

    private void showMessage(String message, View view) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
    }
}
