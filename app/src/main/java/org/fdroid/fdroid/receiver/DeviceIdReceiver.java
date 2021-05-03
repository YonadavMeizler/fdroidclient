package org.fdroid.fdroid.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.fdroid.fdroid.Preferences;

public class DeviceIdReceiver extends BroadcastReceiver {

    public final static String INTENT_FILTER = "org.fdroid.fdroid.action.DEVICE_ID";
    public final static String ODIN_ACTION = "com.MotorolaSolutions.OdinM.action.REQUESTS";
    public final static String EXTRA_REQUEST = "REQUEST";
    public final static String DEVICE_ID_MESSAGE = "DEVICE_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String deviceId = intent.getStringExtra(DEVICE_ID_MESSAGE);
        Preferences pref = Preferences.get();
        if(deviceId!=null && !deviceId.equals(""))
            pref.setDeviceID(deviceId);
    }
}