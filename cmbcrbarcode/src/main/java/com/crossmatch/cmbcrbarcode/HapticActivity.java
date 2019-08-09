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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.crossmatch.libbarcode.LibBarcode;


public class HapticActivity extends Activity implements OnCheckedChangeListener, TextWatcher, OnClickListener {
	
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ProgressDialog progress;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	CheckBox cbHapticEnable;
	EditText editTextHapticDuration;
	Button btnSetHapticDuration;
	boolean hapticRead = false;
	String hapticDuration = "0";

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.haptic);

		Log.i(LOG_TAG, "HapticActivity:oncreate entry");
				
		lb = MainActivity.lb;

		hapticRead = false;
		cbHapticEnable = (CheckBox) findViewById(R.id.cbHapticEnable);
		cbHapticEnable.setVisibility(View.VISIBLE);
		cbHapticEnable.setChecked(false);
		cbHapticEnable.setOnCheckedChangeListener(this);
		editTextHapticDuration = (EditText) findViewById(R.id.editTextHapticDuration);
		editTextHapticDuration.setVisibility(View.VISIBLE);
		editTextHapticDuration.setText(hapticDuration);
		editTextHapticDuration.addTextChangedListener(this);
		btnSetHapticDuration = (Button) findViewById(R.id.btnSetHapticDuration);
		btnSetHapticDuration.setVisibility(View.VISIBLE);
		btnSetHapticDuration.setOnClickListener(this);
		
		
		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				//int value = msg.arg2;
				Log.i(LOG_TAG, "HapticActivity::handleMessage recevied this msg " + msg.arg1 + " " + (String) msg.obj);
				switch (cmd) {
				case MainActivity.MESSAGE_BARCODE_STRING:
					Log.i(LOG_TAG, "handleMessage writing text ");
					break;
				case MainActivity.MESSAGE_QUERY_STRING:
					String query = (String) msg.obj;
					Log.i(LOG_TAG, "handleMessage writing query text [" + query + "]");
					
					if (query.contains(LibBarcode.Query.HAPTIC_DURATION.toString())) {
						String key = LibBarcode.Query.HAPTIC_DURATION.toString();
						int index = query.indexOf(key);
						index += key.length() + 1;
						String sub = query.substring(index);
						String delim = " ";
						String[] tok = sub.split(delim);
						if (tok.length >= 1) {
							Log.i(LOG_TAG, "HapticActivity  " + key + " " + tok[0]);
							index = Integer.valueOf(tok[0]);
						}

						if (index == 0) {
							cbHapticEnable.setVisibility(View.VISIBLE);
							cbHapticEnable.setChecked(false);
							hapticRead = true;
						} else {
							Log.i(LOG_TAG, "handleMessage enable");
							cbHapticEnable.setVisibility(View.VISIBLE);
							cbHapticEnable.setChecked(true);
							hapticRead = true;
						}
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


		lb.sendQuery(LibBarcode.Query.HAPTIC_DURATION);
		
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
			if (hapticRead) {
				if (arg1) {
					lb.setHapticMode(LibBarcode.HapticMode.VIBRATION_ON, hapticDuration);
				} else {					
					lb.setHapticMode(LibBarcode.HapticMode.VIBRATION_OFF, hapticDuration);
				}
			}
			break;
		}
		
	}



	@Override
	public void afterTextChanged(Editable s) {
		Log.i(LOG_TAG, "HapticActivity::handleMessage afterTextChanged " + s.toString());
		if (s.length() < 1) return;
		hapticDuration = s.toString();

	}



	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
	}



	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		Log.i(LOG_TAG, "HapticActivity::handleMessage onTextChanged " + arg0.toString());
	}



	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.btnSetHapticDuration:
			lb.setHapticMode(LibBarcode.HapticMode.VIBRATION_TIMEOUT, hapticDuration);
			break;
		}
	}



}