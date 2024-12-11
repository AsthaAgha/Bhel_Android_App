package com.example.bhelvisualizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bhelvisualizer.adapter.CrudAdapter;
import com.example.bhelvisualizer.model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CrudAdapter crudAdapter;
    private List<User> userList;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;
    private ActionBarDrawerToggle toggle;
    private ImageView list;
    private TextView headerUsername;
    private TextView headerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        recyclerView = findViewById(R.id.adminRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = findViewById(R.id.BhelList);
        userList = new ArrayList<>();
        crudAdapter = new CrudAdapter(this, userList);
        recyclerView.setAdapter(crudAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("User");

        AppCompatImageButton createUserButton = findViewById(R.id.createUserButton);
        createUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCRUDActivity();
            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
                    Intent intent = new Intent(AdminPanel.this, ChangePassword.class);
                    startActivity(intent);

                } else if (id == R.id.nav_logout) {
                    // Handle logout action
                    SharedPreferencesUtil.clearPreferences(AdminPanel.this);
                    SharedPreferencesUtil.setLoggedIn(AdminPanel.this, false, "User");
                    Intent intent = new Intent(AdminPanel.this, Login.class);
                    startActivity(intent);
                    finish();
                }

                drawerLayout.closeDrawer(navigationView);
                return true;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        loadData();
    }

    private void loadData() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }
                crudAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void openCRUDActivity() {
        Intent intent = new Intent(AdminPanel.this, CRUD.class);
        startActivity(intent);
    }

    private void setUserInformation() {
        // Fetch username and email from SharedPreferencesUtil
        String username = SharedPreferencesUtil.getUserName(this);
        String email = SharedPreferencesUtil.getUserEmail(this);

        // Set username and email in the header
        headerUsername.setText(username);
        headerEmail.setText(email);
    }
}
