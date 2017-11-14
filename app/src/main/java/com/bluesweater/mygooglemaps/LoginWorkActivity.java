package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

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


    }


    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        ApplicationMaps.getApps().closeApplication();
    }



}
