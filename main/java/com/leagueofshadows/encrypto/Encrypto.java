package com.leagueofshadows.encrypto;

import android.app.Application;
import android.content.Context;

public class Encrypto extends Application {
    private static Encrypto encrypto;
    private static Context context=null;

    public static Encrypto getEncrypto() {
        return encrypto;
    }

    Boolean refresh()
    {
        if(context == null)
        {
            return false;
        }
        else
        {
            Refresh refresh = (Refresh) context;
            refresh.refresh();
            return true;
        }
    }

    void setContext(Context context) {
        Encrypto.context = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        encrypto=this;
    }

}
