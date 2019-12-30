package com.mydomain;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import static android.provider.MediaStore.Images.Media.getBitmap;

public class SelectPictureUtils {
    static final int REQUEST_CODE_PICK_IMAGE = 200;
    static final int REQUEST_CODE_CAPTURE_IMAGE = 201;
    static final int REQUEST_CODE_CROP_IMAGE = 202;

    // 对应 Manifest.xml 中 provider 标签下的 authorities
    private static final String FILE_PROVIDER_AUTHORITY = "com.mydomain.fileprovider";

    // 保存拍照存放照片的绝对路径
    private static Uri mContentUri = null;
    // 保存图片裁剪后的绝对路径
    private static Uri mCropImageUri = null;

    /**
     * 使用系统相册选择图片
     *
     * @param activity
     */
    static void chooseImage(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 使用系统相机拍照
     *
     * @param activity
     */
    static void captureImage(Activity activity) {
        // 注意这个路径下的目录需要定义在 Manifest.xml-provider-meta-data 标签中来获得共享权限
        // 否则会抛出 SecurityException 异常
        File imagePath = new File(activity.getFilesDir(), "images");
        File newFile = new File(imagePath, System.currentTimeMillis() + ".jpg");
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }
        // 注意这里需要使用 content:// 形式暴露，否则在 7.0 以上系统会抛出 FileUriExposedException 异常
        mContentUri = FileProvider.getUriForFile(activity, FILE_PROVIDER_AUTHORITY, newFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mContentUri);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
    }

    /**
     * 使用系统裁剪
     *
     * @param activity
     * @param uri
     */
    static void startPhoneZoom(Activity activity, Uri uri, boolean isCaptureImage) {
        File file = new File(activity.getExternalCacheDir(), "images");
        if (!file.exists()) {
            file.mkdirs();
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (isCaptureImage) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        mCropImageUri = Uri.parse("file://" + file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        // 这个 outputUri 是要使用 Uri.fromFile(file) 生成的，而不是使用FileProvider.getUriForFile
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCropImageUri);
        activity.startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
    }

    /**
     * 将 uri 转换成 bitmap
     *
     * @param activity
     * @param uri
     * @return
     */
    static Bitmap getBitmapFromUri(Activity activity, Uri uri) {
        if (uri == null) {
            return null;
        }
        try {
            return getBitmap(activity.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Uri getContentUri() {
        return mContentUri;
    }

    public static Uri getCropImageUri() {
        return mCropImageUri;
    }
}
