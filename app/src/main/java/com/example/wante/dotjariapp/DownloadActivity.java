package com.example.wante.dotjariapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DownloadActivity extends AppCompatActivity implements Animation.AnimationListener, View.OnClickListener {

    private ImageView download_back, download_img, download_btn, zoom_in, zoom_out;
    private TextView download_name;

    String imageUrl ;


    private Animation animationZoomIn, animationZoomOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);


        download_back = findViewById(R.id.download_back);
        download_img = findViewById(R.id.download_img);
        download_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        //사진줌인줌아웃  애니메이션
        zoom_in = findViewById(R.id.download_zoomin);
        zoom_out = findViewById(R.id.download_zoomout);

        //load 애니메이션
        animationZoomIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        animationZoomOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_out);

        //set animation listener
        animationZoomIn.setAnimationListener(this);
        animationZoomOut.setAnimationListener(this);
        //줌인  클릭시
        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                download_img.startAnimation(animationZoomIn);
            }
        });

        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download_img.startAnimation(animationZoomOut);
            }
        });


        Intent intent = getIntent();
        String userid = intent.getStringExtra("userId");
        String userName = intent.getStringExtra("userName");
        imageUrl = intent.getStringExtra("userImgUri");

        if (imageUrl.equals("default")) {
            download_img.setImageResource(R.drawable.ic_my_photo_24dp);
        } else {
            Glide.with(DownloadActivity.this).load(imageUrl).into(download_img);
        }

        download_name = findViewById(R.id.download_name);
        download_name.setText(userName);


        download_btn = findViewById(R.id.download_btn);
 /*       download_btn.setOnClickListener(this);*/


        }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }


    @Override
    public void onClick(View v) {
        download_img.buildDrawingCache();
        Bitmap captureView = download_img.getDrawingCache();
        FileOutputStream fos;
        try {

            fos = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+"/capture.jpeg");

            captureView.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        }

        Toast.makeText(getApplicationContext(), "이미지저장", Toast.LENGTH_LONG).show();
        onBackPressed();
        finish();
    }
}
