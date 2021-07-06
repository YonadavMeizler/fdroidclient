package org.fdroid.fdroid.receiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class OtpReceiver extends BroadcastReceiver {

    public final static String extra = "OTP";
    public final static  String INTENT_FILTER = "org.fdroid.fdroid.action.OTP";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null){
            return;
        }
        if(intent.hasExtra(extra)){
            final String otpCode =  intent.getStringExtra(extra);
            Intent otpIntent = new Intent(extra);
            otpIntent.putExtra(extra, otpCode);
            LocalBroadcastManager.getInstance(context).sendBroadcast(otpIntent);
        }
    }
}
