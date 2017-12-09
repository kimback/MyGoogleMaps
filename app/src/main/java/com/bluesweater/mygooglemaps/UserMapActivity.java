package com.bluesweater.mygooglemaps;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.GeofencingExecutor;
import com.bluesweater.mygooglemaps.core.MapsPreference;


public class UserMapActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Activity mActivity;
    private MapsPreference mapsPreference;

    //구글 맵 api관련
    public GeofencingExecutor geofencesExecute;

    //ui
    private RelativeLayout loadingLayout;
    private RelativeLayout userViewsLayout;
    private LinearLayout btnLayout;
    private LinearLayout statusLayout;
    private Button btnStartGeo;
    private Button btnStopGeo;


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

        //if(loadingLayout != null){
            //loadingLayout.setVisibility(View.VISIBLE);
        //}
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
                    geofencesExecute = new GeofencingExecutor(UserMapActivity.this, null);
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

        mapsPreference = ApplicationMaps.getMapsPreference();

        //지오펜싱 초기화
        geofencesExecute = new GeofencingExecutor(this, null);
        //ApplicationMaps.getApps().setGeofencingExecutor(geofencesExecute);

    }

    public void hideLoadingBar(){
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(statusLayout != null) {
                    statusLayout.setVisibility(View.GONE);
                }
            }
        });

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
