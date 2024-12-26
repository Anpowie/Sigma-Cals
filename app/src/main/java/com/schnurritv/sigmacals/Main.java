package com.schnurritv.sigmacals;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.schnurritv.sigmacals.databinding.ActivityMainBinding;
import com.schnurritv.sigmacals.storage.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Storage.deleteAllFilesInDirectory(getApplicationContext());


        /*
        Storage.debugSaveDay(getApplicationContext(), new Day(Arrays.asList(
                new Meal(600, 40, "schnitzl"),
                new Meal(1200, 60, "cum"))), -1);

        Storage.debugSaveDay(getApplicationContext(), new Day(Arrays.asList(
                new Meal(600, 40, "schnitzl"),
                new Meal(1600, 660, "cum"))), -2);

        Storage.debugSaveDay(getApplicationContext(), new Day(Arrays.asList(
                new Meal(600, 40, "schnitzl"),
                new Meal(1400, 20, "cum"))), -3);
        */

        Context context = getApplicationContext();
        Storage.init(context);
        Preferences.init(context);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);



    }

}