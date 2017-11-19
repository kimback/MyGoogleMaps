package com.bluesweater.mygooglemaps;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMapActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private MapsPreference mapsPreference;

    //구글 맵 api관련
    public GeoMainActivity geofencesExecute;

    //ui
    private RelativeLayout loadingLayout;
    private RelativeLayout userViewsLayout;
    private LinearLayout btnLayout;
    private LinearLayout statusLayout;
    private Button btnStartGeo;
    private Button btnStopGeo;


    //geoTaskEnum
    private enum PendingGeofenceTask {
        ADD, ADDED, REMOVE, REMOVED, NONE
    }

    //fence info list
    public List<Map<String, Object>> fencesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        loadingLayout = (RelativeLayout) findViewById(R.id.loading_bar_layout);
        userViewsLayout = (RelativeLayout) findViewById(R.id.user_views);
        btnLayout = (LinearLayout) findViewById(R.id.btn_layout);
        statusLayout = (LinearLayout) findViewById(R.id.status_layout);
        btnStartGeo = (Button) findViewById(R.id.btn_start_geo);
        btnStopGeo = (Button) findViewById(R.id.btn_stop_geo);

        mActivity = this;

        if(loadingLayout != null){
            loadingLayout.setVisibility(View.VISIBLE);
        }
        if(btnLayout != null) {
            btnLayout.setVisibility(View.VISIBLE);
        }
        if(statusLayout != null) {
            statusLayout.setVisibility(View.GONE);
        }

        //측정시작
        btnStartGeo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                //지오펜스셋팅
                if(geofencesExecute == null){
                    geofencesExecute = new GeoMainActivity();
                }

                geofencesExecute.requestAddGeoFences();

                if(btnLayout != null) {
                    btnLayout.setVisibility(View.GONE);
                }
                if(statusLayout != null) {
                    statusLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //측정정지
        btnStopGeo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                if(geofencesExecute != null){
                   geofencesExecute.requestRemoveGeoFences();
               }

                if(btnLayout != null) {
                    btnLayout.setVisibility(View.VISIBLE);
                }
                if(statusLayout != null) {
                    statusLayout.setVisibility(View.GONE);
                }

            }
        });


        //펜스리스트 셋팅
        fencesList = new ArrayList<>();
        HashMap<String, Object> fenceMap = new HashMap<>();
        HashMap<String, Object> fenceMap2 = new HashMap<>();

        fenceMap.put("name","block1");
        fenceMap.put("latitude","37.438447610822294");
        fenceMap.put("longitude","126.68807230889799");

        fenceMap2.put("name","block2");
        fenceMap2.put("latitude","37.438437494782775");
        fenceMap2.put("longitude","126.68697830289602");

        fencesList.add(fenceMap);
        fencesList.add(fenceMap2);

        mapsPreference = ApplicationMaps.getMapsPreference();

        //지오펜싱 초기화
        geofencesExecute = new GeoMainActivity();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    //===========================================================================
    //===========================GEO FENCES LINE=================================
    //===========================================================================

    public class GeoMainActivity implements OnCompleteListener<Void> {

        private GeofencingClient mGeofencingClient;
        private ArrayList<Geofence> mGeofenceList;
        private PendingIntent mGeofencePendingIntent;
        private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


        public GeoMainActivity() {
            //리스트 초기화
            mGeofenceList = new ArrayList<>();
            mGeofencePendingIntent = null;

            //지오펜스 리스트 생성
            buildGeofenceList();

            //init
            mGeofencingClient = LocationServices.getGeofencingClient(UserMapActivity.this);


            if(loadingLayout != null){
                loadingLayout.setVisibility(View.GONE);
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
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this);
        }


        /**
         * Removes geofences. This method should be called after the user has granted the location
         * permission.
         */
        @SuppressWarnings("MissingPermission")
        private void removeGeofences() {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnCompleteListener(this);
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
                String errorMessage = GeofenceErrorMessages.getErrorString(UserMapActivity.this, task.getException());
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
            Intent intent = new Intent(UserMapActivity.this, GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            mGeofencePendingIntent = PendingIntent.getService(UserMapActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            return mGeofencePendingIntent;
        }

        //지오펜스 리스트 생성
        private void buildGeofenceList() {

            //펜스정보 리스트 만큼 펜스빌드 리스트 생성
            for (int i=0; i<fencesList.size(); i++){
                final Map<String, Object> block = fencesList.get(i);

                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(block.get("name").toString())

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                Double.parseDouble(block.get("latitude").toString()), //lati
                                Double.parseDouble(block.get("longitude").toString()),  //lot
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


    @Override
    protected void onDestroy() {
        super.onDestroy();

        //removeGeofences
        if(geofencesExecute != null) {
            geofencesExecute.requestRemoveGeoFences();
        }


    }


}
