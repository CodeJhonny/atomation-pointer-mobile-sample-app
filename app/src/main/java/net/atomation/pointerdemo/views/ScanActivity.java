package net.atomation.pointerdemo.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.atomation.atomationsdk.api.IScanListener;
import net.atomation.atomationsdk.ble.ScanHelper;
import net.atomation.pointerdemo.R;
import net.atomation.pointerdemo.utis.LocationHelper;
import net.atomation.pointerdemo.views.adapters.ScanAdapter;

import java.util.Arrays;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.getSimpleName();

    public static Intent createIntent(Context context) {
        return new Intent(context, ScanActivity.class);
    }

    private Button mBtnScan;
    private SwipeRefreshLayout srlDevices;

    private ScanHelper mScanHelper;
    private ScanAdapter mScanAdapter;
    private IScanListener mScanListener = new IScanListener() {
        @Override
        public void onDeviceFound(final String deviceAddress, String deviceName, int rssi, byte[] scanRecord) {
            Log.d(TAG, "onDeviceFound() called with: " + "deviceAddress = [" + deviceAddress + "], deviceName = [" + deviceName + "], rssi = [" + rssi + "], scanRecord = [" + Arrays.toString(scanRecord) + "]");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(deviceAddress);
                }
            });
        }

        @Override
        public void onScanStatus(boolean isScanning) {
            @StringRes final int scanButtonText = isScanning ? R.string.scan_stop : R.string.scan_start;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mBtnScan.setText(scanButtonText);
                }
            });
        }

        @Override
        public void onScanError(int errorCode) {
            Log.d(TAG, "onScanError() called with: " + "errorCode = [" + errorCode + "]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mScanHelper = ScanHelper.getInstance(this);

        mBtnScan = (Button) findViewById(R.id.btn_scan_start);

        mBtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScanHelper.isScanning()) {
                    stopScan();
                } else {
                    startScan();
                }
            }
        });

        RecyclerView rcvDevices = (RecyclerView) findViewById(R.id.rcv_scan_devices);
        rcvDevices.setLayoutManager(new LinearLayoutManager(this));
        mScanAdapter = new ScanAdapter();
        rcvDevices.setAdapter(mScanAdapter);

        srlDevices = (SwipeRefreshLayout) findViewById(R.id.srl_scan);
        srlDevices.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScanAdapter.clear();
                if (!mScanHelper.isScanning()) {
                    startScan();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationHelper.getInstance(this).askForLocationIfNeeded(this);
    }

    @Override
    protected void onPause() {
        stopScan();
        super.onPause();
    }

    private void stopScan() {
        mScanHelper.stopScanning();
        stopRefreshing();
    }

    private void startScan() {
        mScanHelper.startScanning(mScanListener);
    }

    private void addDevice(final String deviceAddress) {
        stopRefreshing();
        mScanAdapter.addDevice(deviceAddress);
    }

    private void stopRefreshing() {
        srlDevices.setRefreshing(false);
    }
}
