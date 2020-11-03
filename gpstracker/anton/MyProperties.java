package com.gpstracker.anton;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class MyProperties {

    public void CreatePropertiesFile(Context context, String email, String pass, String room) {
        SharedPreferences pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("email", email);
        editor.putString("pass", pass);
        editor.putString("room", room);
        editor.apply();
    }

    public HashMap<String, String> getProp(Context context){
        SharedPreferences pref = context.getSharedPreferences("MyPref", MODE_PRIVATE);
        HashMap<String, String> hm = new HashMap<>();
        hm.put("email", pref.getString("email", null));
        hm.put("pass", pref.getString("pass", null));
        hm.put("room", pref.getString("room", null));
        return hm;
    }
}
