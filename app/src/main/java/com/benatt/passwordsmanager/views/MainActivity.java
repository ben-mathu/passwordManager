package com.benatt.passwordsmanager.views;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
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
import com.benatt.passwordsmanager.utils.SaveFile;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MainViewModel mainViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private KeyguardManager keyguardManager;

    private ActivityMainBinding binding;

    private NavHostFragment navHost;
    private NavController navController;
    private BottomNavigationView bottomNav;

    @Override
    protected void onStart() {
        super.onStart();

        this.keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isDeviceSecure()) {
            Toast.makeText(this, "Please secure your device before using this app", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApp) getApplicationContext()).getPasswordsComponent().inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this, viewModelFactory).get(MainViewModel.class);

        mainViewModel.message.observe(this, msg -> {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        mainViewModel.liveData.observe(this, passwords -> {
            mainViewModel.encryptPasswordData(passwords);
        });

        mainViewModel.encryptedString.observe(this, this::formatAndSaveFile);

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
        inflater.inflate(R.menu.nav_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_password:
                break;
        }

        return true;
    }
}