/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;
import com.crossmatch.libbarcode.LibBarcode.Devices;


public class MainActivity extends Activity implements OnClickListener {

	private static final String LOG_TAG = "CmBarcodeSample";
	private static final boolean debug = true;
	public static LibBarcode lb;
	public static BarcodeListener bl;
	public static ProgressDialog progress;
	public static Handler uiUpdateHandler;
	
	private TextView barcodeText;
	private TextView resultText;
	
	private Button btnScanStart;
	private Button btnScanStop;
	Button btnDevice;
	Button btnBarcode;
	Button btnFormat;
	Button btnLighting;
	Button btnAiming;
	Button btnRaw;
	Button btnResetDefaults;
	Button btnAudio;
	Button btnHaptic;
	Button btnPower;
	Button btnVersion;
	
	private boolean programmingState = false;


	public static final int MESSAGE_BARCODE_STRING = 1;
	public static final int MESSAGE_COMMAND_COMPLETE = 2;
	public static final int MESSAGE_QUERY_STRING = 3;
	public static final int MESSAGE_START_SCAN    = 4;
	public static final int MESSAGE_STOP_SCAN     = 5;
	public static final int MESSAGE_QUERY_COMPORT = 10;
	public static final int MESSAGE_QUERY_READ_MODE = 11;
	public static final int MESSAGE_QUERY_DELAY_OF_READ = 12;
	public static final int MESSAGE_QUERY_SENSITIVITY = 13;
	public static final int MESSAGE_QUERY_SUFFIX_CHAR = 14;
	public static final int MESSAGE_QUERY_ERROR = 15;
	
	public static final int SCANSTATE_MANUAL     = 0;
	public static final int SCANSTATE_AUTO       = 1;
	public static final int SCANSTATE_CONTINUOUS = 3;
	
	
	public int scanState = SCANSTATE_MANUAL;
	private RadioGroup radioGroup;
	
	private static final int ACTIVITY_SELECT_DEVICE = 1000;
	
	private String currentDevice;
	
	//private int readOnlyVisability = View.GONE;
	private int readOnlyVisability = View.VISIBLE;
	//private int readOnlyVisability = View.INVISIBLE;
	//private int resetDefaultsVisability = View.VISIBLE;
	private int resetDefaultsVisability = View.INVISIBLE;
	
	private WakeLock wl;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		printDebug(LOG_TAG, "oncreate entry");
		
		btnDevice = (Button) findViewById(R.id.btnDevice);
		btnDevice.setVisibility(readOnlyVisability);
		btnDevice.setOnClickListener(this);
		btnBarcode = (Button) findViewById(R.id.btnBarcode);
		btnBarcode.setVisibility(readOnlyVisability);
		btnBarcode.setOnClickListener(this);
		btnFormat = (Button) findViewById(R.id.btnFormat);
		btnFormat.setVisibility(readOnlyVisability);
		btnFormat.setOnClickListener(this);
		btnLighting = (Button) findViewById(R.id.btnLighting);
		btnLighting.setVisibility(readOnlyVisability);
		btnLighting.setOnClickListener(this);
		btnResetDefaults = (Button) findViewById(R.id.btnReset);
		btnResetDefaults.setVisibility(resetDefaultsVisability);
		btnResetDefaults.setOnClickListener(this);
		btnAiming = (Button) findViewById(R.id.btnAiming);
		btnAiming.setVisibility(readOnlyVisability);
		btnAiming.setOnClickListener(this);
		btnRaw = (Button) findViewById(R.id.btnRaw);
		btnRaw.setVisibility(readOnlyVisability);
		btnRaw.setOnClickListener(this);
		btnAudio = (Button) findViewById(R.id.btnAudio);
		btnAudio.setVisibility(readOnlyVisability);
		btnAudio.setOnClickListener(this);
		btnHaptic = (Button) findViewById(R.id.btnHaptic);
		btnHaptic.setVisibility(readOnlyVisability);
		btnHaptic.setOnClickListener(this);
		btnPower = (Button) findViewById(R.id.btnPower);
		btnPower.setVisibility(readOnlyVisability);
		btnPower.setOnClickListener(this);
		btnVersion = (Button) findViewById(R.id.btnVersion);
		btnVersion.setVisibility(readOnlyVisability);
		btnVersion.setOnClickListener(this);
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "CMBARCODE");

		

		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				//int value = msg.arg2;
				Log.i(LOG_TAG, "MainActivity::handleMessage recevied this msg " + msg.arg1 + " " + (String) msg.obj);
				switch (cmd) {
				case MESSAGE_BARCODE_STRING:
					Log.i(LOG_TAG, "handleMessage writing text ");
					String msgText = (String) msg.obj;
					String barcode = msgText;
					barcodeText.setText(barcode);
					if (msgText.contains("Trigger") == false) {
						setTriggerButtons(false);
					}
					
					wl.acquire();
					try {
					    Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					wl.release();

					break;
				case MESSAGE_QUERY_STRING:
					Log.i(LOG_TAG, "handleMessage writing query text ");
					String query = (String) msg.obj;
					barcodeText.setText(query);
					break;
				case MESSAGE_COMMAND_COMPLETE:
					String result = (String) msg.obj;
					resultText.setText(result);
					break;
				case MESSAGE_START_SCAN:
					setTriggerButtons(true);
					break;
				case MESSAGE_STOP_SCAN:
					setTriggerButtons(false);
					break;
				case MESSAGE_QUERY_ERROR:
					String queryErr = (String) msg.obj;
					barcodeText.setText(queryErr);
				default:
						super.handleMessage(msg);
				}
			}
		};		
		
		progress = new ProgressDialog(this);
				
		setDevice(getString(R.string.JE227));
	}
	
	private void selectFormat() {
		Intent i = new Intent(this, FormatSettings.class);
		startActivity(i);		
	}

	private void selectBarcode() {
		Intent i = new Intent(this, BarcodeActivity.class);
		startActivity(i);		
	}

	private void selectDevice() {
		Intent i = new Intent(this, DeviceSelection.class);
		startActivityForResult(i, ACTIVITY_SELECT_DEVICE);
	}
	
	private void selectLighting() {
		Intent i = new Intent(this, LightingActivity.class);
		startActivity(i);
	}
	
	private void selectAiming() {
		Intent i = new Intent(this, AimingActivity.class);
		startActivity(i);
	}

	private void selectRaw() {
		Intent i = new Intent(this, RawActivity.class);
		startActivity(i);
	}

	private void selectAudio() {
		Intent i = new Intent(this, AudioActivity.class);
		startActivity(i);
	}

	private void selectHaptic() {
		Intent i = new Intent(this, HapticActivity.class);
		startActivity(i);
	}

	private void selectPower() {
		Intent i = new Intent(this, PowerActivity.class);
		startActivity(i);
	}

	private void selectVersion() {
		Intent i = new Intent(this, VersionActivity.class);
		startActivity(i);
	}
	
	private void setDevice(String device) {
        if (device.equals(currentDevice)) {
            return;
        }

        currentDevice = device;

		if (lb != null) lb.close();
//		if (device.equals(getString(R.string.EA30))) {
//			lb = new LibBarcode(LibBarcode.Devices.BARCODE_EA30);
//		} else
		if (device.equals(getString(R.string.EM3070))) {
			lb = new LibBarcode(this, LibBarcode.Devices.BARCODE_EM3070);
		} else
		if (device.equals(getString(R.string.JE222))) {
			lb = new LibBarcode(this, LibBarcode.Devices.BARCODE_JE222);
		} else
		if (device.equals(getString(R.string.JE227))) {
			lb = new LibBarcode(this, Devices.BARCODE_JE227);
		}
		if (device.equals(getString(R.string.JE227Serial))) {
			lb = new LibBarcode(this, Devices.BARCODE_JE227_SERIAL);
		}
		if (device.equals(getString(R.string.SE4750))) {
			lb = new LibBarcode(this, Devices.BARCODE_SE4750);
		}
		btnDevice.setText(device);
		
		bl = new BarcodeListener(this, uiUpdateHandler, progress);

		addListenerOnButton();

	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String action = currentDevice;
		if (requestCode == ACTIVITY_SELECT_DEVICE) {
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					action = extras.getString(DeviceSelection.ACTION_DEVICE);
				}
				setDevice(action);
			}
		}
	}
	
	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}
	
	public void addListenerOnButton() {
		barcodeText = (TextView) findViewById(R.id.text1);
		barcodeText.setMovementMethod(new ScrollingMovementMethod());
		resultText  = (TextView) findViewById(R.id.resultText);
				
		btnScanStart = (Button) findViewById(R.id.btnScanStart);
		btnScanStart.setVisibility(readOnlyVisability);
		btnScanStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener sending trigger");
				bl.sendUiUpdateMessage(MainActivity.MESSAGE_START_SCAN, null, 0);
				bl.sendUiUpdateMessage(MainActivity.MESSAGE_STOP_SCAN, null, 3000);
				switch(scanState) {
				case SCANSTATE_MANUAL:
					lb.triggerCapture(LibBarcode.TriggerCapture.START);
					break;
				case SCANSTATE_AUTO:
					lb.triggerCapture(LibBarcode.TriggerCapture.AUTOMATIC);
					break;
				case SCANSTATE_CONTINUOUS:
					lb.triggerCapture(LibBarcode.TriggerCapture.CONTINUOUS);
					break;
				}
			}

		});
		btnScanStop = (Button) findViewById(R.id.btnScanStop);
		btnScanStop.setVisibility(readOnlyVisability);
		btnScanStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(LOG_TAG, "setOnClickListener sending trigger");
				bl.sendUiUpdateMessage(MainActivity.MESSAGE_STOP_SCAN, null, 0);
				lb.triggerCapture(LibBarcode.TriggerCapture.STOP);
			}

		});
		
		radioGroup = (RadioGroup) findViewById(R.id.radioGroupScanType);
		radioGroup.setVisibility(readOnlyVisability);
		
		RadioButton btnradioButtonMainAuto = (RadioButton) findViewById(R.id.radioButtonMainAuto);
		btnradioButtonMainAuto.setEnabled(lb.isSupported(LibBarcode.TriggerCapture.AUTOMATIC));

		RadioButton btnradioButtonMainContinuous = (RadioButton) findViewById(R.id.radioButtonMainContinuous);
		btnradioButtonMainContinuous.setEnabled(lb.isSupported(LibBarcode.TriggerCapture.CONTINUOUS));

		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioButtonMainTrigger) {
					scanState = MainActivity.SCANSTATE_MANUAL;
					return;
				}
				if (checkedId == R.id.radioButtonMainAuto) {
					scanState = MainActivity.SCANSTATE_AUTO;
					return;
				}
				if (checkedId == R.id.radioButtonMainContinuous) {
					scanState = MainActivity.SCANSTATE_CONTINUOUS;
				}
			}		
		});
		radioGroup.check(R.id.radioButtonMainTrigger);
		

		setTriggerButtons(false);

	}
	
	

	@Override
	public void onClick(View v) {
	    switch (v.getId()) {

	    case R.id.btnDevice:
	    	selectDevice();
	        break;
	    case R.id.btnBarcode:
	    	selectBarcode();
	        break;
	    case R.id.btnFormat:
	    	selectFormat();
	        break;
	    case R.id.btnLighting:
	    	selectLighting();
	        break;
	    case R.id.btnReset:
	    	lb.sendCommand(LibBarcode.Command.DEFAULT_ALL_COMMANDS);
	        break;
	    case R.id.btnAiming:
	    	selectAiming();
	        break;
	    case R.id.btnRaw:
	    	selectRaw();
	        break;
	    case R.id.btnAudio:
	    	selectAudio();
	        break;
	    case R.id.btnHaptic:
	    	selectHaptic();
	        break;
	    case R.id.btnPower:
	    	selectPower();
	        break;
	    case R.id.btnVersion:
	    	selectVersion();
	        break;
	    default:
	        break;
	    }
	}
	
	private void setTriggerButtons(boolean scanActive) {
		if (scanActive) {
			Log.i(LOG_TAG, "setTriggerButtons active");
			btnScanStart.setVisibility(readOnlyVisability);
			btnScanStop.setVisibility(readOnlyVisability);
		} else {
			Log.i(LOG_TAG, "setTriggerButtons inactive");
			btnScanStart.setVisibility(readOnlyVisability);
			btnScanStop.setVisibility(readOnlyVisability);			
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		  
		Log.i(LOG_TAG, "MainActivity::onDestroy entry");
		uiUpdateHandler = null;
		lb.close();

        // Kill process to clean up Zebra usb interface.

        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
		
	}


    /* (non-Javadoc)
         * @see android.app.Activity#onStart()
         */
	@Override
	protected void onStart() {
		super.onStart();
		Log.i(LOG_TAG, "MainActivity::onStart entry");
        if (lb != null) {
            lb.probe();
            setProgrammingState(false);
        }
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		Log.i(LOG_TAG, "MainActivity::onResume  entry");
        if (lb != null) {
            bl = new BarcodeListener(this, uiUpdateHandler, progress);
            lb.resume();
        }

	}


/*
	@Override
	protected void onNewIntent(Intent intent) {
        Log.i(LOG_TAG, "MainActivity::onNewIntent  entry");
		super.onNewIntent(intent);
		if (lb != null) {
			bl = new BarcodeListener(this, uiUpdateHandler, progress);
			lb.intent(intent);
		}
        Log.i(LOG_TAG, "MainActivity::onNewIntent  finish");
        finish();
	}
*/


	/* (non-Javadoc)
 * @see android.app.Activity#onStart()
 */
	@Override
	protected void onPause() {
        Log.i(LOG_TAG, "MainActivity::onPause entry");
        if (lb != null) {
            lb.pause();
        }
        super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		if (readOnlyVisability == View.VISIBLE) {
			MenuInflater mInflater = getMenuInflater();
			mInflater.inflate(R.menu.main_actions, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = null;
		int retVal=0;
		String msg="";

		switch(item.getItemId()) {
		case R.id.menu_commandStress:
			i = new Intent(this, CommandStress.class);
			startActivity(i);
			break;
		case R.id.menu_commandSupported:
			i = new Intent(this, IsSupportedActivity.class);
			startActivity(i);			
			default:
				break;
			
		}
		if (i == null)
		{
			bl.sendUiUpdateMessage(MainActivity.MESSAGE_QUERY_ERROR, msg + " Unsupported query", 0);
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setProgrammingState(boolean state) {
		
		if (programmingState == state) {
			return;
		}
		
		if (state) {
			lb.setProgrammingMode(LibBarcode.ProgrammingMode.ENABLE);
			programmingState = true;
		} else {
			lb.setProgrammingMode(LibBarcode.ProgrammingMode.DISABLE);
			programmingState = false;
		}
	}

	private void printError(String function, String msg) {
		Log.e(LOG_TAG, "MainActivity::"+ function + ": Error" + msg);
	}

	private void printDebug(String function, String msg) {
		if (debug) {
			Log.d(LOG_TAG, "MainActivity::" + function + ": " + msg);
		}
	}
}
