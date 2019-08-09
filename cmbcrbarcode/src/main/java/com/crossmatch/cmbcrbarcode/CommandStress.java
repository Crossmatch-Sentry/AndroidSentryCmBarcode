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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;


public class CommandStress  extends Activity {
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ProgressDialog progress;

	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	
	private ProgressBar sentProgress;
	private ProgressBar recProgress;
	private TextView txtRec;
	private TextView txtSent;
	
	private int loops = 10;
	private int goal = 0;
	private int recCount = 0;
	private int sentCount = 0;
	
	private LibBarcode.Command [] stresCommands = {
	//		LibBarcode.Command.ONLY_READ_DOUBLE_1D,
			LibBarcode.Command.ENABLE_ALL_BARCODES,
	//		LibBarcode.Command.ONLY_READ_SINGLE_1D,
			LibBarcode.Command.ENABLE_ALL_BARCODES
	};
	
	
	public static Handler uiUpdateHandler;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.component_stress);

		Log.i(LOG_TAG, "TriggerActivity:oncreate entry");

		lb = MainActivity.lb;
		progress = new ProgressDialog(this);

		sentProgress = (ProgressBar) findViewById(R.id.progressBarSent);
		recProgress = (ProgressBar) findViewById(R.id.progressBarReceived);

		goal = (loops * stresCommands.length) + 2;
		
		sentProgress.setVisibility(View.VISIBLE);
		sentProgress.setMax(goal);
		sentProgress.setProgress(sentCount);

		recProgress.setVisibility(View.VISIBLE);
		recProgress.setMax(goal);
		recProgress.setProgress(recCount);
		
		txtRec = (TextView) findViewById(R.id.textRec);
		txtSent = (TextView) findViewById(R.id.textSent);
		

		
		Button btnStart = (Button) findViewById(R.id.btnCsStart);
		btnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "CommantStress:setOnClickListener start");


				sentCount = 0;
				recCount  = 0;
				
				lb.setProgrammingMode(LibBarcode.ProgrammingMode.ENABLE);
				sentCount++;
				sentProgress.setProgress(sentCount);
				txtSent.setText(String.valueOf(sentCount));

				for (int i=0; i<loops; i++) {
					for (int c=0; c < stresCommands.length; c++) {
						lb.sendCommand(stresCommands[c]);
						sentCount++;
						sentProgress.setProgress(sentCount);
						txtSent.setText(String.valueOf(sentCount));
					}
				}

				lb.setProgrammingMode(LibBarcode.ProgrammingMode.DISABLE);
				sentCount++;
				sentProgress.setProgress(sentCount);
				txtSent.setText(String.valueOf(sentCount));
				
				waitSpinner();
			}
		});
		
		Button btnStop = (Button) findViewById(R.id.btCsStop);
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i(LOG_TAG, "CommandStress:setOnClickListener stop");
			}
		});
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(LOG_TAG, "CommandStress::onStart entry");
		setupMessageHandler();
		bl = new BarcodeListener(this, uiUpdateHandler, progress);

	}

	
	private void setupMessageHandler() {
		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				//int value = msg.arg2;
				Log.i(LOG_TAG, "CommandStress::handleMessage recevied this msg " + msg.arg1 + " " + (String) msg.obj);
				switch (cmd) {
				case MainActivity.MESSAGE_COMMAND_COMPLETE:
				case MainActivity.MESSAGE_BARCODE_STRING:
					recCount++;
					recProgress.setProgress(recCount);
					txtRec.setText(String.valueOf(recCount));
					Log.i(LOG_TAG, "CommandStress::handleMessage recCount " + recCount);
					
				default:
					super.handleMessage(msg);
				}
			}
		};
	}


	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}


}
