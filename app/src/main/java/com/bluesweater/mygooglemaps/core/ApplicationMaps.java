package com.bluesweater.mygooglemaps.core;

import android.app.Application;

import com.bluesweater.mygooglemaps.DashboardActivity;

/**
 * Created by kimback on 2017. 11. 10..
 */

public class ApplicationMaps extends Application{
    private static ApplicationMaps apps;
    private DashboardActivity dashboardActivity;

    //============================================
    // 매니저 변수들
    //============================================
    private static PermissionsMachine permissionsMachine;
    private static MapsPreference mapsPreference;

    //============================================
    // request code
    //============================================
    public static final int REQUEST_PERMISSION_FINE_LOCATION = 1000;
    public static final int REQUEST_PERMISSION_COARSE_LOCATION = 2000;

    //============================================
    // result code
    //============================================

    //============================================
    // frag var
    //============================================
    private boolean fineLocationPermit = false;
    private boolean coarseLocationPermit = false;

    public static final String mainUrl = "http://192.168.0.3:8080/webapp/hello";
    public static final String restApiUrl = "http://192.168.0.3:8080/webapp/hello";

    @Override
    public void onCreate() {
        super.onCreate();
        apps = this;

        if(permissionsMachine == null){
            permissionsMachine = new PermissionsMachine(apps);
        }

        if(mapsPreference == null){
            mapsPreference = new MapsPreference(apps);
        }

    }

    public static PermissionsMachine getPermissionsMachine(){
        if(permissionsMachine == null){
            permissionsMachine = new PermissionsMachine(apps);
        }
        return permissionsMachine;
    }

    public static MapsPreference getMapsPreference(){
        if(mapsPreference == null){
            mapsPreference = new MapsPreference(apps);
        }
        return mapsPreference;
    }

    public static ApplicationMaps getApps(){
        return apps;
    }

    public boolean isFineLocationPermit() {
        return fineLocationPermit;
    }

    public void setFineLocationPermit(boolean fineLocationPermit) {
        this.fineLocationPermit = fineLocationPermit;
    }

    public boolean isCoarseLocationPermit() {
        return coarseLocationPermit;
    }

    public void setCoarseLocationPermit(boolean coarseLocationPermit) {
        this.coarseLocationPermit = coarseLocationPermit;
    }

    public DashboardActivity getDashboardActivity() {
        return dashboardActivity;
    }

    public void setDashboardActivity(DashboardActivity dashboardActivity) {
        this.dashboardActivity = dashboardActivity;
    }

    public void closeApplication(){
        //app close logic
        if(permissionsMachine != null){
            permissionsMachine = null;
        }
        if(mapsPreference != null){
            mapsPreference = null;
        }

    }

}
