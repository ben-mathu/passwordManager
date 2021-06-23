package com.benatt.passwordsmanager.views;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.benatt.passwordsmanager.MainApp;
import com.benatt.passwordsmanager.R;
import com.benatt.passwordsmanager.databinding.ActivityMainBinding;
import com.benatt.passwordsmanager.utils.ViewModelFactory;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import static com.benatt.passwordsmanager.views.passwords.adapter.PasswordsViewHolder.RESULT_CODE;

public class MainActivity extends AppCompatActivity {
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

    @Override
    public void onBackPressed() {
        int fragmentCount = getSupportFragmentManager().getBackStackEntryCount();

        if (fragmentCount > 0) {
            assert this.getCurrentFocus() != null;
            Snackbar.make(binding.getRoot(), "Press Back Button Again.", Snackbar.LENGTH_SHORT);
        } else {
            super.onBackPressed();
        }
    }
}