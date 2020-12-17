package com.benatt.passwords.views.addpassword;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.benardmathu.tokengeneration.GenerateRandomString;
import com.benatt.passwords.MainApp;
import com.benatt.passwords.R;
import com.benatt.passwords.data.models.passwords.model.Password;
import com.benatt.passwords.databinding.FragmentAddPasswordBinding;
import com.benatt.passwords.utils.ViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.security.SecureRandom;

import javax.inject.Inject;

import static com.benatt.passwords.utils.Constants.EDIT_PASSWORD;
import static com.benatt.passwords.utils.Decryptor.decryptPassword;

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
    private Password password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        this.password = args != null ? args.getParcelable(EDIT_PASSWORD) : null;

        if (password == null) {
            this.password = new Password();
            this.password.setAccountName("");
            this.password.setCipher("");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainApp) getActivity().getApplicationContext()).getPasswordsComponent().inject(this);

        binding = FragmentAddPasswordBinding.inflate(inflater, container, false);
        addPasswordViewModel = new ViewModelProvider(this, viewModelFactory).get(AddPasswordViewModel.class);

        binding.edtAccountName.setText(password.getAccountName());

        binding.btnShowPrefs.setOnClickListener(view -> {
            if (isShowingPrefs) {
                binding.llPreferences.setVisibility(View.GONE);
                binding.btnShowPrefs.setText(R.string.gen_password);
                isShowingPrefs = false;
            } else {
                binding.llPreferences.setVisibility(View.VISIBLE);
                binding.btnShowPrefs.setText(R.string.hide);
                isShowingPrefs = true;
            }
        });

        if (password.isNotEmpty())
            binding.btnDeletePassword.setVisibility(View.VISIBLE);
        else
            binding.btnDeletePassword.setVisibility(View.GONE);

        binding.btnDeletePassword.setOnClickListener(view -> {
            addPasswordViewModel.deletePassword(password);
            NavHostFragment.findNavController(this).navigate(R.id.fragment_passwords);
        });

        String plainPassword = decryptPassword(password);
        binding.edtPassword.setText(plainPassword);
        binding.edtLength.setText(String.valueOf(plainPassword.length()));

        binding.btnSetPassword
                .setOnClickListener(view -> binding.edtPassword.setText(generatePassword(view)));

        binding.btnSubmitPassword.setOnClickListener(view -> savePassword());

        addPasswordViewModel.msgView.observe(
                getViewLifecycleOwner(),
                message -> showMessage(message, getActivity().getCurrentFocus()));

        addPasswordViewModel.goToPasswordsFragments.observe(
                getViewLifecycleOwner(),
                value -> NavHostFragment.findNavController(this)
                        .navigate(R.id.action_add_password_to_passwords));

        binding.setAddPasswordViewModel(addPasswordViewModel);
        return binding.getRoot();
    }

    private void savePassword() {
        addPasswordViewModel.savePassword(binding.edtPassword.getText().toString(), binding.edtAccountName.getText().toString());
    }

    private String generatePassword(View view) {
        CheckBox alphabets = binding.cbAlphabets;
        CheckBox digits = binding.cbDigits;
        CheckBox special = binding.cbSpecial;

        GenerateRandomString randomString;
        SecureRandom secureRandom = new SecureRandom();

        if (!binding.edtLength.getText().toString().isEmpty())
            passwordLength = Integer.parseInt(binding.edtLength.getText().toString());

        if (alphabets.isChecked() && !special.isChecked() && !digits.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphas()
            );
        else if (alphabets.isChecked() && special.isChecked() && digits.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphanumericSpecial()
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
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        addPasswordViewModel.unsubscribe();
    }
}
