package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.bluesweater.mygooglemaps.core.ApplicationMaps;
import com.bluesweater.mygooglemaps.core.MapsPreference;
import com.bluesweater.mygooglemaps.core.SkiResort;

import java.util.ArrayList;

public class SelectResortActivity extends AppCompatActivity {

    private Spinner selectResortSpinner;
    private ArrayAdapter<SkiResort> spinnerAdapter;
    private ArrayList<SkiResort> spinerList;
    private Button btnOk;
    private MapsPreference pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_resort);

        selectResortSpinner = (Spinner) findViewById(R.id.spiner_select_resort);
        btnOk = (Button)findViewById(R.id.btn_ok);

        pref = ApplicationMaps.getMapsPreference();

        final SkiResort skiResort = new SkiResort();
        skiResort.setCode("AAAA");
        skiResort.setName("하이원리조트");

        spinerList = new ArrayList<>();
        spinerList.add(0, skiResort);

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinerList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectResortSpinner.setAdapter(spinnerAdapter);
        selectResortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View v, int position, long id) {

                SkiResort skiResort1 = (SkiResort) adapter.getSelectedItem();
                pref.setSelectedSkiResort(skiResort.getName());
                pref.appPrefSave();
                Log.i("SELECTTAG","====" + skiResort1.getName() + "====");

            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                //Toast.makeText(LocationActivity.this, "onNothingSelected", Toast.LENGTH_SHORT);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SelectResortActivity.this, MainActivity.class));
                finish();

            }
        });


    }
}
