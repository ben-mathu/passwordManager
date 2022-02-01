package com.benatt.passwordsmanager.views.passwords;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.FragmentPasswordsBinding;
import com.benatt.passwordsmanager.utils.OnActivityResult;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.benatt.passwordsmanager.views.SharedViewModel;
import com.benatt.passwordsmanager.views.passwords.adapter.PasswordsAdapter;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static com.benatt.passwordsmanager.utils.Constants.EDIT_PASSWORD;
import static com.benatt.passwordsmanager.views.passwords.adapter.PasswordsViewHolder.REQUEST_CODE;
import static com.benatt.passwordsmanager.views.passwords.adapter.PasswordsViewHolder.START_PASSWORD_DETAIL_SCREEN;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author bernard
 */
public class PasswordsFragment extends Fragment implements OnItemClick {
    private static final String TAG = PasswordsFragment.class.getSimpleName();
    private static final String PASSWORD_POS = "position";

    private KeyguardManager keyguardManager;

    private PasswordsViewModel passwordsViewModel;
    private SharedViewModel sharedViewModel;

    @Inject
    ViewModelFactory factory;

    private FragmentPasswordsBinding binding;

    private PasswordsAdapter adapter;
    private OnActivityResult onActivityResult;
    private List<Password> passwords = new ArrayList<>();
    private Password password;

    @Override
    public void onStart() {
        super.onStart();

        passwordsViewModel.getPasswords();

        this.keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            showDialog(binding.getRoot());
        }
    }

    private void showDialog(View rootView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Attention");
        builder.setMessage("Please secure your device before using this app");
        builder.setCancelable(false);
        builder.setPositiveButton("ok", ((dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            getActivity().startActivity(intent);
        }));
        builder.show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainApp) getActivity().getApplicationContext()).getPasswordsComponent().inject(this);
        binding = FragmentPasswordsBinding.inflate(inflater, container, false);

        passwordsViewModel = new ViewModelProvider(this, factory).get(PasswordsViewModel.class);
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);

        passwordsViewModel.msgEmpty.observe(getViewLifecycleOwner(), s -> {
            showMessage(s, binding.getRoot());
            binding.rvPasswordList.setVisibility(View.GONE);
            binding.llPlaceholder.setVisibility(View.VISIBLE);
        });

        adapter = new PasswordsAdapter(this, getActivity());
        binding.rvPasswordList.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvPasswordList.setAdapter(adapter);

        passwordsViewModel.passwords.observe(getViewLifecycleOwner(), passwords -> {
            this.passwords = passwords;
            adapter.setPasswords(passwords);

            if (passwords.size() > 0 && binding.rvPasswordList.getVisibility() == View.GONE) {
                binding.rvPasswordList.setVisibility(View.VISIBLE);
                binding.llPlaceholder.setVisibility(View.GONE);
            }
        });

        sharedViewModel.refreshList.observe(getViewLifecycleOwner(), isRefreshList -> {
            if (isRefreshList)
                passwordsViewModel.getPasswords();
        });

        binding.btnAddPassword.setOnClickListener(
                view -> NavHostFragment.findNavController(this)
                        .navigate(R.id.fragment_add_password));

        binding.setPasswordsViewModel(passwordsViewModel);
        return binding.getRoot();
    }

    private void showMessage(String s, View rootView) {
        assert getActivity() != null;
//        assert getActivity().getCurrentFocus() != null;
        Snackbar.make(rootView, s, Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        passwordsViewModel.unsubscribe();
    }

    @Override
    public void onItemClick(Password password) {
        this.password = password;
        startKeyguardActivity(null, START_PASSWORD_DETAIL_SCREEN);
    }

    @Override
    public void startKeyguardActivity(OnActivityResult onActivityResult, int requestCode) {
        this.onActivityResult = onActivityResult;
        KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent intent =  keyguardManager.createConfirmDeviceCredentialIntent(
                    getActivity().getString(R.string.auth_key_guard),
                    getActivity().getString(R.string.auth_msg)
            );
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                this.onActivityResult.onResultReturned();
            }
        } else if (requestCode == START_PASSWORD_DETAIL_SCREEN) {
            if (resultCode == RESULT_OK) {
                Bundle args = new Bundle();
                args.putParcelable(EDIT_PASSWORD, password);
                NavHostFragment.findNavController(this).navigate(R.id.fragment_add_password, args);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.option_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.back_passwords) {

            Gson gson = new Gson();

            if (this.passwords.isEmpty()) {
                this.passwordsViewModel.getPasswords();
            }

            if (!this.passwords.isEmpty()) {
                String jsonPasswordStr = gson.toJson(this.passwords);

                this.passwordsViewModel.encryptPasswordData(jsonPasswordStr);
                this.passwordsViewModel.encryptedString.observe(getViewLifecycleOwner(), cipher -> {
                    SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());
                    try {
                        String fileName = sp.format(new Date()) + ".txt";
                        File dir = new File("backup");
                        if (dir.exists()) {
                            boolean isDirCreated = dir.mkdir();
                        }

                        FileOutputStream fos = getActivity().openFileOutput("backup" + System.lineSeparator() + fileName, Context.MODE_PRIVATE);
                        OutputStreamWriter writer = new OutputStreamWriter(fos);

                        writer.write(cipher);

                        writer.flush();
                        writer.close();

                        File file = getActivity().getDir("backup", Context.MODE_PRIVATE);

                        File fileItem = new File(file, fileName);

//                        FileContent content = new FileContent("text/plain", fileItem);
                    } catch (NullPointerException | IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
