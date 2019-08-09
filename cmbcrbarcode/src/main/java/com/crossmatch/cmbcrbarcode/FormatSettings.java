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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;
import com.crossmatch.libbarcode.LibBarcode.Format;
import com.crossmatch.libbarcode.LibBarcode.Query;


public class FormatSettings extends Activity {

	private static final String LOG_TAG = "CmBarcodeSample";

	private LibBarcode lb = null;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	private ProgressDialog progress;

	private boolean aim     = false;
	private boolean symbol  = false;
	private boolean prefix  = false;
	private boolean suffix = false;
	
	private boolean aimRead = false;
	private boolean symbolRead = false;
	private boolean prefixSuffixRead = false;
	
	private RadioGroup radioGroupAimSymbol;
	private RadioGroup radioGroupPrefixSuffix;
	private Button btnPrefixValue;
	private Button btnSuffixValue;
	private EditText txtPrefixValue;
	private EditText txtSuffixValue;
	private String selfPrefix;
	private String selfSuffix;
	//private RadioButton radioAimSymbolNone;
	//private RadioButton radioAimSymbolAim;
	//private RadioButton radioAimSymbolSymbol;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.format_setting);

		Log.i(LOG_TAG, "FormatSettings:oncreate entry");
				
		lb = MainActivity.lb;
		progress = MainActivity.progress;
		
		radioGroupAimSymbol = (RadioGroup) findViewById(R.id.radioGroupFormatAimSymbol);
		radioGroupPrefixSuffix = (RadioGroup) findViewById(R.id.radioGroupFormatSuffixPrefix);
		//radioAimSymbolNone = (RadioButton) findViewById(R.id.radioButtonFormatNone);
		//radioAimSymbolAim = (RadioButton) findViewById(R.id.radioButtonFormatAim);
		//radioAimSymbolSymbol = (RadioButton) findViewById(R.id.radioButtonFormatSymbol);
		for(int i = 0; i < radioGroupAimSymbol.getChildCount(); i++){
		    ((RadioButton)radioGroupAimSymbol.getChildAt(i)).setEnabled(false);
		}
		radioGroupAimSymbol.check(R.id.radioButtonFormatNone);

		for(int i = 0; i < radioGroupPrefixSuffix.getChildCount(); i++){
		    ((RadioButton)radioGroupPrefixSuffix.getChildAt(i)).setEnabled(false);
		}
		radioGroupPrefixSuffix.check(R.id.radioButtonFormatSuffixPrefixNone);

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

					if (query.contains(LibBarcode.Query.AIM.toString())) {
						aimRead = true;
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage QUERY AIM enabled");
							aim = true;
							symbol = false;
						} else {
							Log.i(LOG_TAG, "handleMessage QUERY AIM disabled");
							aim = false;
						}
					}
					if (query.contains(LibBarcode.Query.CODE_ID.toString())) {
						symbolRead = true;
						if (query.contains(LibBarcode.QueryKey.ENABLE.toString())) {
							Log.i(LOG_TAG, "handleMessage QUERY SYMBOL enabled");
							symbol = true;
							aim = false;
						} else {
							Log.i(LOG_TAG, "handleMessage QUERY SYMBOL disabled");
							symbol = false;
						}
					}
					if (query.contains(LibBarcode.Query.SUFFIX_PREFIX.toString())) {
						prefixSuffixRead = true;
						prefix = false;
						suffix = false;
						if (query.contains("Prefix data"))  {
							prefix = true;
						}							
						if (query.contains("data Suffix"))  {
							suffix = true;
						}							
					}

					if (query.contains(Query.PREFIX_CHAR.toString())) {
						String[] queryArray = query.split(" ");
						Log.i(LOG_TAG, "Suffix char [0] " + queryArray[queryArray.length-1]);
						if (queryArray[queryArray.length-1].matches("^-?\\d+$") == true ) {
							selfPrefix = queryArray[queryArray.length-1];
							txtPrefixValue.setEnabled(false);
							txtPrefixValue.setText(selfPrefix);
							txtPrefixValue.setEnabled(true);
						}
					}

					if (query.contains(Query.SUFFIX_CHAR.toString())) {
						String[] queryArray = query.split(" ");
						Log.i(LOG_TAG, "Suffix char [0] " + queryArray[queryArray.length-1]);
						if (queryArray[queryArray.length-1].matches("^-?\\d+$") == true ) {
							selfSuffix = queryArray[queryArray.length-1];
							txtSuffixValue.setEnabled(false);
							txtSuffixValue.setText(selfSuffix);
							txtSuffixValue.setEnabled(true);
						}
					}

					setAimSymbolRadio();							
					setPrefixSuffixRadio();						
					setFormatText();

					aimSymbolListener();
					prefixSuffixListener();

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
		
		setAimSymbolRadio();
		setPrefixSuffixRadio();
		setPrefixSuffixValues();
		setFormatText();
		
		querySettings();
	}
	
	protected void onResume() {
		super.onResume();
		
		bl = new BarcodeListener(this, uiUpdateHandler, progress);
		lb.resume();
	}	

	
	private void setAimSymbolRadio()
	{
		Log.i(LOG_TAG, "setAimSymbolRadio  entry");
		boolean enable = true;
		if ((aimRead == false) || (symbolRead == false)) enable = false;
		
		for(int i = 0; i < radioGroupAimSymbol.getChildCount(); i++){
			((RadioButton)radioGroupAimSymbol.getChildAt(i)).setEnabled(enable);
		}
		Log.i(LOG_TAG, "setAimSymbolRadio  set radiogroup " + Boolean.toString(enable));
			
		if (enable) {
			Log.i(LOG_TAG, "setAimSymbolRadio  setting buttons");

			if ((aim == false) && (symbol == false)) {
				Log.i(LOG_TAG, "setAimSymbolRadio  setting button none");
				radioGroupAimSymbol.check(R.id.radioButtonFormatNone);

			}
			if (aim) {
				Log.i(LOG_TAG, "setAimSymbolRadio  setting buttons aim");
				radioGroupAimSymbol.check(R.id.radioButtonFormatAim);
			}
			if (symbol) {
				Log.i(LOG_TAG, "setAimSymbolRadio  setting buttons symbol");
				radioGroupAimSymbol.check(R.id.radioButtonFormatSymbol);
			}
		}
		
	}
	private void setPrefixSuffixRadio()
	{
		boolean enable = prefixSuffixRead;

		for(int i = 0; i < radioGroupPrefixSuffix.getChildCount(); i++){
			((RadioButton)radioGroupPrefixSuffix.getChildAt(i)).setEnabled(enable);
		}

		Log.i(LOG_TAG, "setPrefixSuffixRadio " + Boolean.toString(prefixSuffixRead) + " " + Boolean.toString(prefix) + " " + Boolean.toString(suffix));

		if (enable) {
			Log.i(LOG_TAG, "setPrefixSuffixRadio  setting buttons aim");
			if ((prefix) && (suffix)) {
				Log.i(LOG_TAG, "setPrefixSuffixRadio  both");
				radioGroupPrefixSuffix.check(R.id.radioButtonFormatSuffixPrefixBoth);
				return;
			}  
			if (prefix) {
				Log.i(LOG_TAG, "setPrefixSuffixRadio  prefix");
				radioGroupPrefixSuffix.check(R.id.radioButtonFormatPrefix);
				return;
			}
			if (suffix) {
				Log.i(LOG_TAG, "setPrefixSuffixRadio  suffix");
				radioGroupPrefixSuffix.check(R.id.radioButtonFormatSuffix);
				return;
			}
			if ((prefix == false) && (suffix == false)) {
				Log.i(LOG_TAG, "setPrefixSuffixRadio  none");
				radioGroupPrefixSuffix.check(R.id.radioButtonFormatSuffixPrefixNone);
				return;
			}
		}
		
	}
	
	private void setFormatText() {
		String strAimSymbol = new String();
		String strBarcode = new String("<barcode>");
		String strPrefix = new String();
		String strSuffix1 = new String();
		String strSuffix2 = new String();
				
		switch(radioGroupAimSymbol.getCheckedRadioButtonId()) {
		case R.id.radioButtonFormatNone:
			strAimSymbol = new String();
			break;
		case R.id.radioButtonFormatAim:
			strAimSymbol = new String("<Aim>");
			break;
		case R.id.radioButtonFormatSymbol:
			strAimSymbol = new String("<Symbol>");
			break;
		default:
			strAimSymbol = new String();
			break;			
		}
		
		if (prefix) {
			strPrefix = new String("<prefix>");
		} else {
			strPrefix = new String();
		}
		
		if (suffix) {
			strSuffix1 = new String("<suffix>");
		} else {
			strSuffix1 = new String();
		}
		
		TextView textviewFormat = (TextView) findViewById(R.id.formatbarcode);
		textviewFormat.setText(strPrefix+strAimSymbol+strBarcode+strSuffix1+strSuffix2);
		textviewFormat.setTextSize(30);	
	}
	
	private void querySettings() {
		aimRead = false;
		symbolRead = false;
		prefixSuffixRead = false;
		Log.i(LOG_TAG, "querySettings  sendQuery AIM");
		lb.sendQuery(LibBarcode.Query.AIM);
		waitSpinner();
		Log.i(LOG_TAG, "querySettings  sendQuery SUFFIX_PREFIX");
		lb.sendQuery(LibBarcode.Query.SUFFIX_PREFIX);
		waitSpinner();
		Log.i(LOG_TAG, "querySettings  sendQuery CODE_ID");
		lb.sendQuery(LibBarcode.Query.CODE_ID);
		waitSpinner();
		lb.sendQuery(Query.PREFIX_CHAR);
		lb.sendQuery(Query.SUFFIX_CHAR);
		waitSpinner();
	}
	
	private void aimSymbolListener() {

		if ((aimRead == false) || (symbolRead == false)) return;
		
		radioGroupAimSymbol.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				// wait for queries to complete
				if ((aimRead == false) || (symbolRead == false)) return;
				
				Log.i(LOG_TAG, "setOnCheckedChangeListener " + Integer.toString(checkedId) + " " + Boolean.toString(aimRead) + " " + Boolean.toString(symbolRead) );
				if (checkedId == R.id.radioButtonFormatNone) {
					if (aim) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand DO_NOT_ADD_AIM_PREFIX");
						lb.sendCommand(LibBarcode.Command.DO_NOT_ADD_AIM_PREFIX);
						aim = false;
						waitSpinner();
					}
					if (symbol) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand DISABLE_CODE_ID");
						lb.sendCommand(LibBarcode.Command.DISABLE_CODE_ID);
						symbol = false;
						waitSpinner();
					}
				}
				if (checkedId == R.id.radioButtonFormatAim) {
					if (aim == false) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand ADD_ALL_AIM_PREFIX");
						lb.sendCommand(LibBarcode.Command.ADD_ALL_AIM_PREFIX);
						aim = true;
						waitSpinner();
					}
				}
				if (checkedId == R.id.radioButtonFormatSymbol) {
					if (symbol == false) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand ENABLE_CODE_ID");
						lb.sendCommand(LibBarcode.Command.ENABLE_CODE_ID);
						symbol = true;
						waitSpinner();
					}
				}
								
				setFormatText();
			}
		});

	}
	
	private void prefixSuffixListener() {
		
		if (prefixSuffixRead == false) return;

		radioGroupPrefixSuffix.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				
				if (prefixSuffixRead == false) return;
				
				if (checkedId == R.id.radioButtonFormatSuffixPrefixNone) {
					if ((prefix) || (suffix)) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand DISABLE_ALL_PREFIX_SUFFIX");
						lb.sendCommand(LibBarcode.Command.DISABLE_ALL_PREFIX_SUFFIX);
						prefix = false;
						suffix = false;
						waitSpinner();
					}
				}
				if (checkedId == R.id.radioButtonFormatPrefix) {
					//if (prefix == false) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand ENABLE_SELF_PREFIX");
						lb.sendCommand(LibBarcode.Command.ENABLE_SELF_PREFIX);
						prefix = true;
						waitSpinner();
					//}
					//if (suffix) {
					//	Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand DISABLE_SELF_SUFFIX");
					//	lb.sendCommand(LibBarcode.Command.DISABLE_SELF_SUFFIX);
					//	suffix = false;
					//	waitSpinner();
					//}
				}
				if (checkedId == R.id.radioButtonFormatSuffix) {
					//if (suffix == false) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand ENABLE_SELF_SUFFIX");
						lb.sendCommand(LibBarcode.Command.ENABLE_SELF_SUFFIX);
						suffix = true;
						waitSpinner();
					//}
					//if (prefix) {
					//	Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand DISABLE_SELF_PREFIX");
					//	lb.sendCommand(LibBarcode.Command.DISABLE_SELF_PREFIX);
					//	prefix = false;
					//	waitSpinner();
					//}
				}
				if (checkedId == R.id.radioButtonFormatSuffixPrefixBoth) {
					if ((prefix == false) || (suffix == false)) {
						Log.i(LOG_TAG, "setOnCheckedChangeListener  sendCommand ENABLE_ALL_PREFIX_SUFFIX");
						lb.sendCommand(LibBarcode.Command.ENABLE_ALL_PREFIX_SUFFIX);
						prefix = true;
						suffix = true;
						waitSpinner();
					}
				}
				
				setFormatText();
			}
		});
	}
	
	private void setPrefixSuffixValues () {
		
		txtPrefixValue = (EditText) findViewById(R.id.txtPrefixValue);
		txtPrefixValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
				selfPrefix = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
			
		});

		btnPrefixValue = (Button) findViewById(R.id.btnSetPrefixValue);
		btnPrefixValue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener sending trigger");
				
				lb.sendCommand(LibBarcode.Command.SELF_PREFIX_MESSAGE, selfPrefix, LibBarcode.Format.DECIMAL_NUMBER );
				
			}

		});

		txtSuffixValue = (EditText) findViewById(R.id.txtSuffixValue);
		txtSuffixValue.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
				selfSuffix = s.toString();
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
			
		});

		btnSuffixValue = (Button) findViewById(R.id.btnSetSuffixValue);
		btnSuffixValue.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener sending trigger");
				
				lb.sendCommand(LibBarcode.Command.SELF_SUFFIX_MESSAGE, selfSuffix, Format.DECIMAL_NUMBER );
				
			}

		});
	}
	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}

}
