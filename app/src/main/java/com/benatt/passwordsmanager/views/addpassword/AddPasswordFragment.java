package com.benatt.passwordsmanager.views.addpassword;

import static com.benatt.passwordsmanager.utils.Constants.EDIT_PASSWORD;
import static com.benatt.passwordsmanager.utils.Decryptor.decryptPassword;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.benardmathu.tokengeneration.GenerateRandomString;
import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.FragmentAddPasswordBinding;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.google.android.material.snackbar.Snackbar;

import java.security.SecureRandom;

import javax.inject.Inject;

/**
 * @author bernard
 */
public class AddPasswordFragment extends Fragment {
    private static final String TAG = AddPasswordFragment.class.getSimpleName();

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainApp) getActivity().getApplicationContext()).getPasswordsComponent().inject(this);

        binding = FragmentAddPasswordBinding.inflate(inflater, container, false);
        addPasswordViewModel = new ViewModelProvider(this, viewModelFactory).get(AddPasswordViewModel.class);

        if (password == null) {
            this.password = new Password();
            this.password.setAccountName("");
            this.password.setCipher("");
            binding.setPassword(password);
        }

//        binding.edtAccountName.setText(password.getAccountName());

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

        // set password when the user is editing the password details
        String plainPassword = "";
        if (!password.getCipher().isEmpty()) {
            try {
                plainPassword = decryptPassword(password.getCipher(), null, BuildConfig.ALIAS);
            } catch (Exception e) {
                Log.e(TAG, "onCreateView: Error", e);
            }
            this.password.setCipher(plainPassword);
            binding.setPassword(password);
        }

        //        binding.edtPassword.setText(plainPassword);
        binding.edtLength.setText(String.valueOf(plainPassword.length() > 0 ? plainPassword.length() : 12));

        binding.btnSetPassword
                .setOnClickListener(view -> binding.edtPassword.setText(generatePassword(view)));

        binding.btnSubmitPassword.setOnClickListener(view -> savePassword());

        addPasswordViewModel.msgView.observe(
                getViewLifecycleOwner(),
                message -> showMessage(message, getActivity().findViewById(android.R.id.content)));

        addPasswordViewModel.goToPasswordsFragments.observe(
                getViewLifecycleOwner(),
                value -> NavHostFragment.findNavController(this)
                        .navigate(R.id.action_add_password_to_passwords));

        binding.setAddPasswordViewModel(addPasswordViewModel);
        return binding.getRoot();
    }

    private void savePassword() {

        addPasswordViewModel.savePassword(password, binding.edtPassword.getText().toString());
    }

    private String generatePassword(View view) {
        CheckBox alphabets = binding.cbAlphabets;
        CheckBox digits = binding.cbDigits;
        CheckBox special = binding.cbSpecial;

        GenerateRandomString randomString;
        SecureRandom secureRandom = new SecureRandom();

        if (!binding.edtLength.getText().toString().isEmpty())
            passwordLength = Integer.parseInt(binding.edtLength.getText().toString());

        if (passwordLength <= 0) {
            Toast.makeText(requireActivity(), "Password length cannot be less than or equal to 0", Toast.LENGTH_LONG).show();
            return "";
        }

        if (alphabets.isChecked() && !special.isChecked() && !digits.isChecked())
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    GenerateRandomString.getAlphas()
            );
        else if (alphabets.isChecked() && special.isChecked() && digits.isChecked()) {
            String alphanumericSpecial = GenerateRandomString.getAlphanumericSpecial();
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    alphanumericSpecial
            );
        } else if (alphabets.isChecked() && digits.isChecked()) {
            String alphanumeric = GenerateRandomString.getAlphaNumeric();
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    alphanumeric
            );
        } else if (alphabets.isChecked() && special.isChecked()) {
            String alphaSpecial = GenerateRandomString.getAlphaSpecial();
            randomString = new GenerateRandomString(
                    passwordLength,
                    secureRandom,
                    alphaSpecial
            );
        } else {
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
