/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by dms on 8/30/17.
 *
 * This activity allows android to remember the USB permissions state. Without this activity
 * Android will request permission to access the usb bus every time the device comes out of sleep.
 * The disadvantage of using this approach is that your app will pop up every time the device comes
 * out of sleep. You can force the app to close to pop up and close if you set the activity "launchMode"
 * to "singleInstance" and call "finish()" when the activity runs. This will cause this activity to pop
 * up and immediately return to the previous activity.
 *
 * the manifest activity option launchMode will change how the app responds
 * to USB devices hotswaping.
 *     -onResume
 *      - standard - will put app on top off all apps
 *      - singleTop - will put app on top off all apps
 *      - singleTask - will put app on top off all apps
 *      - singleInstance - will put app on top and cause app to immediatly close
 */

public class UsbActivity extends Activity {
    private static final String LOG_TAG = "CmBarcodeSample";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "UsbActivity::onNewIntent  entry");
        Intent intent = getIntent();
        if (intent != null) {
            notifiyIntent(intent);
        } else {
            Log.e(LOG_TAG, "UsbActivity::onCreate No intent");
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(LOG_TAG, "UsbActivity::onNewIntent  entry");
        super.onNewIntent(intent);
        notifiyIntent(intent);
        finish();
    }

    private void notifiyIntent(Intent intent) {
        if (MainActivity.lb != null) {
            Log.i(LOG_TAG, "UsbActivity::notifiyIntent  notify Libbarcode");
            MainActivity.lb.intent(intent);

        } else  {
            Log.e(LOG_TAG, "UsbActivity::notifiyIntent libbarcode not created in MainActivity");
        }

    }
}
