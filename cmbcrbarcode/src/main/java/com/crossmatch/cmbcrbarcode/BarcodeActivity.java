/*
 * Copyright (c) 2019. Crossmatch. All rights reserved
 *
 */

package com.crossmatch.cmbcrbarcode;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.crossmatch.libbarcode.LibBarcode;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BarcodeActivity extends Activity {
	private static final String LOG_TAG = "CmBarcodeSample";
    private static final boolean debug = false;
	
	private LibBarcode lb = null;
	private ProgressDialog progress;
	@SuppressWarnings("unused")
	private BarcodeListener bl = null;
	private Handler uiUpdateHandler;
	private Button btnEnableAll;
	private Button btnDisableAll;

	private ViewGroup vgParent;
	private View view;
	
	private Map<String, BarcodeView> barcodeView = new HashMap<String, BarcodeView>();

    /**
     * Functions to implement barcode_inflator.xml
     */

    private class BarcodeView implements CompoundButton.OnCheckedChangeListener {
		private String state;
		private Context context;
		private Switch togBtnEnable;
		private Button btnSetLength;
		private TextView txtName;
		private TextView txtState;
		private TextView txtMinMaxTitle;
		private TextView txtCheckDigitTitle;
		private EditText editMin;
		private EditText editMax;
		private RadioGroup radioGroupCheckDigit;
		private String min = "-1";
		private String max = "-1";
		private LibBarcode.EncodingType eType;
		private boolean checkDigitRead = false;

		BarcodeView(Context c, LibBarcode.EncodingType e) {
			context = c;
			state = "Unsuported";
			eType = e;
            boolean debug = false;
			
			view = LayoutInflater.from(context).inflate(R.layout.barcode_inflator, null);
			vgParent.addView(view);
			
			txtName = view.findViewById(R.id.txtName);
			txtName.setText(e.toString());
						

			
			barcodeStateSetup();
			lengthSetup();
			checkDigitSetup();
			
		}

        /**
         * inflator state setup
         * NOTE: must reference view
         */
		private void barcodeStateSetup() {
			txtState = view.findViewById(R.id.txtState);
			txtState.setText(state);

			togBtnEnable = view.findViewById(R.id.togBtnEnable);
			togBtnEnable.setVisibility(View.GONE);
            togBtnEnable.setEnabled(false);
            togBtnEnable.setChecked(false);
            togBtnEnable.setSelected(false);
            togBtnEnable.setOnCheckedChangeListener(this);
		}
		
		private void lengthSetup() {
			txtMinMaxTitle = (TextView) view.findViewById(R.id.txtMinMaxTitle);
			txtMinMaxTitle.setVisibility(View.GONE);
			
			btnSetLength = (Button) view.findViewById(R.id.btnSetLength);
			btnSetLength.setVisibility(View.GONE);
			btnSetLength.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    printDebug(LOG_TAG, "BarcodeView::onClick btnEnable");
					lb.setBarcodeOption(eType, LibBarcode.EncodingOption.MIN_LENGTH, min, LibBarcode.Format.DECIMAL_NUMBER);
					waitSpinner();
					lb.setBarcodeOption(eType, LibBarcode.EncodingOption.MAX_LENGTH, max, LibBarcode.Format.DECIMAL_NUMBER);
					waitSpinner();
				}
			});
			
			editMin = (EditText) view.findViewById(R.id.editTextMin);
			editMin.setVisibility(View.GONE);
			editMin.setText(min);
			editMin.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
					min = s.toString();
				}

				@Override
				public void afterTextChanged(Editable s) {
					min = s.toString();
					
				}
			});
			
			
			editMax = (EditText) view.findViewById(R.id.editTextMax);
			editMax.setVisibility(View.GONE);
			editMax.setText(max);
			editMax.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
					max = s.toString();
				}

				@Override
				public void afterTextChanged(Editable s) {
					max = s.toString();
					
				}
			});			
		}
		
		private void  checkDigitSetup() {
			
			txtCheckDigitTitle = (TextView) view.findViewById(R.id.txtcheckdigittitle);
			txtCheckDigitTitle.setVisibility(View.GONE);
			
			radioGroupCheckDigit = (RadioGroup) view.findViewById(R.id.radioGroupCheckDigit);
			radioGroupCheckDigit.setVisibility(View.GONE);
			
			for(int i = 0; i < radioGroupCheckDigit.getChildCount(); i++){
			    ((RadioButton)radioGroupCheckDigit.getChildAt(i)).setEnabled(false);
			}
			radioGroupCheckDigit.check(R.id.radioButtonCheckDigitDisable);
		
			radioGroupCheckDigit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {
					
					// wait for queries to complete
					if (!checkDigitRead) return;

                    printDebug("setOnCheckedChangeListener", Integer.toString(checkedId));
					if (checkedId == R.id.radioButtonCheckDigitDisable) {
						lb.setBarcodeOption(eType, LibBarcode.EncodingOption.NO_CHECK_DIGIT);
						waitSpinner();
					}
					if (checkedId == R.id.radioButtonCheckDigitValidate) {
						lb.setBarcodeOption(eType, LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_DO_NOT_TRANSMIT);
						waitSpinner();
					}
					if (checkedId == R.id.radioButtonCheckDigitValidateTransmit) {
						lb.setBarcodeOption(eType, LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_TRANSMIT);
						waitSpinner();
					}
				}
			});
			
		}
		
		
		public void barcodeCheckDigit(String s) {

            printDebug("barcodeCheckDigit","entry");

			txtCheckDigitTitle.setVisibility(View.VISIBLE);
			radioGroupCheckDigit.setVisibility(View.VISIBLE);
			
			for(int i = 0; i < radioGroupCheckDigit.getChildCount(); i++){
				((RadioButton)radioGroupCheckDigit.getChildAt(i)).setEnabled(true);
				((RadioButton)radioGroupCheckDigit.getChildAt(i)).setVisibility(View.VISIBLE);
			}
            printDebug("barcodeCheckDigit", "set radiogroup " + s );
		
			if (s.contains(LibBarcode.EncodingOption.NO_CHECK_DIGIT.toString())) {
				radioGroupCheckDigit.check(R.id.radioButtonCheckDigitDisable);
			}

			if (s.contains(LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_DO_NOT_TRANSMIT.toString())) {
				radioGroupCheckDigit.check(R.id.radioButtonCheckDigitValidate);
			}
			if (s.contains(LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_TRANSMIT.toString())) {
				radioGroupCheckDigit.check(R.id.radioButtonCheckDigitValidateTransmit);
			}
			checkDigitRead = true;

		}

        /**
         * Sets state of barcode in single view.
         * @param s
         */
		public void barcodeState (String s) {
            togBtnEnable.setOnCheckedChangeListener(null);
			if (s.contains(LibBarcode.QueryKey.ENABLE.toString())) {
				state = s;
				txtState.setText(R.string.supported);
				togBtnEnable.setChecked(true);
                togBtnEnable.setEnabled(true);
                togBtnEnable.setSelected(true);
                togBtnEnable.setVisibility(View.VISIBLE);
                togBtnEnable.setOnCheckedChangeListener(this);
                return;
			}
			if (s.contains(LibBarcode.QueryKey.DISABLE.toString())) {
				state = s;
				txtState.setText(R.string.supported);
				togBtnEnable.setChecked(false);
                togBtnEnable.setEnabled(true);
                togBtnEnable.setSelected(true);
                togBtnEnable.setVisibility(View.VISIBLE);
                togBtnEnable.setOnCheckedChangeListener(this);
                return;
			}
			printError("View.barcodeState", "Unexpected state " + s);
		}

		public void barcodeLength (String minimum, String maximum) {
            printDebug("barcodeLength", minimum + " " + maximum + " " + eType.toString());
			min = minimum;
			max = maximum;
			editMin.setText(min);
			editMax.setText(max);
			if ( (min.contains("-1")) || (max.contains("-1"))) {
				editMin.setVisibility(View.INVISIBLE);
				editMax.setVisibility(View.INVISIBLE);
				btnSetLength.setVisibility(View.INVISIBLE);
				txtMinMaxTitle.setVisibility(View.INVISIBLE);
			} else  {
				editMin.setVisibility(View.VISIBLE);
				editMax.setVisibility(View.VISIBLE);
				btnSetLength.setVisibility(View.VISIBLE);
				txtMinMaxTitle.setVisibility(View.VISIBLE);
			}
		}

		void printError(String function, String msg) {
            Log.e(LOG_TAG, "BarcodeActivity:BarcodeView::" + function + "" + msg);
            Log.i(LOG_TAG, "BarcodeActivity:BarcodeView::" + function + "    state   : " + state);
            Log.i(LOG_TAG, "BarcodeActivity:BarcodeView::" + function + "    enabled : " + togBtnEnable.isChecked());

        }

        void printDebug(String function, String msg) {
            if (debug) {
                Log.d(LOG_TAG, "BarcodeActivity:BarcodeView::" + function + "" + msg);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                lb.barcodeEnable(eType);
                state = LibBarcode.QueryKey.ENABLE.toString();
            } else {
                lb.barcodeDisable(eType);
                state = LibBarcode.QueryKey.DISABLE.toString();
            }
            waitSpinner();

        }
    }

	//-------------------------------------------------------------------------------------------------------------------
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.barcode_all);
		vgParent = findViewById(R.id.layoutBarcode);


        printDebug("oncreate","entry");
				
		lb = MainActivity.lb;
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_CODE128.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_CODE128));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_UCC_EAN_128.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_UCC_EAN_128));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_UPC_E.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_UPC_E));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_UPC_A.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_UPC_A));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_EAN_8.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_EAN_8));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_EAN_13.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_EAN_13));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_INTERLEAVED_2_OF_5.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_INTERLEAVED_2_OF_5));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_ITF14.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_ITF14));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_ITF6.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_ITF6));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_MATRIX25.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_MATRIX25));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_CODE39.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_CODE39));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_FULLASCII.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_FULLASCII));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_CODABAR.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_CODABAR));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_CODE93.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_CODE93));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_GS1_DATABAR.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_GS1_DATABAR));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_EAN_UCC_COMPOSITE.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_EAN_UCC_COMPOSITE));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_CODE11.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_CODE11));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_ISBN.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_ISBN));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_INDUSTRIAL25.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_INDUSTRIAL25));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_STANDARD25.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_STANDARD25));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_PLESSY.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_PLESSY));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_1D_MSI_PLESSY.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_1D_MSI_PLESSY));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_PDF417.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_PDF417));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_QR.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_QR));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_AZTEC.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_AZTEC));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_DATA_MATRIX.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_DATA_MATRIX));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_MAXICODE.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_MAXICODE));
		barcodeView.put(LibBarcode.EncodingType.BARCODE_2D_CHINESE.toString()    , new BarcodeView(this, LibBarcode.EncodingType.BARCODE_2D_CHINESE));


		btnEnableAll = findViewById(R.id.btnEnableAll);
		btnEnableAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                printDebug("onClick","btnEnableAll");
				lb.sendCommand(LibBarcode.Command.ENABLE_ALL_BARCODES);
				waitSpinner();
				lb.sendQuery(LibBarcode.Query.BARCODE_ENABLED);
				waitSpinner();
			}
		});

		btnDisableAll = findViewById(R.id.btnDisableAll);
		btnDisableAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                printDebug(LOG_TAG, "BarcodeView::onClick btnEnableAll");
				lb.sendCommand(LibBarcode.Command.DISABLE_ALL_BARCODES);
				waitSpinner();
				lb.sendQuery(LibBarcode.Query.BARCODE_ENABLED);
				waitSpinner();
			}
		});


		uiUpdateHandler = new Handler() {
			public void handleMessage(Message msg) {
				int cmd = msg.arg1;
				switch (cmd) {
				case MainActivity.MESSAGE_BARCODE_STRING:
                    printDebug("handleMessage", "writing text ");
					break;
				case MainActivity.MESSAGE_QUERY_STRING:
					String query = (String) msg.obj;
                    printDebug("BarcodeActivity", "MESSAGE_QUERY_STRING [" + query + "]");
					
					if (query.contains(LibBarcode.Query.BARCODE_ENABLED.toString())) {
                        printDebug("BarcodeActivity","QUERY_BARCODES_ENABLED ");
						
						Set<String> keys = barcodeView.keySet();
						for (String key : keys) {
							
							
							if (query.contains(key + " " + LibBarcode.QueryKey.ENABLE.toString())) {
                                printDebug("BarcodeActivity", key + " " + LibBarcode.QueryKey.ENABLE.toString());
								BarcodeView v = barcodeView.get(key);
								v.barcodeState(LibBarcode.QueryKey.ENABLE.toString());
							}
							if (query.contains(key + " " + LibBarcode.QueryKey.DISABLE.toString())) {
                                printDebug("BarcodeActivity", key + " " + LibBarcode.QueryKey.DISABLE.toString());
								BarcodeView v = barcodeView.get(key);
								v.barcodeState(LibBarcode.QueryKey.DISABLE.toString());
							}	
						}

					}

					if (query.contains(LibBarcode.Query.MAX_MIN_LENGTH.toString())) {
                        printDebug("BarcodeActivity", "QUERY_MAX_MIN_LENGTH ");
						
						Set<String> keys = barcodeView.keySet();
                        printDebug("BarcodeActivity", "keys " + keys);
						for (String key : keys) {
							if (query.contains(key)) {
								int index = query.indexOf(key);
								index += key.length() + 1;
								String sub = query.substring(index);
								String delim = " ";
								String[] tok = sub.split(delim);
								if (tok.length >= 2) {
                                    printDebug("BarcodeActivity", key + " " + tok[0] + " " + tok[1]);
									BarcodeView v = barcodeView.get(key);
                                    printDebug("BarcodeActivity", v.toString());
									v.barcodeLength(tok[0], tok[1]);
								}
							}
						}								
					}

					if (query.contains(LibBarcode.Query.BARCODE_CHECKDIGIT.toString())) {
                        printDebug("BarcodeActivity", "QUERY_BARCODES_CHECKDIGIT ");
						
						Set<String> keys = barcodeView.keySet();
						for (String key : keys) {
							
							if (query.contains(key + " " + LibBarcode.EncodingOption.NO_CHECK_DIGIT.toString())) {
                                printDebug("BarcodeActivity", key + " " + LibBarcode.EncodingOption.NO_CHECK_DIGIT.toString());
								BarcodeView v = barcodeView.get(key);
								v.barcodeCheckDigit(LibBarcode.EncodingOption.NO_CHECK_DIGIT.toString());
							}
							if (query.contains(key + " " + LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_DO_NOT_TRANSMIT.toString())) {
                                printDebug("BarcodeActivity", key + " " + LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_DO_NOT_TRANSMIT.toString());
								BarcodeView v = barcodeView.get(key);
								v.barcodeCheckDigit(LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_DO_NOT_TRANSMIT.toString());
							}
							if (query.contains(key + " " + LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_TRANSMIT.toString())) {
                                printDebug("BarcodeActivity", key + " " + LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_TRANSMIT.toString());
								BarcodeView v = barcodeView.get(key);
								v.barcodeCheckDigit(LibBarcode.EncodingOption.CHECK_DIGIT_VALIDATE_TRANSMIT.toString());
							}
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

		lb.sendQuery(LibBarcode.Query.BARCODE_ENABLED);
		waitSpinner();
		lb.sendQuery(LibBarcode.Query.MAX_MIN_LENGTH);
		waitSpinner();
		
		lb.sendQuery(LibBarcode.Query.BARCODE_CHECKDIGIT);
		waitSpinner();
		
	}
	

	
	public void waitSpinner() {
		progress.setTitle("Sending command");
		progress.setMessage("Waiting for responce");
		progress.show();
	}


    void printError(String function, String msg) {
        Log.e(LOG_TAG, "BarcodeActivity::" + function + "" + msg);
        Log.i(LOG_TAG, "BarcodeActivity::" + function + "    barcodeView size : " + barcodeView.size());

    }

    void printDebug(String function, String msg) {
        if (debug) {
            Log.d(LOG_TAG, "BarcodeActivity::" + function + "" + msg);
        }
    }


}
