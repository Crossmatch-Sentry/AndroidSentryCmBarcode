/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.crossmatch.libbarcode.LibBarcode;

public class LightingActivity extends Activity {
	
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ProgressDialog progress;
	private Handler uiUpdateHandler;
	RadioGroup radioGroupIllumination;
	RadioGroup radioGroupIllumination_level;
    private boolean listenersSet = false;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.lighting);

		Log.i(LOG_TAG, "HardwareSettings:oncreate entry");
				
		lb = MainActivity.lb;

		
		radioGroupIllumination = (RadioGroup) findViewById(R.id.lightingRadioGroup);
		radioGroupIllumination_level = (RadioGroup) findViewById(R.id.illuminationRadioGroup);

		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				//int value = msg.arg2;
				Log.i(LOG_TAG, "FormatSettings::handleMessage recevied this msg " + msg.arg1 + " " + (String) msg.obj);
				switch (cmd) {
				case MainActivity.MESSAGE_BARCODE_STRING:
					Log.i(LOG_TAG, "handleMessage writing text ");
					break;
				case MainActivity.MESSAGE_QUERY_STRING:
					String query = (String) msg.obj;
					Log.i(LOG_TAG, "handleMessage writing query text [" + query + "]");
					
					if (query.contains(LibBarcode.Query.LIGHT.toString())) {
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonOn");
							radioGroupIllumination.check(R.id.radioButtonOn);
						}
						if (query.contains(LibBarcode.QueryKey.DISABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonOff");
							radioGroupIllumination.check(R.id.radioButtonOff);
						}
					}

					if (query.contains(LibBarcode.Query.LIGHT_LEVEL.toString())) {
						if (query.contains(LibBarcode.QueryKey.LOW.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonLow");
							radioGroupIllumination_level.check(R.id.radioButtonLow);
						}
						if (query.contains(LibBarcode.QueryKey.HIGH.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonHigh");
							radioGroupIllumination_level.check(R.id.radioButtonHigh);
						}

                        setListeners();
                    }

					break;
				case MainActivity.MESSAGE_COMMAND_COMPLETE:
					break;
				case MainActivity.MESSAGE_QUERY_ERROR:
				default:
					super.handleMessage(msg);
				}
			}
		};

		progress = new ProgressDialog(this);
        BarcodeListener bl = new BarcodeListener(this, uiUpdateHandler, progress);


		lb.sendQuery(LibBarcode.Query.LIGHT);
		lb.sendQuery(LibBarcode.Query.LIGHT_LEVEL);
		
	}


    private void setListeners() {
        if (listenersSet == false) {
            radioGroupIllumination.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Log.i(LOG_TAG, "onCheckedChanged LED ON/OFF");

                    if (checkedId == R.id.radioButtonOn) {
                        lb.setLightingMode(LibBarcode.LightingMode.LED_ON);
                        waitSpinner();
                        return;
                    }
                    if (checkedId == R.id.radioButtonOff) {
                        lb.setLightingMode(LibBarcode.LightingMode.LED_OFF);
                        waitSpinner();
                        return;
                    }

                }
            });

            radioGroupIllumination_level.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Log.i(LOG_TAG, "onCheckedChanged LED HIGH/LOW");

                    if (checkedId == R.id.radioButtonLow) {
                        lb.setLightingMode(LibBarcode.LightingMode.LIGHT_LED_LOW);
                        waitSpinner();
                        return;
                    }
                    if (checkedId == R.id.radioButtonHigh) {
                        lb.setLightingMode(LibBarcode.LightingMode.LIGHT_LED_HIGH);
                        waitSpinner();
                        return;
                    }
                }
            });
            listenersSet = true;
        }
	}
	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}


}
