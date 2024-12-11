package com.example.bhelvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bhelvisualizer.adapter.FragmentPagerAdapter;
import com.example.bhelvisualizer.adapter.ReportAdapter;
import com.example.bhelvisualizer.model.Report;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements ReportAdapter.OnReportClickListener {

    private ImageView list;

    private int userPriority;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private ViewPager2 viewPager;

    private TextView headerUsername;
    private Button reportButton;
    private Button fileButton;
    private TextView headerEmail;
    private AppCompatImageButton uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.BhelList);

        userPriority = SharedPreferencesUtil.getUserPriority(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        fileButton = findViewById(R.id.fileButton);
        reportButton = findViewById(R.id.reportButton);
        viewPager = findViewById(R.id.viewPager);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(this);
        viewPager.setAdapter(adapter);

        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1); // Switch to the ViewFilesFragment page
            }
        });

        reportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0); // Switch to the ReportFragment page
            }
        });

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUploadOptions();
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonStates(position);
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
            }
        });

        // Initialize header views
        View headerView = navigationView.getHeaderView(0);
        headerUsername = headerView.findViewById(R.id.header_username);
        headerEmail = headerView.findViewById(R.id.header_email);

        // Fetch user info and set in header
        setUserInformation();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_change_password) {
                    Intent intent = new Intent(MainActivity.this, ChangePassword.class);
                    startActivity(intent);
                } else if (id == R.id.nav_logout) {
                    // Handle logout action
                    SharedPreferencesUtil.clearPreferences(MainActivity.this);
                    SharedPreferencesUtil.setLoggedIn(MainActivity.this, false, "User");
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });

        // Display default fragment (ReportFragment)
        viewPager.setCurrentItem(0);
    }

    private void showUploadOptions() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.upload_options_bottom_sheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        Button reportOptionButton = bottomSheetView.findViewById(R.id.reportOptionButton);
        Button fileOptionButton = bottomSheetView.findViewById(R.id.fileOptionButton);

        reportOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ReportUpload.class);
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });

        fileOptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FileUpload.class);
                startActivity(intent);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.show();
    }

    private void setUserInformation() {
        // Fetch username and email from SharedPreferencesUtil
        String username = SharedPreferencesUtil.getUserName(this);
        String email = SharedPreferencesUtil.getUserEmail(this);

        // Set username and email in the header
        headerUsername.setText(username);
        headerEmail.setText(email);
    }

    @Override
    public void onReportClick(Report report) {
        Intent intent = new Intent(this, Graph.class);
        intent.putExtra("reportName", report.getName());
        intent.putExtra("chartType", report.getChartType());
        intent.putExtra("csvFileName", report.getCsvFileName());
        startActivity(intent);
    }

    private void updateButtonStates(int position) {
        if (position == 0) {
            reportButton.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue));
            reportButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            fileButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            fileButton.setTextColor(ContextCompat.getColor(this, R.color.navy_blue));
        } else if (position == 1) {
            fileButton.setBackgroundColor(ContextCompat.getColor(this, R.color.navy_blue));
            fileButton.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            reportButton.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            reportButton.setTextColor(ContextCompat.getColor(this, R.color.navy_blue));
        }
    }
}
