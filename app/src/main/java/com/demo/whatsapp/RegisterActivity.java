package com.demo.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail, userPassword;
    private Button createAccountButton;
    private TextView alreadyHaveAccountLink;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        makeAd();
        prepareInterstitialAd();

        initializeFields();

        alreadyHaveAccountLink.setOnClickListener(view -> {
            sendUserToLoginActivity();
            showInterstitialAd();
        });

        createAccountButton.setOnClickListener(view -> createNewAccount());
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please, Enter Email ...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please, Enter Password ...", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Creating new Account");
            progressDialog.setMessage("Please wait, while we are creating new account for you...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String currentUserID = firebaseAuth.getCurrentUser().getUid();
                    databaseReference.child("Users").child(currentUserID).setValue("");

                    databaseReference.child("Users").child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            sendUserToMainActivity();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void initializeFields() {
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        createAccountButton = findViewById(R.id.register_button);
        alreadyHaveAccountLink = findViewById(R.id.already_have_account_link);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(RegisterActivity.this, MainActivity.class)
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
                Toast.makeText(RegisterActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(RegisterActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(RegisterActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(RegisterActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(RegisterActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(RegisterActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(RegisterActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(RegisterActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(RegisterActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(RegisterActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(RegisterActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(RegisterActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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