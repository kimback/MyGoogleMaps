package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

/**
 * 웰컴페이지 - 브랜드명 표시후
 * 1. 로그인화면 진입
 * 2. 메인페이지 진입
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

                if(msg.what == 0){
                    //로그인정보를 저장하지 않는다면 무조건 로그인 페이지 이동
                    if(!mapsPreference.isSaveLogin()){
                        startActivity(new Intent(WelcomeActivity.this, LoginWorkActivity.class));
                        finish();

                    }else{ //저장모드라면 저장된게 있는지 확인

                        //로그인 정보와 스키장 선택 정보는 같이 있고 같이 없다고 본다
                        if(!mapsPreference.getLoginId().equals("")
                                && !mapsPreference.getSelectedSkiResortCode().equals("")){

                            //메인으로
                            startActivity(new Intent(WelcomeActivity.this, DashboardActivity.class));
                            finish();
                        }else{
                            //그외 경우 전부 로그인 해야함
                            startActivity(new Intent(WelcomeActivity.this, LoginWorkActivity.class));
                            finish();
                        }

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
