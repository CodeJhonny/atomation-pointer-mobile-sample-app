package net.atomation.pointerdemo.utis;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import net.atomation.pointerdemo.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Class responsible for sending and stopping all the app's notifications
 * Created by eyal on 02/08/2016.
 */
public class NotificationsManager {

    @DrawableRes
    private static final int sLargeIcon = R.drawable.icon_multisense;

    @DrawableRes
    private static final int sSmallIcon = R.drawable.icon_multisense;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NotificationType.SCAN,
            NotificationType.ACTION_ERROR
    })
    private @interface NotificationType {
        int SCAN = 1;
        int ACTION_ERROR = 2;
    }

    private static NotificationsManager sInstance;

    private Context mContext;

    public static NotificationsManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new NotificationsManager(context.getApplicationContext());
        }

        return sInstance;
    }

    private NotificationsManager(Context context) {
        mContext = context;
    }

    @NotificationType
    public int getScanNotificationId() {
        return NotificationType.SCAN;
    }

    public Notification getScanNotification(Intent intent) {
        Resources resources = mContext.getResources();
        String notificationTitle = resources.getString(R.string.notification_title_scanning);
        String notificationText = resources.getString(R.string.notification_text_scanning);
        return buildNotification(notificationTitle, notificationText, intent);
    }

    public void notifyNoSim(Intent intent) {
        Resources resources = mContext.getResources();
        String notificationText = resources.getString(R.string.notification_text_no_sim);
        sendActionErrorNotification(notificationText, intent);
    }

    public void notifyNoNetwork(Intent intent) {
        Resources resources = mContext.getResources();
        String notificationText = resources.getString(R.string.notification_text_no_network);
        sendActionErrorNotification(notificationText, intent);
    }

    public void notifyNoPermission(Intent intent) {
        Resources resources = mContext.getResources();
        String notificationText = resources.getString(R.string.notification_text_no_permission);
        sendActionErrorNotification(notificationText, intent);
    }

    private void sendActionErrorNotification(String notificationText, Intent intent) {
        Resources resources = mContext.getResources();
        String notificationTitle = resources.getString(R.string.notification_title_operation_failed);

        sendNotification(NotificationType.ACTION_ERROR, notificationTitle, notificationText, intent);
    }

    private void sendNotification(int id, String title, String text, Intent intent) {
        Notification notification = buildNotification(title, text, intent);

        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, notification);
    }

    private Notification buildNotification(String title, String text, Intent intent) {
        Resources resources = mContext.getResources();

        Bitmap bm = BitmapFactory.decodeResource(resources, sLargeIcon);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(sSmallIcon)
                .setLargeIcon(bm)
                .setContentTitle(title)
                .setContentText(text);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(intent.getComponent());
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        return builder.build();
    }
}
