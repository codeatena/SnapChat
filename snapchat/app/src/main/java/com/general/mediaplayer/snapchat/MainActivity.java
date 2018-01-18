package com.general.mediaplayer.snapchat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.button1)
    ImageButton leftbottomBtn;

    @BindView(R.id.button2)
    ImageButton rightbottomBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        leftbottomBtn.setOnClickListener(this);
        rightbottomBtn.setOnClickListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        askPermission();
    }

    public void askPermission(){

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (result != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
        }
    }

    public void onSnapChat(View view){

        Intent intent = new Intent(this ,SnapCameraActivity.class);
        startActivity(intent);

    }

    private long mLastClickTIme = 0;

    @Override
    public void onClick(View v) {

        if (SystemClock.elapsedRealtime() - mLastClickTIme < 500)
        {
            // show CSR app
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.general.mediaplayer.csr");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }

            return;
        }

        mLastClickTIme = SystemClock.elapsedRealtime();

    }
}
