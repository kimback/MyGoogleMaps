package com.bluesweater.mygooglemaps.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kimback on 2017. 11. 10..
 */

public class MapsPreference {

    private Context ctx;
    private int versionInt;

    public MapsPreference(Context ctx) {
        this.ctx = ctx;
        SharedPreferences pref = ctx.getSharedPreferences(
                "SSA_pref", Context.MODE_PRIVATE);

        versionInt = pref.getInt("versionInt", 1000);


    }


    public int getVersionInt() {
        return versionInt;
    }

    public void setVersionInt(int versionInt) {
        this.versionInt = versionInt;
    }

    public void appPrefSave() {
        SharedPreferences.Editor edit = ctx.getSharedPreferences(
                "SSA_pref", Context.MODE_PRIVATE).edit();

        edit.putInt("versionInt", versionInt);

        edit.commit();
    }



}
