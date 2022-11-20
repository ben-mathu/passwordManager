package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.utils.Constants.IS_DISCLAIMER_SHOWN;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.ActivityMainBinding;
import com.benatt.passwordsmanager.utils.Constants;
import com.benatt.passwordsmanager.utils.DriveServiceHelper;
import com.benatt.passwordsmanager.utils.SaveFile;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MainViewModel mainViewModel;
    private SharedViewModel sharedViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityMainBinding binding;

    private NavHostFragment navHost;
    private NavController navController;
    private BottomNavigationView bottomNav;

    private DriveServiceHelper driveServiceHelper;
    private List<Password> passwords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApp) getApplicationContext()).getPasswordsComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        sharedViewModel = new ViewModelProvider(this, viewModelFactory).get(SharedViewModel.class);

        mainViewModel.message.observe(this, msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());

        // setup bottom navigation
        navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHost.getNavController();
        bottomNav = binding.navView;

        NavigationUI.setupWithNavController(bottomNav, navController);

        bottomNav.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.add_password) {
                navController.navigate(R.id.action_passwords_to_add_password);
            }
            return true;
        });

        binding.setMainViewModel(mainViewModel);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.fragment_passwords) {
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });

        requestSignIn();

        boolean isDisclaimerShown = MainApp.getPreferences()
                .getBoolean(IS_DISCLAIMER_SHOWN, false);

        if (!isDisclaimerShown)
            startAlertActivity();
    }

    private void startAlertActivity() {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(
                        this, android.R.style.Theme_Material_Dialog_Alert);

        builder.setTitle(R.string.disclaimer);
        builder.setMessage(R.string.disclaimer_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            MainApp.getPreferences().edit()
                    .putBoolean(IS_DISCLAIMER_SHOWN, true)
                    .apply();
            dialog.dismiss();
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> finish());
        builder.show();
    }

    private void formatAndSaveFile(String cipher) {
        SimpleDateFormat sf = new SimpleDateFormat(Constants.BACKUP_DATE_FORMAT, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String now = sf.format(calendar);
        String filePath = "backup_" + now;
        try {
            SaveFile.saveFile(
                    cipher.getBytes(Charset.defaultCharset()),
                    getApplicationInfo().dataDir + File.separator + filePath);
        } catch (IOException e) {
            Log.e(TAG, "formatAndSaveFile: Error " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);

        MenuItem search = menu.findItem(R.id.search);
        SearchView searchview = (SearchView) search.getActionView();
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
            uploadFile(binding.getRoot());
            return true;
        } else if (item.getItemId() == R.id.restore_password) {
            restorePasswords();
            return true;
        } else if (item.getItemId() == R.id.refresh_password_list) {
            sharedViewModel.refreshList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void restorePasswords() {
        driveServiceHelper.getAllFiles()
                .addOnSuccessListener(outputStream -> {
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) outputStream;
                    String jsonCipher = bos.toString();

                    mainViewModel.decrypt(jsonCipher);

                    mainViewModel.decryptedPasswords.observe(this, json -> {
                        List<Password> passwords = new Gson().fromJson(json, new TypeToken<List<Password>>() {}.getType());
                        mainViewModel.savePasswords(passwords);
                    });
                }).addOnFailureListener(this, e ->
                        Toast.makeText(this, "Could not get file", Toast.LENGTH_SHORT)
                                .show());
    }

    public void requestSignIn() {
        GoogleSignInOptions googleSignInOptions
                = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this,
                    googleSignInOptions);

            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            handleSignInIntent(result.getData());
                        }
                    }).launch(googleSignInClient.getSignInIntent());
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(googleSignInAccount -> {
                    GoogleAccountCredential credential = GoogleAccountCredential
                            .usingOAuth2(this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleSignInAccount.getAccount());

                    Drive googleDriveService = new Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            new GsonFactory(),
                            credential
                    ).setApplicationName("Passwords").build();

                    driveServiceHelper = new DriveServiceHelper(googleDriveService);
                }).addOnFailureListener(e ->
                        Log.e(TAG, "handleSignInIntent: Error " + e.getMessage(), e));
    }

    private List<Password> passwordList = new ArrayList<>();

    public void uploadFile(View v) {
        createBackup();
    }

    private void createBackup() {
        ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setTitle("Uploading to Google drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        if (passwordList.isEmpty()) {
            mainViewModel.getPasswords();
        }

        mainViewModel.passwords.observe(this, passwords -> {

            String json = new Gson().toJson(passwords);
            mainViewModel.encryptPasswords(json);

        });

        mainViewModel.encipheredPasswords.observe(this, encryptedText -> {
            String filePath = "/storage/emulated/0/myfile.txt";

            SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());

            String fileName = sp.format(new Date()) + ".txt";

            try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {

                OutputStreamWriter writer = new OutputStreamWriter(fos);

                writer.write(encryptedText);

                writer.flush();
                writer.close();

                File fileOutput = new File(getApplicationContext().getFilesDir(), fileName);

                driveServiceHelper.createFile(fileOutput.getAbsolutePath(), fileName)
                        .addOnSuccessListener(s -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Completed upload", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}