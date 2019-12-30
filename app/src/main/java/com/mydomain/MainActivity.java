package com.mydomain;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.FileNotFoundException;

import static com.mydomain.SelectPictureUtils.REQUEST_CODE_CAPTURE_IMAGE;
import static com.mydomain.SelectPictureUtils.REQUEST_CODE_CROP_IMAGE;
import static com.mydomain.SelectPictureUtils.REQUEST_CODE_PICK_IMAGE;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS_STORAGE = 100;
    private static final int REQUEST_CODE_PERMISSIONS_CAMERA = 101;

    private ImageView mShowImageView;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mShowImageView = findViewById(R.id.iv_show);


        Log.d("wlfTest", getFilesDir().toString());
        Log.d("wlfTest", getCacheDir().toString());
        Log.d("wlfTest", Environment.getExternalStorageDirectory().toString());
        Log.d("wlfTest", getExternalFilesDir(null).toString());
        Log.d("wlfTest", getExternalCacheDir().toString());
        Log.d("wlfTest", getExternalMediaDirs().toString());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_choose_picture:
                if (ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSIONS_STORAGE);
                } else {
                    SelectPictureUtils.chooseImage(this);
                }
                break;
            case R.id.btn_camera:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CODE_PERMISSIONS_CAMERA);
                } else {
                    SelectPictureUtils.captureImage(this);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS_STORAGE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            SelectPictureUtils.chooseImage(this);
        }
        if (requestCode == REQUEST_CODE_PERMISSIONS_CAMERA &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            SelectPictureUtils.captureImage(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        SelectPictureUtils.startPhoneZoom(this, data.getData(), false);
                    }
                }
                break;
            case REQUEST_CODE_CAPTURE_IMAGE:
                if (resultCode == RESULT_OK) {
                    SelectPictureUtils.startPhoneZoom(this, SelectPictureUtils.getContentUri(), true);
                }
                break;
            case REQUEST_CODE_CROP_IMAGE:
                if (resultCode == RESULT_OK) {
                    Bitmap bp = null;
                    try {
                        // 将 uri 转成位图形式
                        bp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                                SelectPictureUtils.getCropImageUri()
                        ));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    mShowImageView.setImageBitmap(bp);
                }
                break;
            default:
                break;
        }

    }
}
