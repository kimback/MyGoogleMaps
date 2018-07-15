package com.bluesweater.mygooglemaps.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bluesweater.mygooglemaps.AdminMapActivity;
import com.bluesweater.mygooglemaps.Constants;
import com.bluesweater.mygooglemaps.R;

/**
 * Created by kimback on 2017. 12. 9..
 */

public class ForegroundService extends Service{

    private static final String LOG_TAG = "ForegroundService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Start Foreground Intent ");

            Intent notificationIntent = new Intent(this, AdminMapActivity.class);
            notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            //Intent previousIntent = new Intent(this, ForegroundService.class);
            //previousIntent.setAction(Constants.ACTION.PREV_ACTION);

            //PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                   //previousIntent, 0);

            Bitmap icon = BitmapFactory.decodeResource(getResources(),
                    R.drawable.img_user);

            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("SNOW GOGO")
                    .setTicker("눈밥쓰 측정중 . . . ")
                    .setContentText("백그라운드 동작중")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setLargeIcon(
                            Bitmap.createScaledBitmap(icon, 128, 128, false))
                    .setContentIntent(pendingIntent)
                    .setOngoing(true).build();
                    //.addAction(android.R.drawable.ic_media_previous,
                            //"Previous", ppreviousIntent).build();

            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);

        } else if (intent.getAction().equals(Constants.ACTION.FOREGROUND_CLICK_ACTION)) {
            Log.i(LOG_TAG, "Clicked");

        } else if (intent.getAction().equals(Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("SERVICE TAG ", "====foreground end===");
    }
}
