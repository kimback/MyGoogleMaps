package com.bluesweater.mygooglemaps.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by kimback on 2017. 11. 20..
 */

public class GeofencingExecutor implements OnCompleteListener<Void> {

    Context context;

    //geoTaskEnum
    private enum PendingGeofenceTask {
        ADD, ADDED, REMOVE, REMOVED, NONE
    }

    //fence info list
    public List<Map<String, Object>> fencesList;

    private GeofencingClient mGeofencingClient;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


    public GeofencingExecutor(Context context, List fencesList) {
        Log.i("TAG_GeofencingExecutor","===GeofencingExecutor create ===");

        this.context = context;

        //리스트 초기화
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
        this.fencesList = fencesList;


        //지오펜스 리스트 생성
        buildGeofenceList();

        //init
        mGeofencingClient = LocationServices.getGeofencingClient(context);

        //if(context instanceof UserMapActivity){
            //UserMapActivity userMapActivity = (UserMapActivity) context;
            //userMapActivity.hideLoadingBar();
        //}else if(context instanceof AdminMapActivity){
            //admin...
        //}

        //권한 확인후
        //M 이상에서 권한 관련 적용됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ApplicationMaps.getApps().isFineLocationPermit()
                    && ApplicationMaps.getApps().isCoarseLocationPermit()) {
                //시작
                mPendingGeofenceTask = PendingGeofenceTask.ADD;
                performPendingGeofenceTask();
            }else{
                //권한이 없다
            }
        }else{
            //시작
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            performPendingGeofenceTask();
        }

    }

    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }


    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER |
                GeofencingRequest.INITIAL_TRIGGER_EXIT);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }


    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if(mGeofencingClient != null) {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this);
        }
    }


    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if(mGeofencingClient != null){
            mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnCompleteListener(this);
        }
    }

    public void requestAddGeoFences(){

        //권한 확인후
        if (ApplicationMaps.getApps().isFineLocationPermit()
                && ApplicationMaps.getApps().isCoarseLocationPermit()) {
            //시작
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            performPendingGeofenceTask();
        }
    }

    public void requestRemoveGeoFences() {

        mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
        performPendingGeofenceTask();

    }


    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     *
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {

            //callback logic
            if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
                mPendingGeofenceTask = PendingGeofenceTask.ADDED;
                Log.i(TAG, "===onComplete=== : 지오펜스 추가 완료");
            } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
                mPendingGeofenceTask = PendingGeofenceTask.REMOVED;
                Log.i(TAG, "===onComplete=== : 지오펜스 제거 완료");
            }

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(context, task.getException());
            Log.w(TAG, errorMessage);
        }

    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    //지오펜스 인텐트 서비스 셋팅
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    //지오펜스 리스트 생성
    private void buildGeofenceList() {


        //펜스정보 리스트 만큼 펜스빌드 리스트 생성
        for (int i=0; i<fencesList.size(); i++){
            final Map<String, Object> block = fencesList.get(i);

            String blockCode = (String)block.get("blockCode");
            double dLati = Double.parseDouble((String)block.get("latitude"));
            double dLongi = Double.parseDouble((String)block.get("longitude"));


            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(blockCode)

                    // Set the circular region of this geofence.
                    .setCircularRegion(

                            dLati, //lati
                            dLongi,  //lot
                            20 //meter
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.

                    //12 시간후에 삭제 (수정해야함)
                    .setExpirationDuration(12 * 60 * 60 * 1000)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }

    }


}
