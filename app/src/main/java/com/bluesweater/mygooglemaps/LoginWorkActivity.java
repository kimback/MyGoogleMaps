package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginWorkActivity extends AppCompatActivity {

    private EditText loginId;
    private EditText loginPw;
    private Button loginBtn;
    private CheckBox chkSaveLogin;
    private RelativeLayout progressLayout;
    private RelativeLayout alertCanvas;
    private MapsPreference mapsPreference;
    private Handler workerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_work);

        loginId = (EditText) findViewById(R.id.login_id);
        loginPw = (EditText) findViewById(R.id.login_pw);
        chkSaveLogin = (CheckBox) findViewById(R.id.chk_save_login);
        loginBtn = (Button) findViewById(R.id.btn_login);
        progressLayout = (RelativeLayout) findViewById(R.id.prog_layout);
        alertCanvas =  (RelativeLayout) findViewById(R.id.login_layout);

        mapsPreference = ApplicationMaps.getMapsPreference();
        workerHandler = new Handler();

        //체크 토글
        if(mapsPreference.isSaveLogin()){
            chkSaveLogin.setChecked(true);
        }else{
            chkSaveLogin.setChecked(false);
        }

        loginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                String uId = loginId.getText().toString();
                String uPw = loginPw.getText().toString();

                if(uId.equals("") || uPw.equals("") ){
                    Snackbar snackbar = Snackbar.make(alertCanvas, "로그인정보를 입력 하세요.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;

                }

                //인터넷 연결 확인
                if(!ApplicationMaps.getPermissionsMachine().internetConnectEnableCheck()){

                    Snackbar snackbar = Snackbar.make(alertCanvas, "인터넷 연결상태를 확인하세요.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;

                }

                //로그인 인증 처리
                requestMyLogin(uId, uPw);
                //startActivity(new Intent(LoginWorkActivity.this, SelectResortActivity.class));
                //finish();

            }
        });
        chkSaveLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                //Log.i("LOGINTAG", "===========" + String.valueOf(b) + "===========");
                mapsPreference.setSaveLogin(b);
                mapsPreference.appPrefSave();

            }
        });

        showHideLoadingBar("hide");

    }


    // GPS, network 셋팅
    private void showDialogForGpsNetworkSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, ApplicationMaps.REQUEST_PERMISSION_GPS_ENABLE_REQUEST_CODE);
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


    //권한체크
    private void permissionCheckAll() {

        //각종 권한 얻기
        //인터넷 활성화 확인
        if (!ApplicationMaps.getPermissionsMachine().internetConnectEnableCheck()) {
            Toast.makeText(this, "인터넷연결 상태가 아닙니다. 인터넷 연결 이후 재 시도 해 주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //gps , network 활성화 확인
        if (!ApplicationMaps.getPermissionsMachine().GPSAndNetworkEnableCheck()) {
            Toast.makeText(this, "GPS, NETWORK 를 활성화 해주세요", Toast.LENGTH_SHORT).show();
            showDialogForGpsNetworkSetting();
            return;
        }else{
            ApplicationMaps.getApps().setGpsNetworkPermit(true);
        }

        //location 권한 확인
        //M 이상에서 권한 관련 적용됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!ApplicationMaps.getPermissionsMachine().locationPermissionCheck()) {
                Toast.makeText(this, "LOCATION 권한을 체크하세요.", Toast.LENGTH_SHORT).show();
                ApplicationMaps.getPermissionsMachine().requestLocationPermissions(this);
            } else {
                ApplicationMaps.getApps().setFineLocationPermit(true);
                ApplicationMaps.getApps().setCoarseLocationPermit(true);
            }
        }else{
            //그이전 버전들은 그냥 true
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
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ApplicationMaps.getApps().setFineLocationPermit(true);
                }

                break;
            case ApplicationMaps.REQUEST_PERMISSION_COARSE_LOCATION:
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ApplicationMaps.getApps().setCoarseLocationPermit(true);
                }
                break;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //CALLBACK
        switch (requestCode){
            case ApplicationMaps.REQUEST_PERMISSION_GPS_ENABLE_REQUEST_CODE :
                if (ApplicationMaps.getPermissionsMachine().GPSAndNetworkEnableCheck()) {
                    ApplicationMaps.getApps().setGpsNetworkPermit(true);
                }

                break;

        }


    }


    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    //========= 로그인 인증처리 관련 http connect =========================================================

    /**
     * 로그인 인증 처리 프로세스
     * restAPI를 통해 서버에서 로그인 정보를 인증받기 위한 요청.
     *
     * requestMyLogin
     *
     *
     * @param loginId
     * @param loginPw
     */
    private void requestMyLogin(String loginId, String loginPw){

        showHideLoadingBar("show");

        //실제 로직
        preLoginProcess(loginId, loginPw, "login");
    }


    /**
     * 가입 처리 프로세스
     * restAPI를 통해 서버에서 로그인 정보를 인증받기 위한 요청.
     *
     * requestJoinUser
     *
     *
     * @param loginId
     * @param loginPw
     */
    private void requestJoinUser(String loginId, String loginPw, String type){

       showHideLoadingBar("show");

        //실제 로직
        preLoginProcess(loginId, loginPw, type);
    }



    /**
     * 로그인요청 전처리 메서드
     *
     * preLoginProcess
     * @param params
     */
    private void preLoginProcess(String... params) {
        String urlStr = "";
        String authJsonStr = "";

        try {
            String uid = params[0];
            String upw = params[1];
            String type = params[2];
            if(type.equals("login")) {
                urlStr = ApplicationMaps.restApiUrl + "/myLogin?uid=" + uid + "&upw=" + upw;
            }else if(type.equals("userJoinService")) {
                urlStr = ApplicationMaps.restApiUrl + "/userJoinService?uid=" + uid + "&upw=" + upw;
            }
            getHttpData(urlStr, type); //서버통신


            //Log.i("LOGINTAG","doInBackground : " + authStr);
        }catch (Exception e){

            e.printStackTrace();

            showHideLoadingBar("hide");

            Snackbar snackbar = Snackbar.make(alertCanvas, "로그인중 장애 발생", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

    }


    /**
     * 로그인 요청 후처리 (response 후 로직)
     * postLoginProcess
     * @param jsonStr
     */
    private void postLoginProcess(String jsonStr) {

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
                        callbackLoginProcess(resultParam);
                    }
                });


            }else{
                Snackbar snackbar = Snackbar.make(alertCanvas, "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG);
                snackbar.show();

                showHideLoadingBar("hide");

                return;
            }
        }else if(resultJsonStr != null && resultJsonStr.equals("err500")) { //에러가 발생했다면

            Snackbar snackbar = Snackbar.make(alertCanvas, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");

            return;
        }else{
            Snackbar snackbar = Snackbar.make(alertCanvas, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");

        }

    }


    /**
     * 가입처리 (response 후 로직)
     * postLoginProcess
     * @param jsonStr
     */
    private void postJoinProcess(String jsonStr) {

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
                        callbackJoinProcess(resultParam);
                    }
                });


            }else{
                Snackbar snackbar = Snackbar.make(alertCanvas, "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG);
                snackbar.show();

                showHideLoadingBar("hide");
                return;
            }
        }else if(resultJsonStr != null && resultJsonStr.equals("err500")) { //에러가 발생했다면

            Snackbar snackbar = Snackbar.make(alertCanvas, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");
            return;
        }else{
            Snackbar snackbar = Snackbar.make(alertCanvas, "서버와 통신중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();
            showHideLoadingBar("hide");
            return;

        }

    }



    /**
     * callbackLoginProcess 인증후 콜백 메서드
     * @param resultStr
     */
    private void callbackLoginProcess(String resultStr){
        String loginSuccess = "F";
        final String uId = loginId.getText().toString();
        final String uPw = loginPw.getText().toString();

        if(alertCanvas != null) {
            alertCanvas.setVisibility(View.GONE);
        }

        try {
            if(resultStr != null && !resultStr.equals("")){

                JSONArray jsonArray = new JSONArray(resultStr);
                //로그인 인증정보[0]
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = jsonArray.getJSONObject(i);
                    //로그인 성공 처리
                    if(jo.get("loginAuth").toString().equals("LT")){

                        //로그인 성공! 아이디저장!
                        mapsPreference.setLoginId(uId);
                        mapsPreference.appPrefSave();
                        loginSuccess = "T";

                    //로그인 실패 (아이디나, 패스워드 틀림)
                    }else if(jo.get("loginAuth").toString().equals("LF")){
                        if(alertCanvas != null) {
                            alertCanvas.setVisibility(View.VISIBLE);
                        }
                        workerHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideSoftKeyboard();
                                //패스워드 틀림
                                Snackbar snackbar = Snackbar.make(alertCanvas,
                                        "패스워드가 틀렸습니다. 다시 로그인 해주세요. ", Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        });

                    //로그인 실패 아이디 없음 -> 가입처리
                    }else if(jo.get("loginAuth").toString().equals("F")){
                        mapsPreference.setLoginId("");
                        mapsPreference.appPrefSave();

                        new AlertDialog.Builder(this)
                                .setTitle("미등록 사용자 입니다. 등록 하시겠습니까?")
                                .setPositiveButton("확인",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which) {


                                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(alertCanvas != null) {
                                                            alertCanvas.setVisibility(View.VISIBLE);
                                                        }
                                                        hideSoftKeyboard();

                                                    }
                                                });
                                                requestJoinUser(uId, uPw, "userJoinService");

                                                return;
                                            }
                                        })

                                    .setNegativeButton("취소",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {
                                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if(alertCanvas != null) {
                                                                alertCanvas.setVisibility(View.VISIBLE);
                                                            }
                                                            hideSoftKeyboard();
                                                            return;
                                                        }
                                                    });
                                                }
                                            })

                                    .show();

                    }else{
                        hideSoftKeyboard();
                        if(alertCanvas != null) {
                            alertCanvas.setVisibility(View.VISIBLE);
                        }
                        Snackbar snackbar = Snackbar.make(alertCanvas,
                                "로그인 처리에 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        return;
                        //그외 처리
                    }


                }

            }

        }catch(Exception e){

            e.printStackTrace();
            hideSoftKeyboard();
            Snackbar snackbar = Snackbar.make(alertCanvas, "로그인처리중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
            snackbar.show();

            showHideLoadingBar("hide");


        }

        if(loginSuccess.equals("T")){
            //Log.i("===LOGINTAG=== : ", "로그인 인증처리 완료");
            startActivity(new Intent(LoginWorkActivity.this, SelectResortActivity.class));
            finish();
        }else{
            //Log.i("===LOGINTAG=== : ", "로그인 인증처리 실패");
        }

    }



    /**
     * callbackJoinProcess 인증후 콜백 메서드
     * @param resultStr
     */
    private void callbackJoinProcess(String resultStr){

        String loginSuccess = "F";

        final String uId = loginId.getText().toString();
        final String uPw = loginPw.getText().toString();


        if(progressLayout != null) {
            progressLayout.setVisibility(View.GONE);
        }

        try {
            if(resultStr != null && !resultStr.equals("")){

                JSONArray jsonArray = new JSONArray(resultStr);
                //로그인 인증정보[0]
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jo = jsonArray.getJSONObject(i);
                    if(jo.get("result").toString().equals("1")){

                        //로그인 성공! 아이디저장!
                        mapsPreference.setLoginId(uId);
                        mapsPreference.appPrefSave();
                        loginSuccess = "T";

                    }else{
                        Snackbar snackbar = Snackbar.make(alertCanvas, "가입처리에 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }

            }

            if(loginSuccess.equals("T")){
                //Log.i("===LOGINTAG=== : ", "로그인 인증처리 완료");
                startActivity(new Intent(LoginWorkActivity.this, SelectResortActivity.class));
                finish();
            }else{
                //Log.i("===LOGINTAG=== : ", "로그인 인증처리 실패");
            }

        }catch(Exception e){
            e.printStackTrace();


            Snackbar snackbar = Snackbar.make(alertCanvas, "로그인처리중 문제가 발생하였습니다.", Snackbar.LENGTH_LONG);
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
    public void getHttpData(String page, final String type) {

        OkHttpClient client = new OkHttpClient();

        //request
        Request request = new Request.Builder()
                .url(page)
                .get()
                .build();

        try {
            client.newCall(request).enqueue(new Callback() {

                final String callType = type;

                @Override
                public void onFailure(Call call, IOException e){
                    e.printStackTrace();

                    Snackbar snackbar = Snackbar.make(alertCanvas, "로그인중 장애 발생", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    showHideLoadingBar("hide");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //받은 정보들로 후처리 구현
                    if(callType.equals("login")){
                        postLoginProcess(response.body().string());
                    }else if(callType.equals("userJoinService")){
                        postJoinProcess(response.body().string());
                    }


                }

            });

        } catch (Exception e) {
            e.printStackTrace();

            Snackbar snackbar = Snackbar.make(alertCanvas, "로그인중 장애 발생", Snackbar.LENGTH_LONG);
            snackbar.show();

        }finally {
            //final logic
            showHideLoadingBar("hide");
        }

    }


    //======================================================================================


    private void showHideLoadingBar(final String type){
        workerHandler.post(new Runnable() {
            @Override
            public void run() {

                if(progressLayout != null) {
                    if(type.equals("show")) {
                        progressLayout.setVisibility(View.VISIBLE);
                    }else{
                        progressLayout.setVisibility(View.GONE);
                    }
                }

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
