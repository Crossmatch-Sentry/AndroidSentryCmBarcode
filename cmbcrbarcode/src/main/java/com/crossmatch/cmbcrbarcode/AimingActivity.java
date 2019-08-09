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

public class AimingActivity extends Activity {

	private static final String LOG_TAG = "CmBarcodeSample";

	private LibBarcode lb = null;
	private ProgressDialog progress;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	private boolean aimRead = false;
	private boolean aimEnabled = false;

	private RadioGroup radioGroup;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.aiming);

		Log.i(LOG_TAG, "HardwareSettings:oncreate entry");

		lb = MainActivity.lb;
		progress = MainActivity.progress;

		radioGroup = (RadioGroup) findViewById(R.id.lightingRadioGroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (aimRead == false) return;

				if (checkedId == R.id.radioButtonOn) {
					lb.setTargetMode(LibBarcode.TargetMode.AIM_LED_ON);
					waitSpinner();
					return;
				}
				if (checkedId == R.id.radioButtonOff) {
					lb.setTargetMode(LibBarcode.TargetMode.AIM_LED_OFF);
					waitSpinner();
					return;
				}
			}

		});
		
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
					
					if (query.contains(LibBarcode.Query.AIMING.toString())) {
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonOn");
							radioGroup.check(R.id.radioButtonOn);
							aimEnabled = true;
							aimRead = true;
						}
						if (query.contains(LibBarcode.QueryKey.DISABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage radioButtonOff");
							radioGroup.check(R.id.radioButtonOff);
							aimEnabled = false;
							aimRead = true;
						}
					}

					break;
				case MainActivity.MESSAGE_COMMAND_COMPLETE:
					progress.dismiss();
					break;
				case MainActivity.MESSAGE_QUERY_ERROR:
				default:
					super.handleMessage(msg);
				}
			}
		};

		progress = new ProgressDialog(this);
		bl = new BarcodeListener(this, uiUpdateHandler, progress);


		lb.sendQuery(LibBarcode.Query.AIMING);
	}
	
	private void setListeners() {
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (aimRead == false) return;
				if (checkedId == R.id.radioButtonOn) {
					if (aimEnabled == false) {
						lb.setTargetMode(LibBarcode.TargetMode.AIM_LED_ON);
						aimEnabled = true;
						waitSpinner();
						return;
					}
				}
				if (checkedId == R.id.radioButtonOff) {
					if (aimEnabled) {
						lb.setTargetMode(LibBarcode.TargetMode.AIM_LED_OFF);
						aimEnabled = false;
						waitSpinner();
						return;
					}
				}
			}
		});
	}

	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}
}
