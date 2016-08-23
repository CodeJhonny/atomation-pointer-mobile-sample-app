package net.atomation.pointerdemo.models;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import net.atomation.atomationsdk.api.IMultiSenseAtom;
import net.atomation.atomationsdk.api.IOnButtonPressedListener;
import net.atomation.atomationsdk.api.IScanListener;
import net.atomation.atomationsdk.ble.MultiSenseAtomManager;
import net.atomation.atomationsdk.ble.ScanHelper;
import net.atomation.pointerdemo.R;
import net.atomation.pointerdemo.utis.LocationHelper;
import net.atomation.pointerdemo.utis.NotificationsManager;
import net.atomation.pointerdemo.views.MainActivity;

public class MultiSenseService extends Service {

    private static final String TAG = MultiSenseService.class.getSimpleName();

    private static final String INTENT_ACTION_START = "net.atomation.pointerdemo.service_start";
    private static final String INTENT_ACTION_STOP = "net.atomation.pointerdemo.service_stop";

    public static Intent createStartIntent(Context context) {
        return new Intent(context, MultiSenseService.class)
                .setAction(INTENT_ACTION_START);
    }

    public static Intent createStopIntent(Context context) {
        return new Intent(context, MultiSenseService.class)
                .setAction(INTENT_ACTION_STOP);
    }

    private ScanHelper mScanHelper;
    private IMultiSenseAtom mAtom;

    private IScanListener scanListener = new IScanListener() {
        @Override
        public void onDeviceFound(String deviceAddress, String deviceName, int rssi, byte[] scanRecord) {

        }

        @Override
        public void onScanStatus(boolean isScanning) {
            NotificationsManager manager = NotificationsManager.getInstance(MultiSenseService.this);
            if (isScanning) {
                Intent intent = MainActivity.createIntent(MultiSenseService.this);
                startForeground(manager.getScanNotificationId(), manager.getScanNotification(intent));
            } else {
                stopForeground(true);
            }
        }

        @Override
        public void onScanError(int errorCode) {

        }
    };

    private IOnButtonPressedListener buttonPressedListener = new IOnButtonPressedListener() {
        @Override
        public void onPress(int reason) {
            if (reason == IMultiSenseAtom.TxReason.PUSH_BUTTON) {
                onButtonPressed();
            }
        }
    };

    public MultiSenseService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mScanHelper = ScanHelper.getInstance(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        switch (action) {
            case INTENT_ACTION_START:
                handleStartIntent();
                break;
            case INTENT_ACTION_STOP:
                Log.d(TAG, "stop intent");
                handleStopIntent();
                break;
            default:
                Log.e(TAG, "unknown intent!");
        }

        return Service.START_REDELIVER_INTENT;
    }

    private void handleStartIntent() {
        startScan();
        activateAtom();
    }

    private void handleStopIntent() {
        stopScan();
        stopSelf();
    }

    private void startScan() {
        mScanHelper.startScanning(scanListener);
    }

    private void stopScan() {
        deactivateAtom();
        mScanHelper.stopScanning();
    }

    public void activateAtom() {
        Configuration configuration = Configuration.getInstance(this);
        String address = configuration.getDeviceAddress();


        if (mAtom != null && mAtom.getAddress().equals(address)) {
            Log.d(TAG, String.format("activateAtom: %s already active", address));
            return;
        }

        deactivateAtom();

        Log.d(TAG, "activateAtom: activating " + address);
        MultiSenseAtomManager manager = MultiSenseAtomManager.getInstance(this);
        mAtom = manager.getMultiSenseAtom(address);
        if (mAtom == null) {
            mAtom = manager.createMultiSenseDevice(address);
        }

        mAtom.activate();
        configAtom();
    }

    private void configAtom() {
        mAtom.disableAccelerometerSensor(null);
        mAtom.disableTemperatureSensor(null);
        mAtom.disableHumiditySensor(null);
        mAtom.disableLightSensor(null);
        mAtom.disableMagnetSensor(null);

        mAtom.setProximityTimer(IMultiSenseAtom.MAX_TIMER_VALUE, null);
        mAtom.setAlertTimer(IMultiSenseAtom.MAX_ALERT_TIMER_VALUE, null);
        mAtom.setViolationTimer(IMultiSenseAtom.MIN_TIMER_VALUE, null);

        mAtom.enableTxOnViolationOnlyMode(null);
        mAtom.setButtonPressedListener(buttonPressedListener);
    }

    public void deactivateAtom() {
        if (mAtom != null) {
            Log.d(TAG, "deactivateAtom: deactivating " + mAtom.getAddress());
            mAtom.removeButtonPressedListener(buttonPressedListener);
            mAtom.deactivate();
            mAtom = null;
        }
    }

    private void onButtonPressed() {
        Configuration configuration = Configuration.getInstance(this);
        @Constants.Actions int action = configuration.getAction();
        final Contact contact = configuration.getContact();

        Log.d(TAG, "button pressed!");
        switch (action) {
            case Constants.Actions.CALL:
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callNumber(contact);
                    }
                });
                break;
            case Constants.Actions.SMS:
                sendSms(contact);
                break;
            default:
                Log.e(TAG, "unknown action!");
        }
    }

    private void callNumber(Contact contact) {
        Log.d(TAG, "callNumber() called with: " + "contact = [" + contact + "]");
        boolean canPerformAction = canPerformAction(Constants.Actions.CALL);
        Log.d(TAG, "canPerformAction = " + canPerformAction);
        if (canPerformAction) {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + contact.getPhone()));
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                Log.d(TAG, "trying to call");
                startActivity(callIntent);
                uploadActionToCloud(contact, "Call");

                Log.d(TAG, "callNumber: enabling speaker and registering listener");
                TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                ListenToPhoneState phoneStateListener = new ListenToPhoneState();
                tManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

                Log.d(TAG, "called!");
            } catch (Exception e) {
                Log.e(TAG, "error calling", e);
            }
        }
    }

    private boolean canPerformAction(@Constants.Actions int action) {
        NotificationsManager notificationsManager = NotificationsManager.getInstance(this);
        Intent intent = MainActivity.createIntent(this);

        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        boolean isSimReady = manager.getSimState() == TelephonyManager.SIM_STATE_READY;
        Log.d(TAG, "isSimReady = " + isSimReady);
        if (!isSimReady) {
            notificationsManager.notifyNoSim(intent);
            return false;
        }

        boolean hasPermission = checkPermission(action);
        Log.d(TAG, "hasPermission = " + hasPermission);
        if (!hasPermission) {
            notificationsManager.notifyNoPermission(intent);
            return false;
        }

        return true;
    }

    private void sendSms(Contact contact) {
        Log.d(TAG, "sendSms() called with: " + "contact = [" + contact + "]");
        if (canPerformAction(Constants.Actions.SMS)) {
            String smsBody = getString(R.string.sms_body);
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contact.getPhone(), null, smsBody, null, null);
            Log.d(TAG, String.format("sent SMS to %s: %s", contact.getPhone(), smsBody));
            uploadActionToCloud(contact, "SMS");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MultiSenseService.this, getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadActionToCloud(Contact contact, String action) {
        LocationHelper locationHelper = LocationHelper.getInstance(this);

        String data = contact.getPhone();
        Location lastKnownLocation = locationHelper.getLastKnownLocation();
        if (lastKnownLocation != null) {
            data = String.format("%s, %s, %s", data, lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        }

        Log.d(TAG, String.format("uploadActionToCloud: sent event: %s, %s, %s", contact.getName(), action, data));
        mAtom.sendEvent(contact.getName(), action, data);
    }

    private boolean checkPermission(@Constants.Actions int action) {
        boolean hasPermission = true;
        String permission = Constants.permissions[action];
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            hasPermission = false;
        }

        return hasPermission;
    }

    private class ListenToPhoneState extends PhoneStateListener {

        private int prevState = TelephonyManager.CALL_STATE_IDLE;

        public ListenToPhoneState() {
            super();
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.d(TAG, "onCallStateChanged() called with: " + "state = [" + state + "], incomingNumber = [" + incomingNumber + "]");
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    prevState = TelephonyManager.CALL_STATE_RINGING;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    prevState = TelephonyManager.CALL_STATE_OFFHOOK;
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (prevState != TelephonyManager.CALL_STATE_IDLE) {
                        Log.d(TAG, "onCallStateChanged: disabling speaker and unregistering listener");
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(false);
                        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                        tManager.listen(this, PhoneStateListener.LISTEN_NONE);
                    }

                    prevState = TelephonyManager.CALL_STATE_IDLE;
                    break;
                default:
                    break;
            }
        }
    }
}
