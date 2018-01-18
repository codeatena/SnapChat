package com.general.mediaplayer.snapchat;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SnapCameraActivity extends AppCompatActivity  implements View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2{

    private static final String  TAG  = "SnapChat::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;

    FeatureDetector detector;
    DescriptorExtractor descriptor;
    DescriptorMatcher matcher;
    Mat descriptors2,descriptors1;
    Mat img1;
    MatOfKeyPoint keypoints1,keypoints2;
    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(SnapCameraActivity.this);
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_snapcamera);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {

        if (!mIsColorSelected)
            recognize(mRgba);

        return false; // don't need subsequent touch events
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        return mRgba;
    }

    public void recognize(Mat aInputFrame) {

        mIsColorSelected = true;

        Imgproc.cvtColor(aInputFrame, aInputFrame, Imgproc.COLOR_RGB2GRAY);
        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        detector.detect(aInputFrame, keypoints2);
        descriptor.compute(aInputFrame, keypoints2, descriptors2);

        // Matching
        MatOfDMatch matches = new MatOfDMatch();
        if (img1.type() == aInputFrame.type()) {
            matcher.match(descriptors1, descriptors2, matches);
        } else {
            return;
        }

        Double min_dist = 100.0; Double max_dist = 0.0;
        List<DMatch> matchesList = matches.toList();
        for (int i = 0; i < matchesList.size(); i++) {
            Double dist = (double) matchesList.get(i).distance;
            if (dist < min_dist)
                min_dist = dist;
            if (dist > max_dist)
                max_dist = dist;
        }

        ArrayList<Double> good_matches = new ArrayList<>();
        for (int i = 0; i < matchesList.size(); i++) {

            Double dist = (double) matchesList.get(i).distance;
            if (dist <= Math.max(2 * min_dist ,0.02)){
                good_matches.add(dist);
            }
        }

        Log.d(TAG ,String.valueOf(good_matches.size()));
        mIsColorSelected = false;

        if (good_matches.size() < 30){

            final MaterialDialog dialog = new MaterialDialog.Builder(this)
                    .customView(R.layout.popup, false)
                    .cancelable(false)
                    .backgroundColorRes(android.R.color.transparent).build() ;
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0);
            dialog.show();

            View view = dialog.getCustomView();
            view.findViewById(R.id.popupdismiss_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.dismiss();
                }
            });
            view.findViewById(R.id.popuplink_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.dismiss();
                    // open link using browser
                    Intent intent = new Intent(SnapCameraActivity.this ,WebViewActivity.class);
                    startActivity(intent);
                    finish();

                }
            });
        }
    }

    private void initializeOpenCVDependencies() throws IOException {
        mOpenCvCameraView.enableView();
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        img1 = new Mat();
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("snapcode.png");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

    }

    public void onCapture(View view)
    {
        if (!mIsColorSelected)
            recognize(mRgba);
    }

    public void onBack(View view)
    {
        finish();
    }
}
