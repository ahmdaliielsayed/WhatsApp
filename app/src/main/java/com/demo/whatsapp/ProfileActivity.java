package com.demo.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "user id: " + receiverUserID, Toast.LENGTH_SHORT).show();
    }
}