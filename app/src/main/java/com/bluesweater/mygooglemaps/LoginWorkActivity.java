package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

public class LoginWorkActivity extends AppCompatActivity {

    private EditText loginId;
    private EditText loginPw;
    private Button loginBtn;
    private CheckBox chkSaveLogin;
    private MapsPreference mapsPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_work);

        loginId = (EditText) findViewById(R.id.login_id);
        loginPw = (EditText) findViewById(R.id.login_pw);
        loginBtn = (Button) findViewById(R.id.btn_login);
        chkSaveLogin = (CheckBox) findViewById(R.id.chk_save_login);


        mapsPreference = ApplicationMaps.getMapsPreference();

        if(mapsPreference.isSaveLogin()){
            chkSaveLogin.setChecked(true);
        }else{
            chkSaveLogin.setChecked(false);
        }

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                startActivity(new Intent(LoginWorkActivity.this, SelectResortActivity.class));
                finish();

            }
        });
        chkSaveLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                Log.i("LOGINTAG", "===========" + String.valueOf(b) + "===========");
                mapsPreference.setSaveLogin(b);
                mapsPreference.appPrefSave();


            }
        });


        //permission
        permissionCheckAll();


    }


    @Override
    public void onBackPressed() {
        finish();
    }



    //권한체크
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
