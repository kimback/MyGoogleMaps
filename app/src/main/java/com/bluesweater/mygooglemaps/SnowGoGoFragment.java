package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class SnowGoGoFragment extends Fragment {

    private Button btnGeo1;
    private Button btnGeo2;
    private Context context;


    public SnowGoGoFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_snow_go_go, container, false);

        btnGeo1 = (Button) view.findViewById(R.id.btn_geotest1);
        btnGeo2 = (Button) view.findViewById(R.id.btn_geotest2);

        btnGeo1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "일반사용자", Toast.LENGTH_LONG);
            }
        });

        btnGeo2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, AdminMapActivity.class);
                startActivity(i);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
