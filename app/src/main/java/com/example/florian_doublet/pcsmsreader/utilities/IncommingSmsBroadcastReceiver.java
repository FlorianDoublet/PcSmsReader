package com.example.florian_doublet.pcsmsreader.utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

/**
 * Created by flori on 05/05/2016.
 */
public class IncommingSmsBroadcastReceiver extends BroadcastReceiver {

    // Get the object of SmsManager
    private final SmsManager smsManager = SmsManager.getDefault();

    @Override
    public void onReceive(Context context, Intent intent) {
        //something
    }
}
