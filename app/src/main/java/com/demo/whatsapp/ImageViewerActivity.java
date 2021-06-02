package com.demo.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageViewerActivity extends AppCompatActivity {

    private ImageView imageView;
    private String imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        imageView = findViewById(R.id.image_viewer);
        imageURL = getIntent().getStringExtra("url");

        Glide.with(this)
                .load(imageURL)
                .placeholder(R.drawable.person_photo)
                .into(imageView);
    }
}