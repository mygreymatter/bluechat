package com.mayo.bluechat;

import android.app.Application;

import java.util.HashMap;

/**
 * Created by mayo on 13/6/16.
 */
public class Blue extends Application {
    private static Blue mInstance = null;
    public HashMap<String,String> devices;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        devices = new HashMap<>();
    }

    public static Blue getInstance(){
        return mInstance;
    }
}
