package com.bluesweater.mygooglemaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button btnGeo1;
    private Button btnGeo2;
    private Button btnGeo3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        btnGeo1 = (Button) findViewById(R.id.btn_geotest1);
        btnGeo2 = (Button) findViewById(R.id.btn_geotest2);
        btnGeo3 = (Button) findViewById(R.id.btn_geotest3);


        //구버전에서
        btnGeo1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(i);
            }
        });

        //신버전에서
        btnGeo2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent ii = new Intent(MainActivity.this, MapsActivity2.class);
                startActivity(ii);
            }
        });

        //geofences
        btnGeo3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, GeoMainActivity_Ex.class);
                startActivity(i);
            }
        });

    }

}
