package com.bluesweater.mygooglemaps;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminMapActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private MapsPreference mapsPreference;
    private GoogleMapPakage googleMapPakage;

    //구글 맵 api관련
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;
    public GeoMainActivity geofencesExecute;

    //interval set
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    //flag
    private boolean mRequestingLocationUpdates = false;

    //ui
    private RelativeLayout loadingLayout;

    //geoTaskEnum
    private enum PendingGeofenceTask {
        ADD, ADDED, REMOVE, REMOVED, NONE
    }

    //fence info list
    public List<Map<String, Object>> fencesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_map);

        loadingLayout = (RelativeLayout) findViewById(R.id.loading_bar_layout);
        mActivity = this;

        if(loadingLayout != null){
            loadingLayout.setVisibility(View.VISIBLE);
        }

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
        googleMapPakage = new GoogleMapPakage();


        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(googleMapPakage)
                .addOnConnectionFailedListener(googleMapPakage)
                .addApi(LocationServices.API)
                .build();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(googleMapPakage);

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


    //위치정보 서비스 시작
    private void startLocationMachine() throws SecurityException{

        if(ApplicationMaps.getPermissionsMachine().locationPermissionCheck()){

            //아래의 셋팅후 부터 onChangeUpdate를 타게 된다
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, googleMapPakage);
            mRequestingLocationUpdates = true;
            mGoogleMap.setMyLocationEnabled(true);

        }else{
            Log.d(TAG, "===startLocationMachine=== : 위치정보 권한 없음 ");
            return;
        }

    }


    //위치 정보 서비스 중지
    private void stopLocationUpdates() {

        Log.d(TAG,"stopLocationUpdates : LocationServices.FusedLocationApi.removeLocationUpdates");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, googleMapPakage);
        mRequestingLocationUpdates = false;
    }


    /**
     * ================ map 관련 리스너 ==========================
     */
    public class GoogleMapPakage implements OnMapReadyCallback,
                            GoogleApiClient.ConnectionCallbacks,
                            GoogleApiClient.OnConnectionFailedListener,
                            LocationListener {

        //현재위치정보저장
        Location location = null;
        String markerTitle = "";
        String markerSnippet = "";

        //frag
        public boolean mMoveMapByUser = true;
        public boolean mMoveMapByAPI = true;


        @Override
        public void onMapReady(GoogleMap googleMap) {
            Log.d(TAG, "====onMapReady====");
            mGoogleMap = googleMap;

            //mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

            mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){

                @Override
                public boolean onMyLocationButtonClick() {
                    Log.i(TAG,"===onMyLocationButtonClick===");

                    if(location != null && !markerTitle.equals("") && !markerSnippet.equals("")) {
                        mMoveMapByAPI = true;

                        //onchange에서 저장한 위치정보로 이동한다
                        setCurrentLocation(location, markerTitle, markerSnippet);

                    }

                    return true;
                }
            });

            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                @Override
                public void onMapClick(LatLng latLng) {
                    Log.i(TAG,"===onMapClick=== : " + latLng);
                }
            });

            mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {

                @Override
                public void onCameraMoveStarted(int i) {
                    Log.i(TAG,"===onCameraMoveStarted=== : " + i);
                    if (mMoveMapByUser == true && mRequestingLocationUpdates){

                        //사용자가 화면이동시 onChangedUpdate 콜백에서의 위치이동은 막는다
                        Log.d(TAG, "===onCameraMove=== : 카메라 이동 비활성화");
                        mMoveMapByAPI = false;
                    }

                    mMoveMapByUser = true;
                }
            });


            mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {

                @Override
                public void onCameraMove() {
                    Log.i(TAG,"===onCameraMove=== ");
                }
            });

            Log.d(TAG, "====onresume : connect start=====");
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }

        }

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "===onConnected===");

            if(loadingLayout != null){
                loadingLayout.setVisibility(View.GONE);
            }

            //업데이트 되고 있지 않다면 시작해라
            if (!mRequestingLocationUpdates) {
                try {

                    //지오펜싱설정
                    setGeoPencingLocation();
                    //위치 정보 스타트
                    startLocationMachine();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "===onConnectionFailed===");

        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "===onLocationChanged=== : " + location.getLatitude() + "/" + location.getLongitude());

            mMoveMapByUser = false;

            //위치정보 바뀔때마다 저장
            this.location = location;
            markerTitle = getCurrentAddress(location);
            markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                    + " 경도:" + String.valueOf(location.getLongitude());

            if ( mMoveMapByAPI ) {
                setCurrentLocation(location, markerTitle, markerSnippet);
            }

        }

        @Override
        public void onConnectionSuspended(int cause) {
            if (cause == CAUSE_NETWORK_LOST) {
                Log.e(TAG, "===onConnectionSuspended=== : google play services connect lost >> NETWORK LOST");
            }else if (cause == CAUSE_SERVICE_DISCONNECTED){
                Log.e(TAG, "===onConnectionSuspended=== : google play services connect lost >> DISCONNECTED");
            }

        }


        //지오코딩 ( 위도경도를 주소로 바꾸어줌 )
        public String getCurrentAddress(Location location) {

            //지오코더... GPS를 주소로 변환
            Geocoder geocoder = new Geocoder(AdminMapActivity.this, Locale.getDefault());

            List<Address> addresses;

            try {

                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);
            } catch (IOException ioException) {
                //네트워크 문제
                Toast.makeText(AdminMapActivity.this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
                return "지오코더 서비스 사용불가";
            } catch (IllegalArgumentException illegalArgumentException) {
                Toast.makeText(AdminMapActivity.this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
                return "잘못된 GPS 좌표";

            }


            if (addresses == null || addresses.size() == 0) {
                Toast.makeText(AdminMapActivity.this, "주소 미발견", Toast.LENGTH_LONG).show();
                return "주소 미발견";

            } else {
                Address address = addresses.get(0);
                return address.getAddressLine(0).toString();
            }
        }


        //현 위치로 이동
        public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            Log.d( TAG, "setCurrentLocation :  ===mGoogleMap moveCamera=== "
                    + location.getLatitude() + "/" + location.getLongitude() ) ;
             CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng, 20);
            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
            mGoogleMap.moveCamera(cameraUpdate);

        }

        //지오펜스 설정(펜스 영역 지도상에 이미지로 표현하고 펜스셋팅은 따로 구현됨)
        public void setGeoPencingLocation() {

            for(int i=0 ; i<fencesList.size(); i++){

                Location geoPenceLocation = new Location("");
                Map<String, Object> fenceMap = fencesList.get(i);
                Marker geoMarker = (Marker)fenceMap.get("marker");

                if (geoMarker != null) {
                    geoMarker.remove();
                }

                geoPenceLocation.setLatitude(Double.parseDouble(
                        fenceMap.get("latitude").toString()));
                geoPenceLocation.setLongitude(Double.parseDouble(
                        fenceMap.get("longitude").toString()));

                String geoMarkerTitle = getCurrentAddress(geoPenceLocation);
                String geoMarkerSnippet = "위도:" + String.valueOf(geoPenceLocation.getLatitude())
                        + " 경도:" + String.valueOf(geoPenceLocation.getLongitude());

                LatLng geoCurrentLatLng = new LatLng(geoPenceLocation.getLatitude(), geoPenceLocation.getLongitude());

                //geoPence 위치 지정(가상의 라인 표시)
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(geoCurrentLatLng);
                markerOptions.title(geoMarkerTitle);
                markerOptions.snippet(geoMarkerSnippet);
                markerOptions.draggable(true);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeBitmap("ic_launcher", 30, 30)));
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmap(
                //geoPenceLocation.getLatitude(),
                //geoPenceLocation.getLongitude())));
                geoMarker = mGoogleMap.addMarker(markerOptions);

                Circle circle = mGoogleMap.addCircle(new CircleOptions()
                        .center(geoCurrentLatLng)
                        .radius(20)
                        .strokeColor(Color.RED)
                        .fillColor(Color.TRANSPARENT));


                //마커 저장
                fenceMap.put("marker",geoMarker);


            }

            //지오펜스셋팅
            geofencesExecute = new GeoMainActivity();

        }

        /*
        public Bitmap resizeBitmap(String drawableName,int width, int height){
            Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(drawableName, "drawable", getPackageName()));
            return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        }*/

        /*
        private Bitmap getBitmap(double lat, double lng) {
            //fill color
            Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG); paint1.setColor(0x110000FF);
            paint1.setStyle(Paint.Style.FILL);
            // stroke color
            Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG); paint2.setColor(0xFF0000FF);
            paint2.setStyle(Paint.Style.STROKE);
            // icon
            Bitmap icon = BitmapFactory.decodeResource(AdminMapActivity.this.getResources(), R.drawable.ic_launcher);
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

        private int convertMetersToPixels(double lat, double lng, double radiusInMeters) {
            double lat1 = radiusInMeters / EARTH_RADIUS;
            double lng1 = radiusInMeters / (EARTH_RADIUS * Math.cos((Math.PI * lat / 180)));
            double lat2 = lat + lat1 * 180 / Math.PI; double lng2 = lng + lng1 * 180 / Math.PI;
            Point p1 = mGoogleMap.getProjection().toScreenLocation(new LatLng(lat, lng));
            Point p2 = mGoogleMap.getProjection().toScreenLocation(new LatLng(lat2, lng2));
            return Math.abs(p1.x - p2.x);
        }
        */

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
            mGeofencingClient = LocationServices.getGeofencingClient(AdminMapActivity.this);

            //권한 확인후
            if (ApplicationMaps.getApps().isFineLocationPermit()
                    && ApplicationMaps.getApps().isCoarseLocationPermit()) {
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
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener(this);
        }


        /**
         * Removes geofences. This method should be called after the user has granted the location
         * permission.
         */
        @SuppressWarnings("MissingPermission")
        private void removeGeofences() {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
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
                String errorMessage = GeofenceErrorMessages.getErrorString(AdminMapActivity.this, task.getException());
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
            Intent intent = new Intent(AdminMapActivity.this, GeofenceTransitionsIntentService.class);
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
            // addGeofences() and removeGeofences().
            mGeofencePendingIntent = PendingIntent.getService(AdminMapActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

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

        //위치정보 업데이트종료
        if (mRequestingLocationUpdates) {

            Log.d(TAG, "===onDestroy : stopLocationUpdate===");
            stopLocationUpdates();
        }

        //연결 종료
        if ( mGoogleApiClient.isConnected()) {

            Log.d(TAG, "===onDestroy=== : disconnected");
            mGoogleApiClient.disconnect();
        }

        //removeGeofences
        if(geofencesExecute != null) {
            geofencesExecute.requestRemoveGeoFences();
        }


    }


}
