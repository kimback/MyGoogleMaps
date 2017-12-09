/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bluesweater.mygooglemaps.core;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.bluesweater.mygooglemaps.R;
import com.bluesweater.mygooglemaps.WelcomeActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG = "GeofenceTransitionsIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
        Log.i("TAG_SERVICE","========geofencingService create ========");
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i("TAG_SERVICE","======== onHandleIntent ========");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);


            requestBlockDataUpdate(triggeringGeofences);



        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), WelcomeActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(WelcomeActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.img_selector)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.drawable.img_selector))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setVibrate(new long[] { 1000, 1000, 1000 })
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }


    private void requestBlockDataUpdate(List<Geofence> geolist){

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : geolist) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }

        JSONArray jsonArray = new JSONArray(triggeringGeofencesIdsList);

        Log.i("IntentServiceTag","jsonArray : " + jsonArray.toString());

        String urlStr = "";

        try {

            urlStr = ApplicationMaps.restApiUrl + "/geoDataUpdateByTrigger?skiResort="
                    + ApplicationMaps.getMapsPreference().getSelectedSkiResortCode()
                    + "&blockCodes=" + jsonArray.toString()
                    + "&userId=" + ApplicationMaps.getMapsPreference().getLoginId();
            getHttpData(urlStr); //서버통신

            //Log.i("LOGINTAG","doInBackground : " + authStr);
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    /**
     * 해당 페이지를 가져온다.
     * okHttp 를 사용하여 작성(새로운 쓰레드가 생성됨)
     * 메인쓰레드와 따로 생각해서 작성해야함
     *
     * getHttpData
     *
     * @param page
     * @return
     * @throws Exception
     */
    public void getHttpData(String page) {

        OkHttpClient client = new OkHttpClient();

        //request
        Request request = new Request.Builder()
                .url(page)
                .get()
                .build();

        try {
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e){
                    e.printStackTrace();

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //받은 정보들로 후처리 구현
                    //Log.i("IntentServiceTag",response.body().string());
                    postGeoDataProcess(response.body().string());

                }

            });

        } catch (Exception e) {
            e.printStackTrace();


        }finally {
            //final logic
        }

    }



    /**
     * callbackJoinProcess 인증후 콜백 메서드
     * @param resultStr
     */
    private void postGeoDataProcess(String resultStr){

        Log.i("IntentServiceTag","resultStr" + resultStr);

        String dataSuccess = "F";
        try {
            if(resultStr != null && !resultStr.equals("")){

                JSONArray jsonArray = new JSONArray(resultStr);
                //정보[0]
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = jsonArray.getJSONObject(i);

                    if(jo.getString("result").equals("1")){
                        dataSuccess = "T";
                        Log.i("IntentServiceTag", "===눈밥 트리거 포인트 업데이트 완료===");
                    }

                }

            }

            if(dataSuccess.equals("T")){
                //t
            }

        }catch(Exception e){
            e.printStackTrace();

        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i("TAG_SERVICE","======== onDestroy ========");


    }
}
