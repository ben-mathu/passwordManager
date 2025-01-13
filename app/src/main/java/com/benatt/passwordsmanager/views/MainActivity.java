package com.benatt.passwordsmanager.views;

import static com.benatt.passwordsmanager.BuildConfig.ALIAS;
import static com.benatt.passwordsmanager.BuildConfig.MIGRATING_VERSION;
import static com.benatt.passwordsmanager.utils.CertUtil.exportPrivateKey;
import static com.benatt.passwordsmanager.utils.Constants.BACKUP_FOLDER;
import static com.benatt.passwordsmanager.utils.Constants.IS_CERT_UPLOADED;
import static com.benatt.passwordsmanager.utils.Constants.IS_DISCLAIMER_SHOWN;
import static com.benatt.passwordsmanager.utils.Constants.PASSWORDS_MIGRATED;
import static com.benatt.passwordsmanager.utils.Constants.PRIVATE_KEY_FILE_NAME;
import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN;
import static com.benatt.passwordsmanager.utils.Constants.SIGNED_IN_WITH_GOOGLE;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.benatt.passwordsmanager.BuildConfig;
import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.data.models.passwords.model.Password;
import com.benatt.passwordsmanager.databinding.ActivityMainBinding;
import com.benatt.passwordsmanager.exceptions.Exception;
import com.benatt.passwordsmanager.utils.Constants;
import com.benatt.passwordsmanager.utils.Decryptor;
import com.benatt.passwordsmanager.utils.DriveServiceHelper;
import com.benatt.passwordsmanager.utils.SaveFile;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_GOOGLE_SIGN_IN = 3001;
    private static final int PIN_REQUEST_CODE = 1102;

    private KeyguardManager keyguardManager;

    private MainViewModel mainViewModel;
    private SharedViewModel sharedViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private ActivityMainBinding binding;

    private NavHostFragment navHost;
    private NavController navController;
    private BottomNavigationView bottomNav;

    private List<Password> passwords = new ArrayList<>();
    private List<Password> passwordList = new ArrayList<>();

    private Gson gson = new Gson();
    private GoogleSignInOptions gso;
    private GoogleSignInClient gsc;
    private GoogleSignInAccount account;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private MenuItem signIn;
    private MenuItem signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApp) getApplicationContext()).getPasswordsComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);
        sharedViewModel = new ViewModelProvider(this, viewModelFactory).get(SharedViewModel.class);
        
        preferences = MainApp.getPreferences();

        mainViewModel.message.observe(this, msg -> {
            if (binding.rlProgressBar.getVisibility() == View.VISIBLE)
                binding.rlProgressBar.setVisibility(View.GONE);

            showMessage(msg);
        });

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
            } else if (destination.getId() == R.id.fragment_auth) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });

        boolean isDisclaimerShown = preferences
                .getBoolean(IS_DISCLAIMER_SHOWN, false);

        if (!isDisclaimerShown)
            startAlertActivity();

        mainViewModel.getPasswords();
        mainViewModel.passwords.observe(this, passwords -> {
            passwordList = passwords;
            sharedViewModel.hideProgressBar();
        });

        sharedViewModel.completeMsg.observe(this, msg -> {
            preferences.edit()
                    .putBoolean(PASSWORDS_MIGRATED, true)
                    .apply();
        });

        sharedViewModel.isLogin.observe(this, isLoggedIn -> {
            if (isLoggedIn)
                requestSignIn();
        });

        sharedViewModel.showLoader.observe(this, showLoader -> {
            if (showLoader)
                binding.rlProgressBar.setVisibility(View.VISIBLE);
            else
                binding.rlProgressBar.setVisibility(View.GONE);
        });

        sharedViewModel.bottomNavLiveData.observe(this, showBottomNav -> {
            bottomNav.setVisibility(View.VISIBLE);
        });

        if (preferences.getBoolean(PASSWORDS_MIGRATED, false) && BuildConfig.VERSION_NAME == MIGRATING_VERSION)
            preferences.edit().putBoolean(PASSWORDS_MIGRATED, false).apply();

        boolean isPasswordsMigrated = preferences.getBoolean(PASSWORDS_MIGRATED, false);
        if (!isPasswordsMigrated && BuildConfig.VERSION_NAME == MIGRATING_VERSION)
            sharedViewModel.migratePasswords();
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean isLoggedIn = preferences.getBoolean(SIGNED_IN, false);
        if (!isLoggedIn) {
            keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager.isKeyguardSecure()) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(
                        getString(R.string.auth_key_guard),
                        getString(R.string.auth_msg)
                );
                startActivityForResult(intent, PIN_REQUEST_CODE);
            }
        }
    }

    private void certificateManenoz(GoogleAccountCredential credential,
                                    Drive googleDriveService)
            throws Exception, java.lang.Exception {

        // Get the backup folder id
        FileList driverList = null;
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
            exportPrivateKey(keyStore, fileDirMetadata, backupFolder, googleDriveService, this);
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

        signOut = menu.findItem(R.id.sign_out);
        signIn = menu.findItem(R.id.sign_in);

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

        boolean googleSignedIn = preferences.getBoolean(SIGNED_IN_WITH_GOOGLE, false);
        if (googleSignedIn) {
            signOut.setVisible(true);
        } else {
            signIn.setVisible(true);
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
        } else if (item.getItemId() == R.id.sign_out) {
            signOutUser();
        } else if (item.getItemId() == R.id.sign_in) {
            requestSignIn();
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOutUser() {
        preferences.edit()
                .putBoolean(SIGNED_IN, false)
                .putBoolean(SIGNED_IN_WITH_GOOGLE, false)
                .apply();

        navController.navigate(R.id.fragment_auth);
    }

    private void scanQRCode() {
        Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

        startActivityForResult(intent, 1005);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1005 && resultCode == RESULT_OK) {
            String jsonPasswords = data.getStringExtra("SCAN_RESULT");
            List<Password> pList = gson.fromJson(jsonPasswords, new TypeToken<List<Password>>() {
            }.getType());

            mainViewModel.savePasswords(pList);
        } else if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);
                preferences.edit()
                        .putBoolean(SIGNED_IN_WITH_GOOGLE, true)
                        .apply();
                navController.navigate(R.id.fragment_passwords);

                signIn.setVisible(false);
                signOut.setVisible(true);

                new Thread(() -> {
                    boolean isCertAlreadyUploaded = preferences
                            .getBoolean(IS_CERT_UPLOADED, false);

                    if (!isCertAlreadyUploaded) {
                        try {
                            // Get user credentials
                            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this,
                                    Arrays.asList(DriveScopes.DRIVE_FILE));
                            credential.setSelectedAccount(account.getAccount());

                            // Create a Google Drive service
                            Drive googleDriveService = new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential
                            ).setApplicationName("Passwords").build();

                            certificateManenoz(credential, googleDriveService);
                        } catch (InvalidObjectException e) {
                            showMessage(e.getMessage());
                            Log.e(TAG, "onStart -> Error", e);
                        } catch (Exception | java.lang.Exception e) {
                            Log.e(TAG, "onStart: Error processing certificate", e);
                        }
                    }
                }).start();
            } catch (ApiException e) {
                Log.e(TAG, "onActivityResult -> sign in failed", e);
            }

        } else if (requestCode == PIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                preferences.edit()
                        .putBoolean(SIGNED_IN, true)
                        .apply();
                requestSignIn();
            } else {
                finish();
            }
        }
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
        text.setText("Scan the QR code to migrate passwords");
        text.setTextColor(getResources().getColor(R.color.white));
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

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this,
                Arrays.asList(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());

        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                credential
        ).setApplicationName("Passwords").build();

        DriveServiceHelper driveServiceHelper = new DriveServiceHelper(googleDriveService);
        driveServiceHelper.getAllFiles()
                .addOnSuccessListener(outputStream -> {
                    new Thread(() -> {
                        ByteArrayOutputStream bos = (ByteArrayOutputStream) outputStream;
                        String jsonCipher = bos.toString();

                        // Get the backup folder id
                        FileList driverList = null;
                        com.google.api.services.drive.model.File backupFolder = null;
                        com.google.api.services.drive.model.File certFile = null;
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
                            Log.e(TAG, "restorePasswords -> Error restoring password", e);
                        }

                        List<Password> passwords = new Gson().fromJson(jsonCipher,
                                new TypeToken<List<Password>>() {}.getType());

                        mainViewModel.savedAndDecrypt(passwords);

                        new Handler(getMainLooper())
                                .post(progressDialog::dismiss);
                    }).start();
                }).addOnFailureListener(this, e -> showMessage("Could not get file"));
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                .show();
    }

    public void requestSignIn() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
    }

    private void createBackup() {
        showProgressBar("Uploading to Google drive");

        // Get a list of passwords
        if (passwordList.isEmpty()) {
            mainViewModel.getPasswords();
        }

        String filePath = "/storage/emulated/0/myfile.txt";

        SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());

        // backup file name
        String fileName = sp.format(new Date()) + ".txt";

        // Create a background thread for network calls
        new Thread(() -> {
            // Create output stream to write backup to
            try (FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE)) {

                OutputStreamWriter writer = new OutputStreamWriter(fos);

                for (Password password : passwordList) {
                    password.setCipher(Decryptor.decryptPassword(password.getCipher(), null, ALIAS));
                }

                String json = new Gson().toJson(passwordList);
                writer.write(json);

                writer.flush();
                writer.close();

                File fileOutput = new File(getApplicationContext().getFilesDir(), fileName);

                // Get user credentials
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(this,
                        Arrays.asList(DriveScopes.DRIVE_FILE));
                credential.setSelectedAccount(account.getAccount());

                // Create a Google Drive service
                Drive googleDriveService = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential
                ).setApplicationName("Passwords").build();

                    com.google.api.services.drive.model.File fileDirMetadata = new com.google.api.services.drive.model.File();
                    fileDirMetadata.setName(BACKUP_FOLDER);
                    fileDirMetadata.setMimeType("application/vnd.google-apps.folder");

                    com.google.api.services.drive.model.File backupFolder = null;

                    String fileId = "";
                    // Get the backup folder id
                    String query = "mimeType = 'application/vnd.google-apps.folder'" +
                            " and 'root' in parents and trashed = false";
                    FileList driverList = null;
                    try {
                        driverList = googleDriveService.files().list().setQ(query)
                                .setFields("files(id, name)")
                                .execute();

                        List<com.google.api.services.drive.model.File> fileList = driverList.getFiles();
                        for (com.google.api.services.drive.model.File file : fileList) {
                            if (BACKUP_FOLDER.equals(file.getName())) {
                                backupFolder = file;
                                fileId = file.getId();
                                break;
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "createBackup -> Error retrieving list", e);
                    }

                    fileDirMetadata = new com.google.api.services.drive.model.File();
                    fileDirMetadata.setName(fileOutput.getName());
                    fileDirMetadata.setParents(Collections.singletonList(fileId));
                    FileContent fileContent = new FileContent("text/plain", fileOutput);
                    com.google.api.services.drive.model.File file = null;
                    try {
                        file = googleDriveService.files().create(fileDirMetadata, fileContent)
                                .setFields("id, parents")
                                .execute();

                    } catch (IOException e) {
                        Log.e(TAG, "createBackup -> Error creating backup", e);
                    } finally {
                        new Handler(getMainLooper())
                                .post(progressDialog::dismiss);
                    }
            } catch (IOException | Exception e) {
                Log.e(TAG, "createBackup -> Error creating backup", e);
                new Handler(getMainLooper())
                        .post(progressDialog::dismiss);
            }
        }).start();
    }

    public void showProgressBar(String message) {
        // Show a progress loader
        progressDialog = new ProgressDialog(this);

        progressDialog.setTitle(message);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.edit().putBoolean(SIGNED_IN, false).apply();
    }
}