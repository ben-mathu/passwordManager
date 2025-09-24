package com.benatt.passwordsmanager.views;

import static com.benatt.core.utils.Constants.APP_PURCHASED;
import static com.benatt.core.utils.Constants.PRODUCT_ID;
import static com.benatt.core.utils.Constants.UI_CONTENT;
import static com.benatt.passwordsmanager.BuildConfig.MIGRATING_VERSION;
import static com.benatt.passwordsmanager.utils.Constants.IS_DISCLAIMER_SHOWN;
import static com.benatt.passwordsmanager.utils.Constants.PASSWORDS_MIGRATED;
import static com.benatt.passwordsmanager.utils.Constants.PASSWORD_LIMIT;
import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.billingclient.api.BillingResult;
import com.benatt.core.billing.BillingCallback;
import com.benatt.core.billing.BillingManager;
import com.benatt.core.utils.AppUtil;
import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MainViewModel mainViewModel;
    private SharedViewModel sharedViewModel;

    private ActivityMainBinding binding;

    private NavController navController;
    private BottomNavigationView bottomNav;

    private List<Password> passwords = new ArrayList<>();
    private List<Password> passwordList = new ArrayList<>();

    @Inject
    SharedPreferences preferences;

    @Inject
    BillingManager billingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        mainViewModel.message.observe(this, msg -> {
            if (binding.progressBar.getVisibility() == View.VISIBLE)
                hideProgressBar();

            showMessage(msg);
        });

        // setup bottom navigation
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            navController = navHost.getNavController();
            bottomNav = binding.navView;

            NavigationUI.setupWithNavController(bottomNav, navController);

            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.add_password && (preferences.getBoolean(APP_PURCHASED, false) || passwordList.size() < PASSWORD_LIMIT)) {
                    navController.navigate(R.id.action_passwords_to_add_password);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this)
                            .setTitle("Limit Reached")
                            .setMessage(getString(R.string.password_limit_reached))
                            .setPositiveButton("Learn More", (dialog, which) -> {
                                Bundle bundle = new Bundle();
                                bundle.putString(PRODUCT_ID, "com.benatt.passwordsmanager.cryptcode_root_access");
                                bundle.putString(UI_CONTENT, AppUtil.readAppDescription(this, R.raw.pro_mode));
                                navController.navigate(R.id.fragment_pro, bundle);
                            })
                            .setNegativeButton("Cancel",
                                    (dialog, which) -> dialog.dismiss());
                    builder.show();
                }
                return true;
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.fragment_passwords) {
                    bottomNav.setVisibility(View.VISIBLE);
                    mainViewModel.setNotHome(true);
                } else if (destination.getId() == R.id.fragment_home) {
                    bottomNav.setVisibility(View.GONE);
                    mainViewModel.setNotHome(false);
                } else {
                    bottomNav.setVisibility(View.GONE);
                    mainViewModel.setNotHome(true);
                }
            });
        }

        binding.setMainViewModel(mainViewModel);

        boolean isDisclaimerShown = preferences
                .getBoolean(IS_DISCLAIMER_SHOWN, false);

        if (!isDisclaimerShown)
            startAlertActivity();

        mainViewModel.getPasswords();
        mainViewModel.passwords.observe(this, passwords -> {
            passwordList = passwords;
            sharedViewModel.showLoader.postValue(false);
        });

        sharedViewModel.completeMsg.observe(this, msg ->
                preferences.edit()
                        .putBoolean(PASSWORDS_MIGRATED, true)
                        .apply());

        sharedViewModel.showLoader.observe(this, showLoader -> {
            if (showLoader)
                showProgressBar();
            else hideProgressBar();
        });

        sharedViewModel.bottomNavLiveData.observe(this, showBottomNav ->
                bottomNav.setVisibility(View.VISIBLE));

        // below set of lines are required if migration of passwords to a different encryption scheme is required
        if (preferences.getBoolean(PASSWORDS_MIGRATED, false) && Objects.equals(BuildConfig.VERSION_NAME, MIGRATING_VERSION))
            preferences.edit().putBoolean(PASSWORDS_MIGRATED, false).apply();

        boolean isPasswordsMigrated = preferences.getBoolean(PASSWORDS_MIGRATED, false);
        if (!isPasswordsMigrated && Objects.equals(BuildConfig.VERSION_NAME, MIGRATING_VERSION))
            sharedViewModel.migratePasswords();

        sharedViewModel.fileNameLiveData.observe(this, fileName -> {
            File file = new File(getApplicationContext().getFilesDir(), fileName);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");

            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                    "com.benatt.passwordsmanager.fileprovider", file));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            backupActivityResultLauncher.launch(Intent.createChooser(intent, "Backup passwords"));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean isLoggedIn = preferences.getBoolean(SIGNED_IN, false);
        if (!isLoggedIn) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager.isKeyguardSecure()) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                        getString(R.string.auth_key_guard),
                        getString(R.string.auth_msg)
                );
                keyGuardLauncher.launch(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        billingManager.checkPayment(billingCallback);
    }

    BillingCallback billingCallback = new BillingCallback() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult) {
            AppUtil.handleBillingResult(preferences, billingResult);
        }

        @Override
        public void productPurchased() {
            preferences.edit().putBoolean(APP_PURCHASED, true).apply();
        }

        @Override
        public void notifyPendingPurchase() {
            showSnackBarMessage("Your purchase is pending.");
        }

        @Override
        public void onPurchaseFailed() {
            showSnackBarMessage("Your purchase failed.");
        }
    };

    private void showSnackBarMessage(String msg) {
        if (getCurrentFocus() == null) return;
        Snackbar snackbar = Snackbar.make(getCurrentFocus(),
                msg, Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Dismiss", v -> snackbar.dismiss());
        snackbar.show();
    }

    private final ActivityResultLauncher<Intent> keyGuardLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    preferences.edit()
                            .putBoolean(SIGNED_IN, true)
                            .apply();
                } else {
                    finish();
                }
            });

    private void startAlertActivity() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(
                this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.disclaimer);
        builder.setMessage(R.string.disclaimer_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            preferences.edit()
                    .putBoolean(IS_DISCLAIMER_SHOWN, true)
                    .apply();
            dialog.dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchview = (SearchView) search.getActionView();
        if (searchview != null) {
            searchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    sharedViewModel.passwords.setValue(filter(newText));
                    return true;
                }
            });
        }

        MenuItem home = menu.findItem(R.id.home);
        mainViewModel.isNotHome().observe(this, home::setVisible);

        return super.onCreateOptionsMenu(menu);
    }

    private List<Password> filter(String nextText) {
        List<Password> passwordList = new ArrayList<>();
        if (passwords.isEmpty()) {
            passwords = sharedViewModel.passwords.getValue();
        }
        assert passwords != null;
        for (Password password : passwords) {
            if (passwordMatch(password, nextText)) {
                passwordList.add(password);
            }
        }
        return passwordList;
    }

    private boolean passwordMatch(Password password, String searchText) {
        String accName = password.getAccountName().toLowerCase(Locale.ROOT);
        String searchTxt = searchText.toLowerCase(Locale.ROOT);
        return accName.contains(searchTxt);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.back_passwords) {
            sharedViewModel.createBackup(this, passwordList);
            return true;
        } else if (item.getItemId() == R.id.restore_password) {
            restorePasswords();
            return true;
        } else if (item.getItemId() == R.id.refresh_password_list) {
            sharedViewModel.refreshList();
            return true;
        } else if (item.getItemId() == R.id.home) {
            navController.navigate(R.id.fragment_home);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restorePasswords() {
        showProgressBar();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        retrieveFileLauncher.launch(Intent.createChooser(intent, "Select a file"));
    }

    private final ActivityResultLauncher<Intent> retrieveFileLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Log.d(TAG, "Successfully retrieved file -> ");
                    Uri contentUri = result.getData().getData();
                    if (contentUri != null) {
                        sharedViewModel.restorePasswords(this, contentUri);
                    }
                } else Log.e(TAG, "Could not retrieve file -> ");
            });

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                .show();
    }

    Uri filePath;
    private final ActivityResultLauncher<Intent> backupActivityResultLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    filePath = result.getData().getData();
                    Log.d(TAG, "Uploaded file -> " + filePath);
                } else {
                    Log.e(TAG, "Could not upload file -> error");
                }
                hideProgressBar();
            });

    public void showProgressBar() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putBoolean(SIGNED_IN, false).apply();
    }
}