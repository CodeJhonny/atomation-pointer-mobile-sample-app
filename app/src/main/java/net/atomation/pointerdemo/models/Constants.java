package net.atomation.pointerdemo.models;

import android.Manifest;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Constants used in the application
 * Created by eyal on 15/08/2016.
 */
public class Constants {

    public static String[] permissions = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS
    };

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Constants.Actions.CALL, Constants.Actions.SMS})
    public @interface Actions {
        int CALL = 0;
        int SMS = 1;
    }
}
