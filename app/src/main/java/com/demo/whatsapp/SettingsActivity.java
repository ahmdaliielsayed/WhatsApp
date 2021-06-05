package com.demo.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText userName, userStatus;
    private Button updateSettingsButton;

    private FirebaseAuth firebaseAuth;
    private String currentUserID;
    private DatabaseReference databaseReference;

    private static final int GALLERY_IMAGE_PICK = 1;

    private StorageReference userProfileImageReference;

    private ProgressDialog progressDialog;

    private Toolbar settingsToolbar;

    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        makeAd();
        prepareInterstitialAd();

        initializeComponents();

        updateSettingsButton.setOnClickListener(view -> {
            updateSettings();
            showInterstitialAd();
        });

        retrieveUserInfo();

        profileImage.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, GALLERY_IMAGE_PICK);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // start picker to get image for cropping and then use the image in cropping activity
        if (requestCode == GALLERY_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog.setTitle("Set Profile Image");
                progressDialog.setMessage("please wait while your profile image uploading...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Uri resultUri = result.getUri();

                StorageReference imagePath = userProfileImageReference.child(currentUserID + ".jpg");
                imagePath.putFile(resultUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            Task<Uri> downloadUrl = imagePath.getDownloadUrl();
                            downloadUrl.addOnSuccessListener(uri -> {
                                Toast.makeText(SettingsActivity.this, "profile image uploaded successfully!", Toast.LENGTH_SHORT).show();

                                final String profileImageURL = uri.toString();
                                databaseReference.child("Users").child(currentUserID).child("image").setValue(profileImageURL).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "image was saved in database successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(SettingsActivity.this, "Error: " + Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            });
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(SettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                progressDialog.dismiss();
                Exception error = result.getError();
            }
        }
    }

    private void initializeComponents() {
        settingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        profileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        updateSettingsButton = findViewById(R.id.update_settings_button);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        progressDialog = new ProgressDialog(this);
    }

    private void updateSettings() {
        String setUserName = userName.getText().toString();
        String setUserStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)) {
            Toast.makeText(this, "Please, write your username first...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(setUserStatus)) {
            Toast.makeText(this, "Please, write your status...", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", currentUserID);
                profileMap.put("name", setUserName);
                profileMap.put("status", setUserStatus);
            databaseReference.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        databaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image")))) {
                    String retrieveUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveUserStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();
                    String retrieveProfileImage = Objects.requireNonNull(snapshot.child("image").getValue()).toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                    Glide.with(getApplicationContext())
                            .load(retrieveProfileImage)
                            .placeholder(R.drawable.person_photo)
                            .into(profileImage);
                } else if ((snapshot.exists()) && (snapshot.hasChild("name"))) {
                    String retrieveUserName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    String retrieveUserStatus = Objects.requireNonNull(snapshot.child("status").getValue()).toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveUserStatus);
                } else {
                    Toast.makeText(SettingsActivity.this, "Please, set & update your profile information!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(SettingsActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void makeAd() {
        // 1. Place an AdView
        adView = findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(SettingsActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(SettingsActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(SettingsActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(SettingsActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(SettingsActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(SettingsActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        // 2. Build a request
        AdRequest adRequest = new AdRequest.Builder().build();
        // 3.Load an ad
        adView.loadAd(adRequest);
    }

    private void prepareInterstitialAd() {
        // 1. Create InterstitialAd object
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(SettingsActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(SettingsActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(SettingsActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(SettingsActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(SettingsActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(SettingsActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
        // 2. Request an ad
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        // 3. Wait until the right moment
    }

    public void showInterstitialAd() {
        // 4. Check if the ad has loaded
        // 5. Display ad
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.d("InterstitialActivity", "The interstitial wasn't loaded yet.");
        }
    }
}