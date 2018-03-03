package tv.ourglass.alyssa.bourbon_android.Scenes.Introduction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Registration.CheckAuthActivity;

public class CheckAppOpenedActivity extends AppCompatActivity {

    String TAG = "CheckAppOpenedActivity";
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_app_opened);

        Intent intent = new Intent(this, CheckAuthActivity.class);
        startActivity(intent);




    }

}
