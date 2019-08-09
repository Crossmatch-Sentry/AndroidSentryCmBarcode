/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;

public class VersionActivity extends Activity {
	
	private static final String LOG_TAG = "CmBarcodeSample";

	private LibBarcode lb = null;
	private String versionName = "unknown";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.version);

		Log.i(LOG_TAG, "PowerActivity:oncreate entry");
		
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.i(LOG_TAG, e.getMessage());
		}
		
				
		lb = MainActivity.lb;
		String libBarcodeVer = lb.version();
		
		TextView txtAppVer = (TextView) findViewById(R.id.txtAppVersion);
		txtAppVer.setText("Apk version " + versionName);
		TextView txtLibbarcodeVer = (TextView) findViewById(R.id.txtLibbarcodeVersion);
		txtLibbarcodeVer.setText("LibBarcode version : " + libBarcodeVer);
	}

}
