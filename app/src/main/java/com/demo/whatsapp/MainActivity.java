package com.demo.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("WhatsApp");

        viewPager = findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);

        tabLayout = findViewById(R.id.main_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null){
            sendUserToLoginActivity();
        } else {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence() {
        String currentUserID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        databaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("name").exists()) {
                    Toast.makeText(MainActivity.this, "welcome", Toast.LENGTH_SHORT).show();
                } else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_find_friends_option) {
            sendUserToFindFriendsActivity();
        } else if (item.getItemId() == R.id.main_create_group_option) {
            requestNewGroup();
        } else if (item.getItemId() == R.id.main_settings_option) {
            sendUserToSettingsActivity();
        } else if (item.getItemId() == R.id.main_logout_option) {
            firebaseAuth.signOut();
            sendUserToLoginActivity();
        }

        return true;
    }

    private void sendUserToFindFriendsActivity() {
        startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
    }

    private void requestNewGroup() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.AlertDialog);
        alertDialog.setTitle("Enter Group Name: ");

        final EditText groupNameField = new EditText(this);
        groupNameField.setHint("e.g. Family");
        alertDialog.setView(groupNameField);

        alertDialog.setPositiveButton("Create", (dialogInterface, i) -> {
            String groupName = groupNameField.getText().toString();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(MainActivity.this, "Please, write group name!", Toast.LENGTH_SHORT).show();
            } else {
                createNewGroup(groupName);
            }
        });
        alertDialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());

        alertDialog.show();
    }

    private void createNewGroup(String groupName) {
        databaseReference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, groupName + " group is created successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendUserToLoginActivity() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void sendUserToSettingsActivity() {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}