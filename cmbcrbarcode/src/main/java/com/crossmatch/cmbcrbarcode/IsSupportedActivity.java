/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;
import com.crossmatch.libbarcode.LibBarcode.Command;
import com.crossmatch.libbarcode.LibBarcode.EncodingOption;
import com.crossmatch.libbarcode.LibBarcode.EncodingType;
import com.crossmatch.libbarcode.LibBarcode.Query;
import com.crossmatch.libbarcode.LibBarcode.TriggerCapture;


public class IsSupportedActivity extends Activity {
	private static final String LOG_TAG = "CmBarcodeSample";
	
	private LibBarcode lb = null;
	private ViewGroup vgParent;

	
	private class SupportView {
		private Context context;
		
		SupportView(Context c, String type, String function, String result) {
			context = c;
			
			View view = LayoutInflater.from(context).inflate(R.layout.support_inflator, null);
			vgParent.addView(view);
			
			TextView txtSupportType = (TextView) view.findViewById(R.id.txtSupportType);
			txtSupportType.setText(type);
			TextView txtSupportFunction = (TextView) view.findViewById(R.id.txtSupportFunction);
			txtSupportFunction.setText(function);
			TextView txtSupportResult = (TextView) view.findViewById(R.id.txtSupportResult);
			txtSupportResult.setText(result);						
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.is_supported);
		vgParent = (ViewGroup) findViewById(R.id.layoutSupport);

		
		Log.i(LOG_TAG, "IsSupportedActivity:oncreate entry");
				
		lb = MainActivity.lb;

	
		for (Command c : Command.values()) {
			boolean b = lb.isSupported(c);
			new SupportView(this, "Command", c.toString(), Boolean.toString(b));
			
		}
		
		for (Query q : Query.values()) {
			boolean b = lb.isSupported(q);
			new SupportView(this, "Query", q.toString(), Boolean.toString(b));
			
		}
		
		for (TriggerCapture t : TriggerCapture.values()) {
			boolean b = lb.isSupported(t);
			new SupportView(this, "TriggerCapture", t.toString(), Boolean.toString(b));
			
		}
		

		for (EncodingType e : EncodingType.values()) {
			boolean b = lb.isSupported(e);
			new SupportView(this, "EncodintType", e.toString(), Boolean.toString(b));
			
		}

		for (EncodingType e : EncodingType.values()) {
			boolean b = lb.isSupported(e);
			if (b) {
				for (EncodingOption o : EncodingOption.values()) {
					boolean bb = lb.isSupported(e,o);
					new SupportView(this, "EncodingOption", e.toString() + ", " + o.toString(), Boolean.toString(bb));
				}				
			}
		}
		

	}

}
