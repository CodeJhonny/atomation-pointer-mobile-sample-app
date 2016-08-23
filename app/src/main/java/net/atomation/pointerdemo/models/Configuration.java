package net.atomation.pointerdemo.models;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class containing all the app's configuration parameters
 * Created by eyal on 16/08/2016.
 */
public class Configuration {

    private static final String SHARED_PREFERENCES_KEY = "sp_configuration_key";
    private static final String DEVICE_ADDRESS_KEY = "device_address_key";
    private static final String ACTION_KEY = "action_key";
    private static final String NAME_KEY = "name_key";
    private static final String PHONE_NUMBER_KEY = "phone_number_key";

    private static Configuration sInstance;
    private final SharedPreferences mSharedPreferences;

    public static Configuration getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Configuration(context.getApplicationContext());
        }

        return sInstance;
    }

    private String mDeviceAddress;

    private Contact mContact;

    @Constants.Actions
    private int mAction;

    public Configuration(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        load();
    }

    private void load() {
        mDeviceAddress = mSharedPreferences.getString(DEVICE_ADDRESS_KEY, null);
        String phone = mSharedPreferences.getString(PHONE_NUMBER_KEY, null);
        String name = mSharedPreferences.getString(NAME_KEY, null);
        mContact = phone != null ? new Contact(name, phone) : null;
        //noinspection WrongConstant
        mAction = mSharedPreferences.getInt(ACTION_KEY, Constants.Actions.CALL);
    }

    public void save() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(DEVICE_ADDRESS_KEY, mDeviceAddress)
                .putString(NAME_KEY, mContact.getName())
                .putString(PHONE_NUMBER_KEY, mContact.getPhone())
                .putInt(ACTION_KEY, mAction)
                .apply();
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.mDeviceAddress = deviceAddress;
    }

    public Contact getContact() {
        return mContact;
    }

    public void setContact(Contact contact) {
        this.mContact = contact;
    }

    @Constants.Actions
    public int getAction() {
        return mAction;
    }

    public void setAction(@Constants.Actions int action) {
        this.mAction = action;
    }
}
