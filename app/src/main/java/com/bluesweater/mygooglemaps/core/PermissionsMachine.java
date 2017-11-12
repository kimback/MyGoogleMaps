package com.bluesweater.mygooglemaps.core;

/**
 * Created by kimback on 2017. 11. 10..
 */

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;


import static android.content.Context.LOCATION_SERVICE;

/**
 * 각종 퍼미션 및 활성화 관련 인터페이스
 */
public class PermissionsMachine {
    private Application apps;


    public PermissionsMachine(Application apps){
        this.apps = apps;
    }

    /**
     * 활성화 체크 =====================================
     */

    public boolean internetConnectEnableCheck(){
        ConnectivityManager manager =
                (ConnectivityManager) apps.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if ((mobile != null && mobile.isConnected())
                || (wifi != null && wifi.isConnected())){
            return true;

        }else{

            return false;
        }
    }

    public boolean GPSAndNetworkEnableCheck(){
        LocationManager locationManager = (LocationManager) apps.getSystemService(LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){

            return true;

        }else{

            return false;
        }

    }
    public void BluetoothEnableCheck(){

    }

    /**
     * 권한체크 =====================================
     */

    public boolean locationPermissionCheck(){

        int permFineLocation = ActivityCompat.checkSelfPermission(apps, Manifest.permission.ACCESS_FINE_LOCATION);
        int permCoarseLocation = ActivityCompat.checkSelfPermission(apps, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(permFineLocation == PackageManager.PERMISSION_GRANTED
                && permCoarseLocation == PackageManager.PERMISSION_GRANTED) {

            return true;
        }else{

            return false;
        }

    }
    public void bluetoothPermissionCheck(){

    }
    public void cameraPermissionCheck(){

    }


    /**
     * 권한 요청
     */

    public void requestLocationPermissions(final Activity act){

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle("위치정보 권한 요청");
            builder.setMessage("위치정보 사용을 허가합니다.");
            builder.setCancelable(false);
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    ActivityCompat.requestPermissions(
                            act,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            ApplicationMaps.REQUEST_PERMISSION_FINE_LOCATION);

                    ActivityCompat.requestPermissions(
                            act,
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            ApplicationMaps.REQUEST_PERMISSION_COARSE_LOCATION);

                }
            });

            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //finish();
                    return;
                }
            });
        }


}
