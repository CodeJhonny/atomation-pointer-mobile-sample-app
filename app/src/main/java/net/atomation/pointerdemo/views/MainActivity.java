package net.atomation.pointerdemo.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.atomation.pointerdemo.R;
import net.atomation.pointerdemo.models.Configuration;
import net.atomation.pointerdemo.models.Constants;
import net.atomation.pointerdemo.models.Contact;
import net.atomation.pointerdemo.models.MultiSenseService;
import net.atomation.pointerdemo.utis.LocationHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean checkedPermission = false;

    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Configuration configuration = Configuration.getInstance(this);
        Contact contact = configuration.getContact();
        @Constants.Actions int action = configuration.getAction();
        String actionStr = getResources().getStringArray(R.array.main_actions)[action];
        String titleFormat = getString(R.string.main_title_format);
        String contactStr = !TextUtils.isEmpty(contact.getName()) ? contact.getName() : contact.getPhone();
        String title = String.format(titleFormat, actionStr, contactStr);
        TextView tvTitle = (TextView) findViewById(R.id.tv_main_title);
        tvTitle.setText(title);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() called with: " + "");
        super.onResume();
        LocationHelper.getInstance(this).askForLocationIfNeeded(this);
        Configuration configuration = Configuration.getInstance(this);

        if (!checkedPermission) {
            requestPermissionIfNeeded(configuration.getAction());
        }

        checkedPermission = !checkedPermission;

        Intent startIntent = MultiSenseService.createStartIntent(this);

        startService(startIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.Actions.CALL:
            case Constants.Actions.SMS:
                boolean hasPermission = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                Log.d(TAG, "has permission: " + hasPermission);
                break;
            default:
                Log.d(TAG, "unknown permission result");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.menu_action_switch_device:
                Log.d(TAG, "switch device");
                intent = MultiSenseService.createStopIntent(this);
                startService(intent);
                intent = ScanActivity.createIntent(this);
                startActivity(intent);
                break;
            case R.id.menu_action_settings:
                Log.d(TAG, "settings");
                Configuration configuration = Configuration.getInstance(this);
                intent = ConfigurationActivity.createIntent(this, configuration.getDeviceAddress());
                startActivity(intent);
                break;
            case R.id.menu_action_exit:
                Log.d(TAG, "exit");
                intent = MultiSenseService.createStopIntent(this);
                startService(intent);
                finish();
                break;
            default:
                Log.e(TAG, "unknown action");
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean requestPermissionIfNeeded(@Constants.Actions int action) {
        boolean hasPermission = true;
        String permission = Constants.permissions[action];
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            hasPermission = false;
            ActivityCompat.requestPermissions(this, new String[]{permission}, action);
        }

        return hasPermission;
    }
}
