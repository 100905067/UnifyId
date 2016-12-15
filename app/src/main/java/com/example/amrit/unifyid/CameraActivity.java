package com.example.amrit.unifyid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {

    private static final String LOG_TAG = CameraActivity.class.getSimpleName() ;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private MediaActionSound sound;
    int count = 0;
    TextView countTxt;

    private static String mCurrentPhotoPath;
        private Camera mCamera;
        private CameraPreview mPreview;
        static String TAG = MainActivity.class.getSimpleName();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_camera);
            countTxt = (TextView) findViewById(R.id.countTxt);

            sound = new MediaActionSound();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            
            if(checkCameraHardware(getApplicationContext())) {
                mCamera = getCameraInstance();
                mPreview = new CameraPreview(this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mPreview);
                mCamera.startPreview();
                startTimer();
            }
        }

        //detecting camera hardware
        private boolean checkCameraHardware(Context context) {
            if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
                    return true;
                else
                    return false;
            } else {
                // no camera on this device
                return false;
            }
        }

        //create instance of Camera
        public static Camera getCameraInstance(){
            Camera c = null;
            try {
                c = Camera.open(1); // attempt to get a Camera instance
            }
            catch (Exception e){
                // Camera is not available (in use or does not exist)
            }
            return c; // returns null if camera is unavailable
        }

        //capture

        public void capture() {
            Log.e(LOG_TAG,"captured image");
            mCamera.takePicture(null, null, mPicture);
            sound.play(MediaActionSound.SHUTTER_CLICK);
            mCamera.startPreview();

            increamentCount();
        }

        public void increamentCount() {
            count++;
            countTxt.setText("Count ="+count);
        }

        //start timer
        public  void startTimer() {
            new CountDownTimer(5500, 500) {
                public void onTick(long millisUntilFinished) {
                    capture();
                }

                public void onFinish() {
                    releaseCamera();
                    finish();
                }
            }.start();

        }

        private void releaseCamera(){
            if (mCamera != null){
                mCamera.release();        // release the camera for other applications
                mCamera = null;
            }
        }

        private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = null;
                try {
                    pictureFile = getOutputMediaFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (pictureFile == null){
                    Log.d(LOG_TAG, "Error creating media file, check storage permissions: ");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(LOG_TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
                }
            }
        };

        private File getOutputMediaFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
            return image;
        }
    }
