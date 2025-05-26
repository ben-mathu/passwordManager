package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.BuildConfig.ALIAS;
import static com.benatt.passwordsmanager.BuildConfig.MIGRATING_VERSION;
import static com.benatt.passwordsmanager.utils.CertUtil.exportPrivateKey;
import static com.benatt.passwordsmanager.utils.Constants.BACKUP_FOLDER;
import static com.benatt.passwordsmanager.utils.Constants.ID_TOKEN;
import static com.benatt.passwordsmanager.utils.Constants.IS_DISCLAIMER_SHOWN;
import static com.benatt.passwordsmanager.utils.Constants.PASSWORDS_MIGRATED;
import static com.benatt.passwordsmanager.utils.Constants.PRIVATE_KEY_FILE_NAME;
import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN;
import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN_WITH_GOOGLE;

import android.Manifest;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.benardmathu.tokengeneration.GenerateRandomString;
import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.ActivityMainBinding;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.BillingManager;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
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

    private final Gson gson = new Gson();

    @Inject
    SharedPreferences preferences;

    @Inject
    BillingManager billingManager;

    private ProgressDialog progressDialog;
    private GoogleIdTokenCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        mainViewModel.message.observe(this, msg -> {
            if (binding.rlProgressBar.getVisibility() == View.VISIBLE)
                binding.rlProgressBar.setVisibility(View.GONE);

            showMessage(msg);
        });

        // setup bottom navigation
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            navController = navHost.getNavController();
            bottomNav = binding.navView;

            NavigationUI.setupWithNavController(bottomNav, navController);

            bottomNav.setOnItemSelectedListener(item -> {
                if (item.getItemId() == R.id.add_password) {
                    navController.navigate(R.id.action_passwords_to_add_password);
                }
                return true;
            });

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.fragment_passwords) {
                    bottomNav.setVisibility(View.VISIBLE);
                } else if (destination.getId() == R.id.fragment_auth) {
                    bottomNav.setVisibility(View.GONE);
                } else {
                    bottomNav.setVisibility(View.GONE);
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
            sharedViewModel.hideProgressBar();
        });

        sharedViewModel.completeMsg.observe(this, msg ->
                preferences.edit()
                        .putBoolean(PASSWORDS_MIGRATED, true)
                        .apply());

        sharedViewModel.showLoader.observe(this, showLoader -> {
            if (showLoader)
                binding.rlProgressBar.setVisibility(View.VISIBLE);
            else
                binding.rlProgressBar.setVisibility(View.GONE);
        });

        sharedViewModel.bottomNavLiveData.observe(this, showBottomNav ->
                bottomNav.setVisibility(View.VISIBLE));

        // below set of lines are required if migration of passwords to a different encryption scheme is required
        if (preferences.getBoolean(PASSWORDS_MIGRATED, false) && Objects.equals(BuildConfig.VERSION_NAME, MIGRATING_VERSION))
            preferences.edit().putBoolean(PASSWORDS_MIGRATED, false).apply();

        boolean isPasswordsMigrated = preferences.getBoolean(PASSWORDS_MIGRATED, false);
        if (!isPasswordsMigrated && Objects.equals(BuildConfig.VERSION_NAME, MIGRATING_VERSION))
            sharedViewModel.migratePasswords();
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

    private void certificateManenoz(Drive googleDriveService) throws Exception, java.lang.Exception {

        // Get the backup folder id
        FileList driverList;
        com.google.api.services.drive.model.File backupFolder = null;
        com.google.api.services.drive.model.File privateKeyFile = null;
        String query = "mimeType = 'application/vnd.google-apps.folder'" +
                " and 'root' in parents and trashed = false";
        try {
            driverList = googleDriveService.files().list().setQ(query)
                    .setFields("files(id,name)").execute();

            List<com.google.api.services.drive.model.File> fileList = driverList.getFiles();

            for (com.google.api.services.drive.model.File file : fileList) {
                if (BACKUP_FOLDER.equals(file.getName())) {
                    backupFolder = file;
                    break;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "certificateManenoz -> Error retrieving cert", e);
        }

        // Create the backup folder if it does not exist
        com.google.api.services.drive.model.File fileDirMetadata = new com.google.api.services.drive.model.File();
        fileDirMetadata.setName(BACKUP_FOLDER);
        fileDirMetadata.setMimeType("application/vnd.google-apps.folder");
        if (backupFolder == null)
            backupFolder = createFolder(googleDriveService, fileDirMetadata);

        // Get the security certificate from Google drive
        query = "mimeType = 'text/plain'" +
                " and '" + backupFolder.getId() + "' in parents and trashed = false";
        FileList backupFolderList = googleDriveService.files().list().setQ(query)
                .setFields("files(id,name)").execute();

        for (com.google.api.services.drive.model.File item : backupFolderList.getFiles()) {
            if (PRIVATE_KEY_FILE_NAME.equals(item.getName())) {
                privateKeyFile = item;
                break;
            }
        }

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        if (privateKeyFile == null && keyStore.containsAlias(ALIAS))
            exportPrivateKey(keyStore, backupFolder, googleDriveService, this, preferences);
    }

    private com.google.api.services.drive.model.File createFolder(Drive googleDriveService,
                                                                  com.google.api.services.drive.model.File fileDirMetadata) {
        try {
            return googleDriveService.files().create(fileDirMetadata)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            Log.e(TAG, "createFolder -> Error creating folder", e);
        }
        return fileDirMetadata;
    }

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
            createBackup();
            return true;
        } else if (item.getItemId() == R.id.restore_password) {
            restorePasswords();
            return true;
        } else if (item.getItemId() == R.id.refresh_password_list) {
            sharedViewModel.refreshList();
            return true;
        } else if (item.getItemId() == R.id.generate_qr_code) {
            try {
                generateQRCode();
            } catch (WriterException e) {
                Log.e(TAG, "onOptionsItemSelected -> Error generating QR code", e);
            }
            return true;
        } else if (item.getItemId() == R.id.scan_qr) {
            scanQRCode();
            return true;
        } else if (item.getItemId() == R.id.unlock_premium) {
            billingManager.launchBillingFlow(this, (billingResult, list) -> {

            });
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        preferences.edit()
                .putBoolean(SIGNED_IN, false)
                .putBoolean(SIGNED_IN_WITH_GOOGLE, false)
                .apply();

        if (navController != null) navController.navigate(R.id.fragment_auth);
    }

    private void scanQRCode() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

        scanQrCodeLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> scanQrCodeLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String jsonPasswords = result.getData().getStringExtra("SCAN_RESULT");
                    List<Password> pList = gson.fromJson(jsonPasswords, new TypeToken<List<Password>>() {
                    }.getType());

                    mainViewModel.savePasswords(pList);
                }
            });

    private void signInUser() {
        String nounce = Arrays.toString(Base64.encodeBase64(new GenerateRandomString(16).nextString().getBytes(Charset.defaultCharset())));
        GetGoogleIdOption getGoogleIdOption = new GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setNonce(nounce)
                .build();
        requestSignIn(getGoogleIdOption);
    }

    private void generateQRCode() throws WriterException {
        String passwordsJson = gson.toJson(passwordList);

        QRCodeWriter codeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = codeWriter.encode(passwordsJson, BarcodeFormat.QR_CODE, 300, 300);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bmp);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = 300;
        params.height = 300;
        params.horizontalMargin = 20;
        params.verticalMargin = 20;

        WindowManager.LayoutParams textParams = new WindowManager.LayoutParams();
        textParams.horizontalMargin = 20;
        textParams.verticalMargin = 20;

        imageView.setLayoutParams(params);

        TextView text = new TextView(this);
        text.setPadding(50, 0, 0, 50);
        text.setText(R.string.scan_the_qr_code_to_migrate_passwords);
        text.setTextColor(ContextCompat.getColor(this, R.color.white));
        text.setTextSize(21);
        text.setLayoutParams(textParams);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(text);
        linearLayout.addView(imageView);

        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                android.R.style.Theme_Material_Dialog_Alert);
        builder.setView(linearLayout);
        builder.setPositiveButton("Close", (dialogInterface, i) ->
                dialogInterface.dismiss()).show();
    }

    public void restorePasswords() {
        showProgressBar("Restoring Passwords");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra("path", filePath);

        retrieveFileLauncher.launch(Intent.createChooser(intent, "Select a file"));
    }

    private final ActivityResultLauncher<Intent> retrieveFileLauncher =
            registerForActivityResult(new StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Log.d(TAG, "Successfully retrieved file -> ");
                    Uri contentUri = result.getData().getData();
                    if (contentUri != null) {
                        new Thread(() -> {
                            try (InputStream inputStream = getContentResolver().openInputStream(contentUri)) {
                                if (inputStream != null) {
                                    StringBuilder sb = new StringBuilder();
                                    byte[] buffer = new byte[2046];
                                    long read;
                                    while ((read = inputStream.read(buffer)) != -1) {
                                        sb.append(new String(buffer, 0, (int) read));
                                    }

                                    List<Password> passwordsList = gson.fromJson(sb.toString(), new TypeToken<List<Password>>() {
                                    }.getType());
                                    mainViewModel.savedAndDecrypt(passwordsList);
                                }
                                new Handler(getMainLooper())
                                        .post(progressDialog::dismiss);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                } else Log.e(TAG, "Could not retrieve file -> ");
            });

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                .show();
    }

    public void requestSignIn(GetGoogleIdOption getGoogleIdOption) {
        CredentialManager credentialManager = CredentialManager.create(this);
        GetCredentialRequest request;
        if (getGoogleIdOption == null) {
            String nounce = Arrays.toString(Base64.encodeBase64(new GenerateRandomString(16).nextString().getBytes(Charset.defaultCharset())));
            GetSignInWithGoogleOption getSignInWithGoogleOption = new GetSignInWithGoogleOption.Builder(BuildConfig.CLIENT_ID)
                    .setNonce(nounce)
                    .build();
            request = new GetCredentialRequest.Builder()
                    .addCredentialOption(getSignInWithGoogleOption)
                    .build();
        } else {
            request = new GetCredentialRequest.Builder()
                    .addCredentialOption(getGoogleIdOption)
                    .build();
        }
        Executor executor = ContextCompat.getMainExecutor(this);
        credentialManager.getCredentialAsync(this, request, null, executor, new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
            @Override
            public void onResult(GetCredentialResponse response) {
                credential = GoogleIdTokenCredential.createFrom(response.getCredential().getData());
                preferences.edit()
                        .putString(ID_TOKEN, credential.getIdToken())
                        .putBoolean(SIGNED_IN_WITH_GOOGLE, true)
                        .apply();
                navController.navigate(R.id.fragment_passwords);
                Log.d(TAG, "onResult -> Successfully authenticated");

                if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS);
                }
            }

            @Override
            public void onError(@NonNull GetCredentialException e) {
                Log.e(TAG, "onError -> Error authenticating", e);
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new RequestPermission(), isGranted -> {
        if (isGranted) Log.d(TAG, "onResult -> Thank you");
    });

    private void createBackup() {
        showProgressBar("Uploading to Google drive");
        SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());
        String fileName = sp.format(new Date()) + ".txt";
        new Thread(() -> {
            try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {

                for (Password password : passwordList) {
                    password.setCipher(Decryptor.decryptPassword(password.getCipher(), null, ALIAS));
                }

                String json = new Gson().toJson(passwordList);
                if (fos != null) fos.write(json.getBytes(Charset.defaultCharset()));
                else {
                    Log.e(TAG, "Error uploading file -> important");
                    return;
                }

                new Handler(getMainLooper()).post(() -> {
                    File file = new File(getApplicationContext().getFilesDir(), fileName);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");

                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this,
                            "com.benatt.passwordsmanager.fileprovider", file));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    backupActivityResultLauncher.launch(Intent.createChooser(intent, "Backup passwords"));
                });
            } catch (IOException | Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
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
                progressDialog.dismiss();
            });

    public void showProgressBar(String message) {
        // Show a progress loader
        if (!this.isDestroyed()) {
            progressDialog = new ProgressDialog(this);

            progressDialog.setTitle(message);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putBoolean(SIGNED_IN, false).apply();
    }
}