/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.crossmatch.libbarcode.LibBarcode;
import com.crossmatch.libbarcode.LibBarcode.LibBarcodeListener;

public class BarcodeListener {

	private static final String LOG_TAG = "CmBarcodeSample";

	private LibBarcode lb = null;
	private ProgressDialog progress;
	@SuppressWarnings("unused")
	private Context context = null;
	public static Handler handler;
	
	BarcodeListener(Context c, Handler h, ProgressDialog p ) {
		lb = MainActivity.lb;
		progress = p;
		context = c;
		handler = h;
		setupBarcodeListener();
	}
	
	private void setupBarcodeListener() {
		lb.setLibBarcodeListener(new LibBarcodeListener() {
			@Override
			public void onBarcodeDataAvailable(String arg0) {
				Log.i(LOG_TAG, "BarcodeListener:onBarcodeDataAvailable recevied this string [" + arg0	+ "]");
				Message msg = handler.obtainMessage();
				msg.arg1 = MainActivity.MESSAGE_BARCODE_STRING;
				msg.obj = arg0;
				handler.sendMessage(msg);				
			}

			@Override
			public void onBarcodeByteDataAvailable(byte[] arg0) {
				Log.i(LOG_TAG,
						"BarcodeListener:onBarcodeByteDataAvailable recevied this string ["
								+ arg0 + "]");
				String strArg0 = new String(arg0);
				Message msg = handler.obtainMessage();
				msg.arg1 = MainActivity.MESSAGE_BARCODE_STRING;
				msg.obj = strArg0;
				handler.sendMessage(msg);
			}
			
			@Override
			public void onCommandResultAvailable(String command, int value) {
				Log.i(LOG_TAG,"BarcodeListener::onCommandResultAvailable " + command + " " + value);
				progress.dismiss();
				if (value == LibBarcode.RESULT_SUCCESS ) {
					sendUiUpdateMessage(MainActivity.MESSAGE_COMMAND_COMPLETE, command + " Successful", 0);
				} else {					
					sendUiUpdateMessage(MainActivity.MESSAGE_COMMAND_COMPLETE, command + " Failed", 0);
				}
			}

			@Override
			public void onQueryResultAvailable(String command, String value) {
				Log.i(LOG_TAG,"BarcodeListener::onQueryResultAvailable " + command + " " + value);
				progress.dismiss();
				sendUiUpdateMessage(MainActivity.MESSAGE_QUERY_STRING, command + " " + value, 0);
			}


		});
	}
	
	public void sendUiUpdateMessage(int message, String obj, int delay){
		Message msg = handler.obtainMessage();
		msg.arg1 = message;
		msg.obj = obj;					
		if (delay > 0) {
			handler.sendMessageDelayed(msg, delay);
			
		} else {
			handler.sendMessage(msg);
		}
	}
}
