package com.bluesweater.mygooglemaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.ForegroundService;
import com.bluesweater.mygooglemaps.core.GeofencingExecutor;
import com.bluesweater.mygooglemaps.core.MapsPreference;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 위치정보 표시와 geofencing 기술을 같이 사용
 * 화면이 백그라운드 상태일때는 위치 데이터 수신을 정지한다.
 * 지오펜스 정보는 계속 수신중 (서비스에서)
 * 화면에 다시 들어오면 현재 위치를 수신하여 카메라가 이동한다.
 *
 *
 *
 * AdminMapActivity
 */
public class AdminMapActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private MapsPreference mapsPreference;
    private GoogleMapPakage googleMapPakage;

    //구글 맵 api관련
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    //위치정보 interval set
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    //flag
    private boolean mRequestingLocationUpdates = false;
    //resume, pause 에서 중지하고 시작하니 firstMapStarted 를 통해 mapReady후에 타도록 한다
    private boolean firstMapStarted = false;

    //ui
    private RelativeLayout loadingLayout;
    private RelativeLayout adminMapLayout;

    private Handler workerHandler;

    //fenceList
    List<Map<String, Object>> fenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_map);

        loadingLayout = (RelativeLayout) findViewById(R.id.loading_bar_layout);
        adminMapLayout = (RelativeLayout) findViewById(R.id.admin_map_layout);

        mActivity = this;
        mapsPreference = ApplicationMaps.getMapsPreference();
        workerHandler = new Handler();
        fenceList = new ArrayList<>();

        showHideLoadingBar("show");

        //데이터 준비 후 - > 콜백에서 위치정보 초기화 시작
        requestFenceBlockData();

        //initGeoService();


        Intent foregroundStartIntent = new Intent(AdminMapActivity.this, ForegroundService.class);
        foregroundStartIntent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
        startService(foregroundStartIntent);



    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("TAGAdminMapActivity","=======onResume==========");

        if(firstMapStarted){
            // 연결되어있지 않다면
            if(mGoogleApiClient != null && !mGoogleApiClient.isConnected()){

                //연결해라.
                mGoogleApiClient.connect();
                Log.i("TAGAdminMapActivity","=======connect==========");
            }

            //연결되어 있다면
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){

                //위치정보가 업데이트 되고 있지 않다면 시작해라
                if (!mRequestingLocationUpdates) {
                    Log.i("TAGAdminMapActivity", "=======mRequestingLocationUpdates==========");
                    try {
                        //위치 정보 스타트
                        startLocationMachine();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }




            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TAGAdminMapActivity","=======onPause==========");

        // 백그라운드 진입시 지오펜싱이 동작안하는 현상이 있음 - 뭐때문인지 확인중

        if(firstMapStarted) {
            //위치정보 연결 확인
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()
                    && mRequestingLocationUpdates) { //업데이트 되고 있다면
                Log.i("TAGAdminMapActivity", "=======stopLocationUpdates==========");

                //중지하라(백그라운드에서 돌 필요 없음)

                //백그라운드에서 안도는것 때문에 테스트로 주석처리
                //stopLocationUpdates();
            }


            //그리고 연결을 끊어라
            /*if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Log.i("TAGAdminMapActivity", "=======disconnect==========");
                mGoogleApiClient.disconnect();
            }*/
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("위치서비스가 종료됩니다. \n종료하시겠습니까?")
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {

                                finish();
                            }
                        })
                .setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        }).show();


    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //mTextView.setText(savedInstanceState.getString(TEXT_VIEW_KEY));
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putString(GAME_STATE_KEY, mGameState);
        //outState.putString(TEXT_VIEW_KEY, mTextView.getText());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    /**
     * 위치정보 사용 관련 초기화
     */
    private void initGeoService(){

        //mapsPreference = ApplicationMaps.getMapsPreference();
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
        //맵 스타트
        mapFragment.getMapAsync(googleMapPakage);
    }


    //위치정보 서비스 시작
    private void startLocationMachine() throws SecurityException{

        //M 이상에서 권한 관련 적용됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ApplicationMaps.getPermissionsMachine().locationPermissionCheck()) {

                //아래의 셋팅후 부터 onChangeUpdate를 타게 된다
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, googleMapPakage);
                mRequestingLocationUpdates = true;
                mGoogleMap.setMyLocationEnabled(true);

            } else {
                Log.d(TAG, "===startLocationMachine=== : 위치정보 권한 없음 ");
                Toast.makeText(this, "위치정보 권한이 없습니다", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            //아래의 셋팅후 부터 onChangeUpdate를 타게 된다
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, googleMapPakage);
            mRequestingLocationUpdates = true;
            mGoogleMap.setMyLocationEnabled(true);
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
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(10));

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

                        //사용자가 화면이동시 onChangedUpdate 콜백에서오는 위치이동은 막는다
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

            Log.d(TAG, "====map ready connect start=====");
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }

            firstMapStarted = true;

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

            //지오펜스셋팅
            if(ApplicationMaps.getApps().geofencesExecute == null) {
                ApplicationMaps.getApps().geofencesExecute = new GeofencingExecutor(AdminMapActivity.this, fenceList);
            }

            List<Map<String, Object>> fencesList = ApplicationMaps.getApps().geofencesExecute.fencesList;

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
                        .radius(50)
                        .strokeColor(Color.RED)
                        .fillColor(Color.TRANSPARENT));


                //마커 저장
                fenceMap.put("marker",geoMarker);


            }

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


    //========= 데이터 로드 http connect =========================================================
    /**
     * 선택한 스키장별 펜스 블록 데이터를 가져온다.
     *
     * requestFenceBlockData
     *
     */
    private void requestFenceBlockData(){

        showHideLoadingBar("show");

        //실제 로직
        preGeoDataProcess();
    }



    /**
     * http 요청 메서드
     *
     */
    private void preGeoDataProcess() {
        String urlStr = "";
        String authJsonStr = "";

        try {

            urlStr = ApplicationMaps.restApiUrl + "/geoDataList?selectedResort=" + mapsPreference.getSelectedSkiResortCode();
            getHttpData(urlStr); //서버통신

            //Log.i("LOGINTAG","doInBackground : " + authStr);
        }catch (Exception e){
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(adminMapLayout, "데이터 로드중 문제 발생", Snackbar.LENGTH_LONG);
            snackbar.show();
            showHideLoadingBar("hide");
        }

    }


    /**
     * 요청 후처리 (response 후 로직)
     * postGeoDataProcess
     * @param jsonStr
     */
    private void postGeoDataProcess(String jsonStr) {

        //기본적인 값 필터링
        String resultJsonStr = "";
        resultJsonStr = jsonStr.replaceAll("\n","");
        resultJsonStr = resultJsonStr.replaceAll("\r","");
        resultJsonStr = resultJsonStr.trim();
        if(resultJsonStr.equals("[]")){
            resultJsonStr = "";
        }

        //에러가 아니라면 처리
        if(resultJsonStr != null && !resultJsonStr.equals("err500")){
            if(resultJsonStr != "") {
                final String resultParam = resultJsonStr;

                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callbackDataProcess(resultParam);
                    }
                });


            }else{
                Snackbar snackbar = Snackbar.make(adminMapLayout, "데이터 로드에 실패하였습니다.", Snackbar.LENGTH_LONG);
                snackbar.show();

                showHideLoadingBar("hide");
                return;
            }
        }else if(resultJsonStr != null && resultJsonStr.equals("err500")) { //에러가 발생했다면

            Snackbar snackbar = Snackbar.make(adminMapLayout, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");
            return;
        }else{
            Snackbar snackbar = Snackbar.make(adminMapLayout, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");
            return;

        }

    }



    /**
     * callbackJoinProcess 인증후 콜백 메서드
     * @param resultStr
     */
    private void callbackDataProcess(String resultStr){

        String dataSuccess = "F";
        showHideLoadingBar("hide");

        try {
            if(resultStr != null && !resultStr.equals("")){

                JSONArray jsonArray = new JSONArray(resultStr);
                //정보[0]
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = jsonArray.getJSONObject(i);

                    //여기에 돌리면서 펜스 정보 로직 추가
                    Map<String, Object> blockMap = new HashMap<>();
                    blockMap.put("skiResortCode", jo.getString("skiResortCode"));
                    blockMap.put("blockCode", jo.getString("blockCode"));
                    blockMap.put("name", jo.getString("name"));
                    blockMap.put("meter", jo.getString("meter"));
                    blockMap.put("latitude", jo.getString("latitude"));
                    blockMap.put("longitude", jo.getString("longitude"));

                    fenceList.add(blockMap);
                    dataSuccess = "T";
                }

            }

            //데이터 불러온 뒤 위치 정보 서비스 초기화 시작
            initGeoService();

        }catch(Exception e){
            e.printStackTrace();

            Snackbar snackbar = Snackbar.make(adminMapLayout, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");
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

                    Snackbar snackbar = Snackbar.make(adminMapLayout, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    showHideLoadingBar("hide");

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //받은 정보들로 후처리 구현
                    postGeoDataProcess(response.body().string());



                }

            });

        } catch (Exception e) {
            e.printStackTrace();

            Snackbar snackbar = Snackbar.make(adminMapLayout, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

        }finally {
            //final logic

            showHideLoadingBar("hide");
        }

    }




    //==============================================================================================


    private void showHideLoadingBar(final String type){
        workerHandler.post(new Runnable() {
            @Override
            public void run() {

                if(loadingLayout != null) {
                    if(type.equals("show")) {
                        loadingLayout.setVisibility(View.VISIBLE);
                    }else{
                        loadingLayout.setVisibility(View.GONE);
                    }
                }

            }
        });


    }






    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i("TAGAdminMapActivity","=======on Destroy==========");

        //화면과 지오펜싱을 동기화시킴
        if(ApplicationMaps.getApps().geofencesExecute != null){
            ApplicationMaps.getApps().geofencesExecute.requestRemoveGeoFences();
            ApplicationMaps.getApps().geofencesExecute = null;
            Log.i("TAGAdminMapActivity","=======requestRemoveGeoFences==========");
        }

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()
                && mRequestingLocationUpdates){
            stopLocationUpdates();
            Log.i("TAGAdminMapActivity","=======stopLocationUpdates==========");
        }

        if (mGoogleApiClient != null &&  mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.i("TAGAdminMapActivity","=======mGoogleApiClient.disconnect==========");
        }

        Intent foregroundStopIntent = new Intent(AdminMapActivity.this, ForegroundService.class);
        foregroundStopIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        startService(foregroundStopIntent);

    }
}
