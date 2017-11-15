package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

/**
 *
 * class WelcomeActivity
 */
public class WelcomeActivity extends AppCompatActivity {
    Handler handler;
    MapsPreference mapsPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mapsPreference = ApplicationMaps.getMapsPreference();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                //로그인 사용자 정보 저장여부 확인
                if(!mapsPreference.isSaveLogin()){
                    startActivity(new Intent(WelcomeActivity.this, LoginWorkActivity.class));
                    finish();

                }else{
                    //저장모드라면 저장된게 있는지 확인
                    if(!mapsPreference.getLoginId().equals("")
                            && !mapsPreference.getSelectedSkiResort().equals("")){

                        //메인으로
                        startActivity(new Intent(WelcomeActivity.this, DashboardActivity.class));
                        finish();
                    }else{
                        //그외 로그인 해야함
                        startActivity(new Intent(WelcomeActivity.this, LoginWorkActivity.class));
                        finish();
                    }

                }


            }
        };

        handler.sendEmptyMessageDelayed(0,2000);


    }

    @Override
    public void onBackPressed() {
        //ignore
    }
}
