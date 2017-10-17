package com.leagueofshadows.encrypto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Check extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        SharedPreferences sp = getSharedPreferences(Util.preferences, Context.MODE_PRIVATE);
        String username = sp.getString(Util.username,null);
        if(username==null)
        {
            Intent i = new Intent(this,Parent.class);
            startActivity(i);
            finish();
        }
        else
        {
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
            finish();
        }

    }
}
