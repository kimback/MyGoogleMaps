package com.bluesweater.mygooglemaps.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kimback on 2017. 11. 10..
 */

public class MapsPreference {

    private Context ctx;
    private int versionInt;
    private boolean saveLogin;
    private String loginId;
    private String selectedSkiResort;


    public MapsPreference(Context ctx) {
        this.ctx = ctx;
        SharedPreferences pref = ctx.getSharedPreferences(
                "SSA_pref", Context.MODE_PRIVATE);

        versionInt = pref.getInt("versionInt", 1000);
        saveLogin = pref.getBoolean("saveLogin", true);
        loginId = pref.getString("loginId", "");
        selectedSkiResort = pref.getString("selectedSkiResort", "");


    }


    public int getVersionInt() {
        return versionInt;
    }

    public void setVersionInt(int versionInt) {
        this.versionInt = versionInt;
    }

    public boolean isSaveLogin() {
        return saveLogin;
    }

    public void setSaveLogin(boolean saveLogin) {
        this.saveLogin = saveLogin;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getSelectedSkiResort() {
        return selectedSkiResort;
    }

    public void setSelectedSkiResort(String selectedSkiResort) {
        this.selectedSkiResort = selectedSkiResort;
    }

    public void appPrefSave() {
        SharedPreferences.Editor edit = ctx.getSharedPreferences(
                "SSA_pref", Context.MODE_PRIVATE).edit();

        edit.putInt("versionInt", versionInt);
        edit.putBoolean("saveLogin", saveLogin);
        edit.putString("loginId", loginId);
        edit.putString("selectedSkiResort", selectedSkiResort);


        edit.commit();
    }



}
