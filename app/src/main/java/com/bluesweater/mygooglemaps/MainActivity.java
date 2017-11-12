package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

public class MainActivity extends AppCompatActivity {

    private Button btnGeo1;
    private Button btnGeo2;
    private MapsPreference mapsPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnGeo1 = (Button) findViewById(R.id.btn_geotest1);
        btnGeo2 = (Button) findViewById(R.id.btn_geotest2);

        btnGeo1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "일반사용자", Toast.LENGTH_LONG);
            }
        });

        btnGeo2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AdminMapActivity.class);
                startActivity(i);
            }
        });

        //permission
        permissionCheckAll();


    }

    private void permissionCheckAll(){
        //각종 권한 얻기
        //인터넷 활성화 확인
        if (!ApplicationMaps.getPermissionsMachine().internetConnectEnableCheck()) {
            Toast.makeText(this, "인터넷연결 상태가 아닙니다. 인터넷 연결 이후 재 시도 해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //gps , network 활성화 확인
        if (!ApplicationMaps.getPermissionsMachine().GPSAndNetworkEnableCheck()) {
            Toast.makeText(this, "GPS, NETWORK 를 활성화 해주세요", Toast.LENGTH_SHORT).show();
            return;
        }

        //location 권한 확인
        if(!ApplicationMaps.getPermissionsMachine().locationPermissionCheck()){
            Toast.makeText(this, "LOCATION 권한을 체크하세요.", Toast.LENGTH_SHORT).show();
            ApplicationMaps.getPermissionsMachine().requestLocationPermissions(this);
        }else{
            ApplicationMaps.getApps().setFineLocationPermit(true);
            ApplicationMaps.getApps().setCoarseLocationPermit(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //CALLBACK
        switch (requestCode){
            case ApplicationMaps.REQUEST_PERMISSION_FINE_LOCATION:
                ApplicationMaps.getApps().setFineLocationPermit(true);
                break;
            case ApplicationMaps.REQUEST_PERMISSION_COARSE_LOCATION:
                ApplicationMaps.getApps().setCoarseLocationPermit(true);
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //CALLBACK

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApplicationMaps.getApps().closeApplication();

    }
}
