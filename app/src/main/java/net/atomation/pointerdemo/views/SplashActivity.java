package net.atomation.pointerdemo.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import net.atomation.pointerdemo.models.Configuration;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration configuration = Configuration.getInstance(this);
        String deviceAddress = configuration.getDeviceAddress();

        Intent intent = ScanActivity.createIntent(this);
        if (deviceAddress != null) {
            intent = MainActivity.createIntent(this);
        }

        startActivity(intent);
    }
}
