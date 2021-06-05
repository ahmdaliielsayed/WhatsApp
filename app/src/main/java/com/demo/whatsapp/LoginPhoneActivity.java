package com.demo.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    private EditText inputPhoneNumber, inputVerificationCode;
    private Button btnSendVerificationCode, btnVerify;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;

    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        makeAd();
        prepareInterstitialAd();

        initializationFields();

        btnSendVerificationCode.setOnClickListener(view -> {
            String phoneNumber = inputPhoneNumber.getText().toString();
            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(LoginPhoneActivity.this, "please, enter your phone number first!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setTitle("Phone Verification");
                progressDialog.setMessage("please wait, while we are authentication your phone...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                PhoneAuthOptions options =
                        PhoneAuthOptions.newBuilder(mAuth)
                                .setPhoneNumber(phoneNumber)       // Phone number to verify
                                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                .setActivity(LoginPhoneActivity.this)                 // Activity (for callback binding)
                                .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

                showInterstitialAd();
            }
        });

        btnVerify.setOnClickListener(view -> {
            String verificationCode = inputVerificationCode.getText().toString();

            if (TextUtils.isEmpty(verificationCode)) {
                Toast.makeText(LoginPhoneActivity.this, "please, write verification code first!", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setTitle("Verification Code");
                progressDialog.setMessage("please wait, while we are verifying verification code...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);

                signInWithPhoneAuthCredential(credential);
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();

                Toast.makeText(LoginPhoneActivity.this, "Invalid phone number!\nPlease, Enter the correct phone number with your country code!", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.VISIBLE);
                btnSendVerificationCode.setVisibility(View.VISIBLE);

                inputVerificationCode.setVisibility(View.GONE);
                btnVerify.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);

                progressDialog.dismiss();
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = forceResendingToken;

                Toast.makeText(LoginPhoneActivity.this, "Code has been sent, please check and verify!", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.GONE);
                btnSendVerificationCode.setVisibility(View.GONE);

                inputVerificationCode.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
            }
        };
    }

    private void initializationFields() {
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        btnSendVerificationCode = findViewById(R.id.send_verification_code_button);
        btnVerify = findViewById(R.id.verify_button);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        progressDialog.dismiss();

                        Toast.makeText(LoginPhoneActivity.this, "logged in successfully!", Toast.LENGTH_SHORT).show();

//                            FirebaseUser user = task.getResult().getUser();
                        // Update UI
                        sendUserToMainActivity();
                    } else {
                        progressDialog.dismiss();
                        // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(LoginPhoneActivity.this, "Error: " + Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // try to send it again!
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        startActivity(new Intent(LoginPhoneActivity.this, MainActivity.class));
        finish();
    }

    private void makeAd() {
        // 1. Place an AdView
        adView = findViewById(R.id.adView);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
                Toast.makeText(LoginPhoneActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(LoginPhoneActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(LoginPhoneActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginPhoneActivity.this, "onAdFailedToLoad(int errorCode): " + errorCode + "\nده لماا الإعلاان مبيحملش", Toast.LENGTH_SHORT).show();
                switch (errorCode) {
                    case AdRequest.ERROR_CODE_INTERNAL_ERROR:
                        Toast.makeText(LoginPhoneActivity.this, "Something happened internally; for instance, an invalid response was received from the ad server.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_INVALID_REQUEST:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was invalid; for instance, the ad unit ID was incorrect.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NETWORK_ERROR:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was unsuccessful due to network connectivity.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_NO_FILL:
                        Toast.makeText(LoginPhoneActivity.this, "The ad request was successful, but no ad was returned due to lack of ad inventory.", Toast.LENGTH_SHORT).show();
                        break;
                    case AdRequest.ERROR_CODE_APP_ID_MISSING:
                        Toast.makeText(LoginPhoneActivity.this, "APP_ID_MISSING", Toast.LENGTH_SHORT).show();
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