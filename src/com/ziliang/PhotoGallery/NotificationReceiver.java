package com.ziliang.PhotoGallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Kurt on 4/13/2015.
 */
public class NotificationReceiver extends BroadcastReceiver {
//    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent i) {
//        Log.i(TAG, "Received result: " + getResultCode());
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }
        int requestCode = i.getIntExtra("REQUEST_CODE", 0);
        Notification notification = (Notification) i.getParcelableExtra("NOTIFICATION");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(requestCode, notification);
    }
}
