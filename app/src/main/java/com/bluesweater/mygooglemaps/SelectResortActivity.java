package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;
import com.bluesweater.mygooglemaps.core.SkiResort;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SelectResortActivity extends AppCompatActivity {

    private Spinner selectResortSpinner;
    private ArrayAdapter<SkiResort> spinnerAdapter;
    private ArrayList<SkiResort> spinerList;
    private Button btnOk;
    private MapsPreference pref;
    private Handler workerHandler;
    private RelativeLayout progressLayout;
    private RelativeLayout alertLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_resort);

        selectResortSpinner = (Spinner) findViewById(R.id.spiner_select_resort);
        btnOk = (Button)findViewById(R.id.btn_ok);
        progressLayout = (RelativeLayout) findViewById(R.id.prog_layout);
        alertLayout = (RelativeLayout) findViewById(R.id.selectSkiLayout);

        pref = ApplicationMaps.getMapsPreference();
        workerHandler = new Handler();
        spinerList = new ArrayList<>();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinerList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectResortSpinner.setAdapter(spinnerAdapter);
        selectResortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {

                //선택한 아이템을 저장
                SkiResort skiResort1 = (SkiResort) adapter.getSelectedItem();
                pref.setSelectedSkiResortCode(skiResort1.getCode());
                pref.setSelectedSkiResortName(skiResort1.getName());
                pref.appPrefSave();
                //Log.i("SELECTTAG","====" + skiResort1.getName() + "====");

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //Toast.makeText(LocationActivity.this, "onNothingSelected", Toast.LENGTH_SHORT);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(!pref.getLoginId().equals("") && !pref.getSelectedSkiResortCode().equals("")) {
                    startActivity(new Intent(SelectResortActivity.this, DashboardActivity.class));
                    finish();
                }else{
                    // 아이디나 스키장 정보가 없음
                }

            }
        });


        //스키장 정보 가져옴
        initSkiResortDataLoad();

    }


    /**
     *
     * 스키장정보를 서버와 http 통신을 하여 가져온다.
     *
     * initSkiResortDataLoad
     */
    private void initSkiResortDataLoad(){
        if(progressLayout != null){
            progressLayout.setVisibility(View.VISIBLE);
        }

        if(!pref.getLoginId().equals("")) {
           new SearchApiModule().doInBackground(pref.getLoginId());
        }

    }


    /**
     * api 를 통하여 데이터 연동
     */
    // 조회연동
    private class SearchApiModule {
        String urlStr = "";

        /**
         * doInBackground
         * @param params
         */
        public void doInBackground(String... params) {
            String loginId = params[0];

            try {
                urlStr = ApplicationMaps.restApiUrl + "/searchSkiResortData?loginId=" + loginId;
                getHttpData(urlStr, this); //서버와통신작업

            } catch (Exception e) {
                e.printStackTrace();

                Snackbar snackbar = Snackbar.make(alertLayout, "서버통신중 장애가 발생하였습니다.", Snackbar.LENGTH_LONG);
                snackbar.show();

            }

        }



        /**
         * onPostExecute
         * @param jsonStr
         */
        public void onPostExecute(final String jsonStr) {

            String resultJsonStr = "";
            //빈값 체크
            resultJsonStr = jsonStr.replaceAll("\n","");
            resultJsonStr = resultJsonStr.replaceAll("\r","");
            resultJsonStr = resultJsonStr.trim();
            if(resultJsonStr.equals("[]") || resultJsonStr.equals("null")){
                resultJsonStr = "";
            }

            //서버에러가 없다면
            if(resultJsonStr != null && !resultJsonStr.equals("err500")){

                if(resultJsonStr != "") {
                    //Log.i("LOGINTAG", "=======onPostExecute========= : \n" + resultJsonStr);
                    final String resultParam = resultJsonStr;

                    workerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbackApiRequest(resultParam);
                        }
                    });

                }else{

                    Snackbar snackbar = Snackbar.make(alertLayout, "데이터를 가져오지 못하였습니다.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    //Toast.makeText(LocationActivity.this, "데이터를 가져오지 못하였습니다.", Toast.LENGTH_SHORT).show();

                    workerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(progressLayout != null){
                                progressLayout.setVisibility(View.GONE);
                            }
                        }
                    });


                    return;
                }
            }else if(resultJsonStr != null && resultJsonStr.equals("err500")) {

                Snackbar snackbar = Snackbar.make(alertLayout, "서버와의 장애가 발생하였습니다.", Snackbar.LENGTH_LONG);
                snackbar.show();

                //Toast.makeText(LocationActivity.this, "서버와의 장애가 발생하였습니다.", Toast.LENGTH_SHORT).show();

                workerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(progressLayout != null){
                            progressLayout.setVisibility(View.GONE);
                        }
                    }
                });
                return;

            }

        }


    } //SearchApiModule inner class end


    /**
     *
     * 서버와 통신하여 스키장 리스트를 조회합니다.
     * 새로운 쓰레드가 생성되며, 커넥션 합니다.
     *
     * getHttpData
     *
     * @param restUrl
     * @param searchApiModule
     * @return
     * @throws Exception
     */
    public void getHttpData(String restUrl, SearchApiModule searchApiModule) throws Exception {

        final SearchApiModule searchApiModule1 = searchApiModule;

        OkHttpClient client = new OkHttpClient();

        //request
        Request request = new Request.Builder()
                .url(restUrl)
                .get()
                .build();

        try {
            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e){
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    //후처리로직
                    searchApiModule1.onPostExecute(response.body().string());

                }

            });

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());

        }finally {
            //
        }

    }


    /** 서버 통신후 콜백
     * callbackApiRequest
     * @param result
     */
    private void callbackApiRequest(String result){

        if(progressLayout != null){
            progressLayout.setVisibility(View.GONE);
        }

        //Log.i("APITAG","result : \n" + result );

        //리스트 클리어
        if(spinerList != null){
            spinerList.clear();
        }

        try {

            if(result != null && !result.equals("")) {
                JSONArray jsonArray = new JSONArray(result);

                //스키장 목록을 리스트에 추가해줍니다 (spinner)
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);

                    SkiResort dataMap = new SkiResort();
                    dataMap.setCode(jo.get("skiResortCode").toString());
                    dataMap.setName(jo.get("name").toString());

                    spinerList.add(dataMap);

                }

            }

            //스피너 리플레쉬
            spinnerAdapter.notifyDataSetChanged();

        }catch(Exception e){
            e.printStackTrace();
        }

    } //callbackApiRequest end


}
