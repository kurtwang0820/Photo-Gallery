package com.ziliang.PhotoGallery;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Kurt on 4/13/2015.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final int POLL_INTERVAL = 1000 * 60 * 5;
    public static final String PREF_IS_ALARM_ON="isAlarmOn";
    public static final String ACTION_SHOW_NOTIFICATION="com.ziliang.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE="com.ziliang.PhotoGallery.PRIVATE";
    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
        if (!isNetworkAvailable) {
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences((this));
        String query = prefs.getString(FlickrFetcher.PREF_SEARCH_QUERY, null);
        String lastResultId = prefs.getString(FlickrFetcher.PREF_LAST_RESULT_ID, null);
        ArrayList<GalleryItem> items;
        if (query != null) {
            items = new FlickrFetcher().search(query);
        } else {
            items = new FlickrFetcher().fetchItems();
        }
        if (items.size() == 0) {
            return;
        }
        String resultId = items.get(0).getmId();
        if (!resultId.equals(lastResultId)) {
            Log.i(TAG, "Got a new result: " + resultId);
            Resources r = getResources();
            PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);
            Notification notification = new Notification.Builder(this).setTicker(r.getString(R.string.new_picture_title)).setSmallIcon(android.R.drawable.ic_menu_report_image).setContentTitle(r.getString(R.string.new_picture_title)).setContentText(r.getString(R.string.new_picture_text)).setContentIntent(pi).setAutoCancel(true).build();
//            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.notify(0, notification);
//            sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION),PERM_PRIVATE);
            showBackgroundNotification(0,notification);
        } else {
            Log.i(TAG, "Got an old result: " + resultId);
        }
        prefs.edit().putString(FlickrFetcher.PREF_LAST_RESULT_ID, resultId).commit();
        Log.i(TAG, "Received an intent: " + intent);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PollService.PREF_IS_ALARM_ON,isOn).commit();
    }

    public static boolean isServiceAlarm(Context context) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    void showBackgroundNotification(int requestCode,Notification notification){
        Intent i=new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE",requestCode);
        i.putExtra("NOTIFICATION",notification);
        sendOrderedBroadcast(i,PERM_PRIVATE,null,null,Activity.RESULT_OK,null,null);
    }
}
