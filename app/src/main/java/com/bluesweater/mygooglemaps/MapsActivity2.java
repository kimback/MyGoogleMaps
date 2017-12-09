package com.bluesweater.mygooglemaps;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.GeofenceErrorMessages;
import com.bluesweater.mygooglemaps.core.GeofenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 *
 * 1. M(mashmallow) 버전 이후에 퍼미션 체크 관련 (ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
 * 2. GPS, NETWORK 체크 관련
 * 3. 관련 권한들 체크후에 connect 필수
 * 4. connect 후의  onConnect 에서의 mapStart
 * 5. 후에 MapReady에서의 콜백 함수들 작업
 *
 */
public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private Activity mActivity;
    private static final String TAG = MainActivity.class.getSimpleName();

    //구글 맵 api관련
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    LocationRequest locationRequest;

    //request code
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;

    //interval set
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    //geoTaskEnum
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE

    }

    //퍼미션관련
    boolean askPermissionOnceAgain = false;
    private boolean permissionChecked = false;

    private static final double EARTH_RADIUS = 6378100.0;
    private Marker currentMarker;
    private int offset;
    boolean mRequestingLocationUpdates = false;
    Location mCurrentLocatiion;
    boolean mMoveMapByUser = true;
    boolean mMoveMapByAPI = true;

    // 지오펜싱관련
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
    private GeoMainActivity geofencesExecute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Log.d(TAG, "onCreate");

        mActivity = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get the UI widgets.
        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        mAddGeofencesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                addGeoFences(view);
            }
        });
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
        mRemoveGeofencesButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                removeGeoFences(view);
            }
        });

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public void onResume() {

        super.onResume();

        //연결체크 후 위치정보 스타트
        if (mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onResume : call startLocationUpdates");
            //업데이트 되고 있지 않다면 시작해라
            if (!mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();
            }
        }
    }


    //위치 정보 시작
    private void startLocationUpdates() {

        //gps, network 활성화 상태 확인후
        if (!checkLocationServicesStatus()) {

            // 퍼미션 체크후

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

        }else {

            //퍼미션체크
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                permissionChecked = false;
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }


            permissionChecked = true;
            Log.d(TAG, "startLocationUpdates : call FusedLocationApi.requestLocationUpdates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
            mRequestingLocationUpdates = true;

            mGoogleMap.setMyLocationEnabled(true);


        }

    }



    private void stopLocationUpdates() {

        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

            @Override
            public boolean onMyLocationButtonClick() {

                Log.d( TAG, "onMyLocationButtonClick : 위치에 따른 카메라 이동 활성화");
                mMoveMapByAPI = true;
                return true;
            }
        });

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :" + latLng);
            }
        });

        mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

            @Override
            public void onCameraMoveStarted(int i) {

                if (mMoveMapByUser == true && mRequestingLocationUpdates){

                    Log.d(TAG, "onCameraMove : 위치에 따른 카메라 이동 비활성화");
                    mMoveMapByAPI = false;
                }

                mMoveMapByUser = true;

            }
        });


        mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

            @Override
            public void onCameraMove() {


            }
        });

        geofencesExecute = new GeoMainActivity();

    }


    @Override
    public void onLocationChanged(Location location) {


        Log.d(TAG, "onLocationChanged : ");

        String markerTitle = getCurrentAddress(location);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());

        Log.i("LOCTAG",markerSnippet.toString());

        //현재 위치에 마커 생성하고 이동
        setCurrentLocation(location, markerTitle, markerSnippet);

        mCurrentLocatiion = location;
    }


    @Override
    protected void onStart() {

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected() == false){

            Log.d(TAG, "onStart: mGoogleApiClient connect");
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {

        if (mRequestingLocationUpdates) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            stopLocationUpdates();
        }

        if ( mGoogleApiClient.isConnected()) {

            Log.d(TAG, "onStop : mGoogleApiClient disconnect");
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {


        if ( mRequestingLocationUpdates == false ) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {

                    ActivityCompat.requestPermissions(mActivity,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

                } else {

                    Log.d(TAG, "onConnected : 퍼미션 가지고 있음");
                    Log.d(TAG, "onConnected : call startLocationUpdates");
                    startLocationUpdates();
                    mGoogleMap.setMyLocationEnabled(true);
                }

            }else{

                Log.d(TAG, "onConnected : call startLocationUpdates");
                startLocationUpdates();
                mGoogleMap.setMyLocationEnabled(true);
            }
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d(TAG, "onConnectionFailed");
        setDefaultLocation();
    }


    @Override
    public void onConnectionSuspended(int cause) {

        Log.d(TAG, "onConnectionSuspended");
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }


    public String getCurrentAddress(Location location) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    //체크 gps, network 활성화여부
    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        mMoveMapByUser = false;

        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //구글맵의 디폴트 현재 위치는 파란색 동그라미로 표시
        //마커를 원하는 이미지로 변경하여 현재 위치 표시하도록 수정해야함.
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentMarker = mGoogleMap.addMarker(markerOptions);


        if ( mMoveMapByAPI ) {

            Log.d( TAG, "setCurrentLocation :  mGoogleMap moveCamera "
                    + location.getLatitude() + " " + location.getLongitude() ) ;
            // CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 15);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);
        }
    }


    public void setDefaultLocation() {

        mMoveMapByUser = false;


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {

        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "checkPermissions : 퍼미션 가지고 있음");
            permissionChecked = true;

            if ( mGoogleApiClient.isConnected() == false) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {


                if ( mGoogleApiClient.isConnected() == false) {

                    Log.d(TAG, "onRequestPermissionsResult : mGoogleApiClient connect");
                    mGoogleApiClient.connect();
                }



            } else {

                checkPermissions();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity2.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity2.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }


    // GPS 활성화 셋팅
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity2.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {

                    //현재 연결되어 있지 않다면 연결하라
                    Log.d(TAG, "onActivityResult : ===gps, network 활성화됨===");
                    if (!mGoogleApiClient.isConnected()) {

                        Log.d( TAG, "onActivityResult :=== mGoogleApiClient connect ===");
                        mGoogleApiClient.connect();
                    }
                    return;
                }

                break;
        }
    }


    //===========================================================================
    //===========================GEO FENCES LINE=================================
    //===========================================================================

    public class GeoMainActivity implements OnCompleteListener<Void> {

        /**
         * Provides access to the Geofencing API.
         */
        private GeofencingClient mGeofencingClient;

        /**
         * The list of geofences used in this sample.
         */
        private ArrayList<Geofence> mGeofenceList;

        /**
         * Used when requesting to add or remove geofences.
         */
        private PendingIntent mGeofencePendingIntent;

        private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;


        public GeoMainActivity(){
            // Empty list for storing geofences.
            mGeofenceList = new ArrayList<>();

            // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
            mGeofencePendingIntent = null;

            setButtonsEnabledState();

            // Get the geofences used. Geofence data is hard coded in this sample.
            populateGeofenceList();

            mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity2.this);

            if (permissionChecked) {
                performPendingGeofenceTask();
            }

            MarkerOptions options = new MarkerOptions();
            options.position(getCoords(Constants.BAY_AREA_LANDMARKS.get("HOME").latitude,
                    Constants.BAY_AREA_LANDMARKS.get("HOME").longitude));
            options.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(Constants.BAY_AREA_LANDMARKS.get("HOME").latitude,
                    Constants.BAY_AREA_LANDMARKS.get("HOME").longitude)));
            mGoogleMap.addMarker(options);

        }


        /**
         * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
         * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
         * user has added geofences.
         */
        private void setButtonsEnabledState() {
            if (getGeofencesAdded()) {
                mAddGeofencesButton.setEnabled(false);
                mRemoveGeofencesButton.setEnabled(true);
            } else {
                mAddGeofencesButton.setEnabled(true);
                mRemoveGeofencesButton.setEnabled(false);
            }
        }





        /**
         * Returns true if geofences were added, otherwise false.
         */
        private boolean getGeofencesAdded() {
            return PreferenceManager.getDefaultSharedPreferences(MapsActivity2.this).getBoolean(
                    Constants.GEOFENCES_ADDED_KEY, false);
        }


        /**
         * Performs the geofencing task that was pending until location permission was granted.
         */
        private void performPendingGeofenceTask() {
            if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
                addGeofences();
            } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
                removeGeofences();
            }
        }


        /**
         * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
         * Also specifies how the geofence notifications are initially triggered.
         */
        private GeofencingRequest getGeofencingRequest() {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

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
            if (!permissionChecked) {
                showSnackbar(getString(R.string.insufficient_permissions));
                return;
            }

            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this);
        }



        /**
         * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
         * specified geofences. Handles the success or failure results returned by addGeofences().
         */
        public void addGeofencesButtonHandler(View view) {
            if (permissionChecked) {
                addGeofences();
            }

        }

        /**
         * Removes geofences. This method should be called after the user has granted the location
         * permission.
         */
        @SuppressWarnings("MissingPermission")
        private void removeGeofences() {
            if (!permissionChecked) {
                showSnackbar(getString(R.string.insufficient_permissions));
                return;
            }

            mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
        }

        /**
         * Removes geofences, which stops further notifications when the device enters or exits
         * previously registered geofences.
         */
        public void removeGeofencesButtonHandler(View view) {
            if (permissionChecked) {
                removeGeofences();
            }

        }


        /**
         * Shows a {@link Snackbar} using {@code text}.
         *
         * @param text The Snackbar text.
         */
        private void showSnackbar(final String text) {
            View container = findViewById(android.R.id.content);
            if (container != null) {
                Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
            }
        }




        /*
         * @param added Whether geofences were added or removed.
         */
        private void updateGeofencesAdded(boolean added) {
            PreferenceManager.getDefaultSharedPreferences(MapsActivity2.this)
                    .edit()
                    .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                    .apply();
        }


        /**
         * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
         * is available.
         * @param task the resulting Task, containing either a result or error.
         */
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            mPendingGeofenceTask = PendingGeofenceTask.NONE;
            if (task.isSuccessful()) {
                updateGeofencesAdded(!getGeofencesAdded());
                setButtonsEnabledState();

                int messageId = getGeofencesAdded() ? R.string.geofences_added :
                        R.string.geofences_removed;
                Toast.makeText(MapsActivity2.this, getString(messageId), Toast.LENGTH_SHORT).show();
            } else {
                // Get the status code for the error and log it using a user-friendly message.
                String errorMessage = GeofenceErrorMessages.getErrorString(MapsActivity2.this, task.getException());
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
        private PendingIntent getGeofencePendingIntent() {
            // Reuse the PendingIntent if we already have it.
            if (mGeofencePendingIntent != null) {
                return mGeofencePendingIntent;
            }
            Intent intent = new Intent(MapsActivity2.this, GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            return PendingIntent.getService(MapsActivity2.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        /**
         * This sample hard codes geofence data. A real app might dynamically create geofences based on
         * the user's location.
         */
        private void populateGeofenceList() {
            for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {

                mGeofenceList.add(new Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence.
                        .setRequestId(entry.getKey())

                        // Set the circular region of this geofence.
                        .setCircularRegion(
                                entry.getValue().latitude,
                                entry.getValue().longitude,
                                Constants.GEOFENCE_RADIUS_IN_METERS
                        )

                        // Set the expiration duration of the geofence. This geofence gets automatically
                        // removed after this period of time.
                        .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT)

                        // Create the geofence.
                        .build());
            }
        }




    }

    private void addGeoFences(View v){
        geofencesExecute.addGeofencesButtonHandler(v);
    }


    private void removeGeoFences(View v){
        geofencesExecute.removeGeofencesButtonHandler(v);
    }

    // 2. convert meters to pixels between 2 points in current zoom:
    private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {
        double lat1 = radiusInMeters / EARTH_RADIUS;
        double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));
        double lat2 = lat + lat1 * 180 / Math.PI; double lng2 = lng + lng1 * 180 / Math.PI;
        Point p1 = mGoogleMap.getProjection().toScreenLocation(new LatLng(lat, lng));
        Point p2 = mGoogleMap.getProjection().toScreenLocation(new LatLng(lat2, lng2));
        return Math.abs(p1.x - p2.x);
    }

    // 3. bitmap creation:
    private Bitmap getBitmap(double lat, double lng) {
        //fill color
        Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG); paint1.setColor(0x110000FF);
        paint1.setStyle(Paint.Style.FILL);
        // stroke color
        Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG); paint2.setColor(0xFF0000FF);
        paint2.setStyle(Paint.Style.STROKE);
        // icon
        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher);
        // circle radius - 200 meters
        int radius = offset = convertMetersToPixels(lat, lng, 200);
        // if zoom too small
        if (radius < icon.getWidth() / 2) {
            radius = icon.getWidth() / 2;
        }

        // create empty bitmap
        Bitmap b = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        // draw blue area if area > icon size
        if (radius != icon.getWidth() / 2) {
            c.drawCircle(radius, radius, radius, paint1);
            c.drawCircle(radius, radius, radius, paint2);
        }

        // draw icon

        c.drawBitmap(icon, radius - icon.getWidth() / 2, radius - icon.getHeight() / 2, new Paint());
        return b;


    }

    // 4. calculate image offset:
    private LatLng getCoords(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        Projection proj = mGoogleMap.getProjection();
        Point p = proj.toScreenLocation(latLng);
        p.set(p.x, p.y + offset);

        return proj.fromScreenLocation(p);
    }





}
