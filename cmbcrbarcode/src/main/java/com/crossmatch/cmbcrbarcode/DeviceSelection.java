/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class DeviceSelection extends Activity {

	private static final String LOG_TAG = "CmBarcodeSample";
	private ListView lv;
	private Intent i;
	
	public static final String ACTION_DEVICE = "Action_device";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_selection);

		Log.i(LOG_TAG, "oncreate entry");
		
		lv = (ListView) findViewById(R.id.device_selection);
		
		String[] values = new String[5];
		
		values[0] = getString(R.string.EA30);
		values[1] = getString(R.string.EM3070);
		values[2] = getString(R.string.JE222);
		values[3] = getString(R.string.JE227);
		values[4] = getString(R.string.JE227Serial);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
		lv.setAdapter(adapter);
		
        lv.setOnItemClickListener(new OnItemClickListener() {
        	 
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	// ListView Clicked item value
            	String itemValue    = (String) lv.getItemAtPosition(position);

    			i = getIntent();
    			i.putExtra(ACTION_DEVICE, itemValue);
    			setResult(RESULT_OK,i);
    			finish();
            }
       }); 
		
	}
}
