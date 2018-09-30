package com.rohit.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import circlecrop.rohit.image.CircleCropActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements CircleCropActivity.CroppedImageCallbacks{

    private int reequestCode=101;
private CircleImageView ivCrop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivCrop=findViewById(R.id.iv_crop);
        CircleCropActivity.openCircleCropActivityWithBitmap(this, BitmapFactory.decodeResource(getResources(),R.drawable.photo),true);
    }



    @Override
    public void setCroppedImage(Bitmap bitmap) {
            ivCrop.setImageBitmap(bitmap);

    }
}
