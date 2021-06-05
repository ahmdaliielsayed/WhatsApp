package com.demo.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {

    private Button loginButton, phoneLoginButton;
    private EditText userEmail, userPassword;
    private TextView forgetPasswordLink, needNewAccountLink;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        makeAd();
        prepareInterstitialAd();

        initializeFields();

        needNewAccountLink.setOnClickListener(view -> {
            sendUserToRegisterActivity();
            showInterstitialAd();
        });

        loginButton.setOnClickListener(view -> allowUserToLogin());

        phoneLoginButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, LoginPhoneActivity.class));
            showInterstitialAd();
        });
    }

    private void allowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please, Enter Email ...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please, Enter Password ...", Toast.LENGTH_SHORT).show();
        } else {
            progressDialog.setTitle("Sign in");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String currentUserID = firebaseAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    databaseReference.child(currentUserID).child("device_token").setValue(deviceToken).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            sendUserToMainActivity();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void initializeFields() {
        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.new_account_link);
        forgetPasswordLink = findViewById(R.id.forget_password_link);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        progressDialog = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void sendUserToRegisterActivity() {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }

    private void makeAd() {
        // 1. Place an AdView
        adView = findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(LoginActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(LoginActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(LoginActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(LoginActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(LoginActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(LoginActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(LoginActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(LoginActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(LoginActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(LoginActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(LoginActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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