package net.atomation.pointerdemo.views;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import net.atomation.pointerdemo.R;
import net.atomation.pointerdemo.models.Configuration;
import net.atomation.pointerdemo.models.Constants;
import net.atomation.pointerdemo.models.Contact;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String TAG = ConfigurationActivity.class.getSimpleName();

    private static final String INTENT_EXTRA_DEVICE_ADDRESS = "intent_extra_device_address";
    private static final int PICK_CONTACT_REQUEST = 1;

    private EditText edtPhoneNumber;
    private Spinner spnAction;
    private String deviceAddress;
    private TextInputLayout tilPhoneNumber;
    private Contact mContact = null;

    public static Intent createIntent(Context context, String address) {
        return new Intent(context, ConfigurationActivity.class)
                .putExtra(INTENT_EXTRA_DEVICE_ADDRESS, address);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        handleIntent(getIntent());

        ImageButton button = (ImageButton) findViewById(R.id.btn_pick_contact);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickContact();
            }
        });

        tilPhoneNumber = (TextInputLayout) findViewById(R.id.til_configuration_phone_number);

        Configuration configuration = Configuration.getInstance(this);
        Contact contact = configuration.getContact();
        edtPhoneNumber = (EditText) findViewById(R.id.edt_phone_number);

        if (contact != null) {
            edtPhoneNumber.setText(contact.getPhone());
        }

        spnAction = (Spinner) findViewById(R.id.spn_configuration_action);
        ArrayAdapter<CharSequence> actionAdapter = ArrayAdapter.createFromResource(this, R.array.actions, R.layout.centered_spinner_item);
        actionAdapter.setDropDownViewResource(R.layout.centered_spinner_item);
        spnAction.setAdapter(actionAdapter);
        spnAction.setSelection(configuration.getAction());

        Button btnSave = (Button) findViewById(R.id.btn_configuration_done);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfiguration();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri contactUri = data.getData();
                GetContactPhoneTask task = new GetContactPhoneTask();
                task.execute(contactUri);
            }
        }
    }

    private void handleIntent(Intent intent) {
        deviceAddress = intent.getStringExtra(INTENT_EXTRA_DEVICE_ADDRESS);
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    private void saveConfiguration() {
        String phone = edtPhoneNumber.getText().toString();
        boolean isValidPhone = Patterns.PHONE.matcher(phone).matches();

        tilPhoneNumber.setErrorEnabled(false);
        if (isValidPhone) {
            @Constants.Actions int action = spnAction.getSelectedItemPosition();
            Log.d(TAG, String.format("saved configuration: phone - %s, action - %d", phone, action));

            if (mContact == null) {
                mContact = new Contact("", phone);
            }

            Configuration configuration = Configuration.getInstance(this);
            configuration.setDeviceAddress(deviceAddress);
            configuration.setAction(action);

            Contact savedContact = configuration.getContact();
            if (!(savedContact != null && savedContact.getPhone().equals(mContact.getPhone()))) {
                configuration.setContact(mContact);
            }

            configuration.save();

            Intent intent = MainActivity.createIntent(this);
            startActivity(intent);
        } else {
            tilPhoneNumber.setError(getString(R.string.error_invalid_phone_number));
            tilPhoneNumber.setErrorEnabled(true);
        }
    }

    private class GetContactPhoneTask extends AsyncTask<Uri, Void, Contact> {

        @Override
        protected Contact doInBackground(Uri... uris) {
            Uri contactUri = uris[0];
            Contact contact = null;
            String[] projection = {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int columnName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String name = cursor.getString(columnName);
                int columnPhone = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phone = cursor.getString(columnPhone);
                contact = new Contact(name, phone);
                cursor.close();
            }

            return contact;
        }

        @Override
        protected void onPostExecute(Contact contact) {
            Log.d(TAG, "contact: " + contact);

            if (contact != null) {
                mContact = contact;
                edtPhoneNumber.setText(contact.getPhone());
            }
        }
    }
}
