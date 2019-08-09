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
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.crossmatch.libbarcode.LibBarcode;


public class AudioActivity extends Activity implements OnCheckedChangeListener {
	
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ProgressDialog progress;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	CheckBox cbAudioGoodRead;
	CheckBox cbAudioPowerUp;
	int audioRead = 0;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.audio);

		Log.i(LOG_TAG, "HardwareSettings:oncreate entry");
				
		lb = MainActivity.lb;

		audioRead = 0;
		cbAudioGoodRead = (CheckBox) findViewById(R.id.cbAudioGoodRead);
		cbAudioGoodRead.setVisibility(View.INVISIBLE);
		cbAudioGoodRead.setChecked(false);
		cbAudioGoodRead.setOnCheckedChangeListener(this);
		
		cbAudioPowerUp = (CheckBox) findViewById(R.id.cbAudioPowerUp);
		cbAudioPowerUp.setVisibility(View.INVISIBLE);
		cbAudioPowerUp.setChecked(false);
		cbAudioPowerUp.setOnCheckedChangeListener(this);
		
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
					
					if (query.contains(LibBarcode.Query.READ_BEEP.toString())) {
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage enable");
							cbAudioGoodRead.setVisibility(View.VISIBLE);
							cbAudioGoodRead.setChecked(true);
							audioRead |= 1;
						}
						if (query.contains(LibBarcode.QueryKey.DISABLE.toString())) {
							cbAudioGoodRead.setVisibility(View.VISIBLE);
							cbAudioGoodRead.setChecked(false);
							audioRead |= 1;
						}
						break;
					}

					if (query.contains(LibBarcode.Query.POWERUP_BEEP.toString())) {
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage enable");
							cbAudioPowerUp.setVisibility(View.VISIBLE);
							cbAudioPowerUp.setChecked(true);
							audioRead |= 2;
						}
						if (query.contains(LibBarcode.QueryKey.DISABLE.toString())) {
							cbAudioPowerUp.setVisibility(View.VISIBLE);
							cbAudioPowerUp.setChecked(false);
							audioRead |= 2;
						}
						break;
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
		bl = new BarcodeListener(this, uiUpdateHandler, progress);


		lb.sendQuery(LibBarcode.Query.READ_BEEP);
		lb.sendQuery(LibBarcode.Query.POWERUP_BEEP);		
	}
	
	
	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}


	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		Log.i(LOG_TAG, "onCheckedChanged entry");
		switch (arg0.getId()) {
		case R.id.cbAudioGoodRead:
			if (audioRead == 3) {
				if (arg1) {
					
					lb.setAudioMode(LibBarcode.AudioMode.READ_BEEP_ENABLE);
				} else {					
					lb.setAudioMode(LibBarcode.AudioMode.READ_BEEP_DISABLE);
				}
			}
			break;
		case R.id.cbAudioPowerUp:
			if (audioRead == 3) {
				if (arg1) {
					
					lb.setAudioMode(LibBarcode.AudioMode.POWERUP_BEEP_ENABLE);
				} else {					
					lb.setAudioMode(LibBarcode.AudioMode.POWERUP_BEEP_DISABLE);
				}
			}
			break;
		}
		
	}


}