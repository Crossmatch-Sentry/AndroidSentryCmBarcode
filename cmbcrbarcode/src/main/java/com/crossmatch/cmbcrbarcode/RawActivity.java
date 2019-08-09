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
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;

public class RawActivity extends Activity implements OnClickListener {
	
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ProgressDialog progress;
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	
	private TextView txtRawOutput;
	private TextView txtRawResult;
	
	private Button btnGoodCommand;
	private Button btnGoodQuery;
	private Button btnBadCommand;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.raw_command);

		txtRawOutput = (TextView) findViewById(R.id.txtrawoutput);
		txtRawResult = (TextView) findViewById(R.id.txtrawresult);
		btnGoodCommand = (Button) findViewById(R.id.btnRawGoodCommand);
		btnGoodCommand.setOnClickListener(this);
		btnGoodQuery = (Button) findViewById(R.id.btnRawGoodQuery);
		btnGoodQuery.setOnClickListener(this);
		btnBadCommand = (Button) findViewById(R.id.btnRawBadCommand);
		btnBadCommand.setOnClickListener(this);
		
		Log.i(LOG_TAG, "BarcodeActivity:oncreate entry");
				
		lb = MainActivity.lb;
		
		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				//int value = msg.arg2;
				Log.i(LOG_TAG, "MainActivity::handleMessage recevied this msg " + msg.arg1 + " " + (String) msg.obj);
				switch (cmd) {
				case MainActivity.MESSAGE_BARCODE_STRING:
					Log.i(LOG_TAG, "handleMessage writing text ");
					String msgText = (String) msg.obj;
						String barcode = msgText;
						txtRawOutput.setText(barcode);
					break;
				case MainActivity.MESSAGE_QUERY_STRING:
					Log.i(LOG_TAG, "handleMessage writing query text ");
					String query = (String) msg.obj;
					txtRawOutput.setText(query);
					break;
				case MainActivity.MESSAGE_COMMAND_COMPLETE:
					String result = (String) msg.obj;
					txtRawResult.setText(result);
					break;
				case MainActivity.MESSAGE_START_SCAN:
					break;
				case MainActivity.MESSAGE_STOP_SCAN:
					break;
				case MainActivity.MESSAGE_QUERY_ERROR:
					String queryErr = (String) msg.obj;
					txtRawOutput.setText(queryErr);
				default:
						super.handleMessage(msg);
				}
			}
		};		
		
		progress = new ProgressDialog(this);
		bl = new BarcodeListener(this, uiUpdateHandler, progress);

	}
	
	
	@Override
	public void onClick(View v) {

	    switch (v.getId()) {

	    case R.id.btnRawGoodCommand:
	    	//lb.sendRawCommand("Good", new byte [] { (byte) 0x04, (byte) 0xe5, (byte) 0x04, (byte) 0x08, (byte) 0xff, (byte) 0x0b});
	    	lb.sendRawCommand("Good command", new byte [] { (byte) 0xe5, (byte) 0x04, (byte) 0x08});
	    	waitSpinner();
	        break;
	    case R.id.btnRawGoodQuery:
	    	lb.sendRawCommand("Good query", new byte [] { (byte) 0xC7, (byte) 0x04, (byte) 0x08, (byte) 0x2b, (byte) 0x30, (byte) 0x2f, (byte) 0x34, (byte) 0xf1, (byte) 0x6f, (byte) 0xf1, (byte) 0x6e});
	    	waitSpinner();
	        break;
	    case R.id.btnRawBadCommand:
	    	lb.sendRawCommand("bad", new byte [] { (byte) 3, (byte) 0x08, (byte) 10});
	    	waitSpinner();
	        break;
	    default:
	        break;
	    }
	}

	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}

}
