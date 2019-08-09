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
import android.widget.EditText;

import com.crossmatch.libbarcode.LibBarcode;


public class PowerActivity extends Activity {

	private static final String LOG_TAG = "CmBarcodeSample";

	private LibBarcode lb = null;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	private ProgressDialog progress;
	private EditText txtPowerValue;
	private String powerTimeout;
	private Button btnSetPowerTimeout;
	private Button btnForceSleep;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.power);

		Log.i(LOG_TAG, "PowerActivity:oncreate entry");
				
		lb = MainActivity.lb;
		progress = MainActivity.progress;
		
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
					Log.i(LOG_TAG, "handleMessage writing query text ");
					String query = (String) msg.obj;
					Log.i(LOG_TAG, "handleMessage writing query text [" + query + "]");
					
					if (query.contains(LibBarcode.Query.LOW_POWER_DELAY.toString())) {

						int index = LibBarcode.Query.LOW_POWER_DELAY.toString().length() + 1;
						String sub = query.substring(index);
						Log.i(LOG_TAG, "sub  " + sub);
						powerTimeout = sub;
						
					}
					
					setPowerTimeValue();

					break;
				case MainActivity.MESSAGE_COMMAND_COMPLETE:
					break;
				case MainActivity.MESSAGE_QUERY_ERROR:
				default:
					super.handleMessage(msg);
				}
			}
		};	
		
		addPowerTimeoutListener();
		lb.sendQuery(LibBarcode.Query.LOW_POWER_DELAY);
		waitSpinner();

	}
	
	protected void onResume() {
		super.onResume();
		
		bl = new BarcodeListener(this, uiUpdateHandler, progress);
		lb.resume();
	}
	
	private void setPowerTimeValue() {
		txtPowerValue.setText(powerTimeout);
	}
	
	private void addPowerTimeoutListener() {
		txtPowerValue = (EditText) findViewById(R.id.txtTimeoutValue);
		txtPowerValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
				powerTimeout = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
			
		});

		btnSetPowerTimeout = (Button) findViewById(R.id.btnSetTimeoutValue);
		btnSetPowerTimeout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener set sleep timeout");
				
				lb.sendCommand(LibBarcode.Command.SET_IDLE_TO_SLEEP, powerTimeout, LibBarcode.Format.HEX_NUMBER );
				
			}

		});

		btnForceSleep = (Button) findViewById(R.id.btnForceSleep);
		btnForceSleep.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener force sleep");
				
				lb.sendCommand(LibBarcode.Command.SLEEP);
				
			}

		});
	}

	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}
}
