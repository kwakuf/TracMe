package com.capstone.TracMe;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import com.capstone.TracMe.R;
import com.slidingmenu.lib.app.SlidingActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TracMe extends SlidingActivity implements OnClickListener {
	private static final String TAG = "WiFiDemo";
	private static String SOMETHING_HAPPENED = "com.example.somethinghappened";
	private static String EXTRA_INTEGER = "extra integer";

	WifiManager wifi;
	BroadcastReceiver receiver2;
	WiFiScanReceiver receiver;
	AccessPoint ap;
	TextView textStatus;
	Button buttonScan;
	Button buttonSet;
	Button buttonGps;
	Button buttonSave;
	APTable apTable;
	SampleProgram prog = new SampleProgram();
	AlertDialog dialog;
	GPSTracker gps;

	String outputfile;
	String aptablefile;
	String buttonPressed = "none";
	int numSamples = 0;
	String direction;
	int maxX;
	int maxY;
	int totalSamples;

	int index = 0;
	int valuesConfirmed = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Setup Action Bar Sherlock and Sliding Menu
		setBehindContentView(R.layout.activity_menu);

		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadow);
		getSlidingMenu().setBehindOffsetRes(R.dimen.slidingmenu_offset);
		getSlidingMenu().setBehindScrollScale(0.25f);
		setSlidingActionBarEnabled(true);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.icon);

		// Setup UI
		textStatus = (TextView) findViewById(R.id.textStatus);
		buttonScan = (Button) findViewById(R.id.buttonScan);
		buttonScan.setOnClickListener(this);
		buttonSet = (Button) findViewById(R.id.buttonSet);
		buttonSet.setOnClickListener(this);
		buttonGps = (Button) findViewById(R.id.buttonGps);
		buttonGps.setOnClickListener(this);
		buttonSave = (Button) findViewById(R.id.buttonSave);
		buttonSave.setOnClickListener(this);
		buttonSave.setEnabled(false);

		// Setup WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Register Broadcast Receiver
		if (receiver == null)
			receiver = new WiFiScanReceiver(this);

		registerReceiver((BroadcastReceiver) receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		Log.d(TAG, "onCreate()");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			unregisterReceiver((BroadcastReceiver) receiver);
		} catch (IllegalArgumentException e) {
			System.out.println("register error - do nothing");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			unregisterReceiver((BroadcastReceiver) receiver);
		} catch (IllegalArgumentException e) {
			System.out.println("register error - do nothing");
		}
	}

	public void onClick(View view) {
		if (view.getId() == R.id.buttonScan) {
			if (valuesConfirmed == 0) {
				alertError("The scan settings have not been set. Press the logo in the top right and enter the settings.");
			} else {
				Log.d(TAG, "onClick() wifi.startScan()");

				buttonPressed = "scan";
				String inputStr = ((EditText) findViewById(R.id.editTextX))
						.getText().toString();
				if (inputStr.length() <= 0) {
					alertError("Please enter a valid input for the X axis");
					return;
				}
				try {
					int X = Integer.parseInt(inputStr);
					if (X <= 0 || X > prog.getGridSizeX()) {
						alertError("Invalid X coordinate");
						return;
					}
					prog.setGridX(X);
				} catch (NumberFormatException e) {
					alertError("Please enter a valid input for the X axis");
					return;
				}
				inputStr = ((EditText) findViewById(R.id.editTextY)).getText()
						.toString();
				if (inputStr.length() <= 0) {
					alertError("Please enter a valid input for the Y axis");
					return;
				}
				try {
					int Y = Integer.parseInt(inputStr);
					if (Y <= 0 || Y > prog.getGridSizeY()) {
						alertError("Invalid Y coordinate");
						return;
					}
					prog.setGridY(Y);
				} catch (NumberFormatException e) {
					alertError("Please enter a valid input for the Y axis");
					return;
				}
				if (checkCell()) {
					dialog = new AlertDialog.Builder(this)
							.setTitle("TracMe")
							.setMessage(
									"This cell has already been sampled. Do you want to Redo this cell sample?")
							.setPositiveButton("Redo",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											deleteCell();
											writeToConsole("Coordinate being sampled: "
													+ prog.getGridX()
													+ "x"
													+ prog.getGridY());
											scan(0);
											dialog.dismiss();
										}
									}).setNeutralButton("Cancel", null).show();
				} else {
					writeToConsole("Coordinate being sampled: "
							+ prog.getGridX() + "x" + prog.getGridY());
					scan(0);
					/* The run sample function can be called here */
				}
			}
		} else if (view.getId() == R.id.buttonSet) {
			// This is the submit button for all the unchangeable variables

			if (valuesConfirmed == 1) {
				Toast.makeText(this, "These values have already been set",
						Toast.LENGTH_LONG).show();
				return;
			}

			String inputStr = ((EditText) findViewById(R.id.editTextOutFile))
					.getText().toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter the output file.");
				return;
			} else {
				outputfile = new String(inputStr);
			}

			inputStr = ((EditText) findViewById(R.id.editTextAPTFile))
					.getText().toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter the APtable file.");
				return;
			} else {
				aptablefile = new String(inputStr);
			}

			inputStr = ((Spinner) findViewById(R.id.spinner1))
					.getSelectedItem().toString();
			direction = new String(inputStr);

			inputStr = ((EditText) findViewById(R.id.editTextXaxis)).getText()
					.toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter a valid input for the X axis");
				return;
			}
			try {
				int maxNumX = Integer.parseInt(inputStr);
				prog.setGridSizeX(maxNumX);
			} catch (NumberFormatException e) {
				alertError("Please enter a valid input for the X axis");
				return;
			}

			inputStr = ((EditText) findViewById(R.id.editTextYaxis)).getText()
					.toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter a valid input for the Y axis");
				return;
			}
			try {
				int maxNumY = Integer.parseInt(inputStr);
				prog.setGridSizeY(maxNumY);
				totalSamples = prog.getGridSizeX() * prog.getGridSizeY();
			} catch (NumberFormatException e) {
				alertError("Please enter a valid input for the Y axis");
				return;
			}

			inputStr = ((EditText) findViewById(R.id.editTextNumSamples))
					.getText().toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter a valid input for the number of samples");
				return;
			}
			try {
				numSamples = Integer.parseInt(inputStr);

			} catch (NumberFormatException e) {
				alertError("Please enter a valid input for the number of samples");
				return;
			}

			// First we should check for errors and empty fields, if all of that
			// is done, alert them to confirm values
			dialog = new AlertDialog.Builder(this)
					.setTitle("Confirm Values")
					.setMessage(
							"Are you sure you want to save these values? (They cannot be changed once set.)")
					.setPositiveButton("Confirm",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									confirmValues();
									dialog.dismiss();
								}
							}).setNeutralButton("Cancel", null).show();
		} else if (view.getId() == R.id.buttonSave) {
			dialog = new AlertDialog.Builder(this)
					.setTitle("Save Results")
					.setMessage(
							"Sampling finished. Do you want to save these results?")
					.setPositiveButton("Save",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									prog.finishSampling("", ""); // Finish
																	// sampling
																	// will be
																	// in the
																	// save
																	// results
																	// onclick
																	// event
									writeToConsole("Sample saved at "
											+ outputfile + ".txt");
									dialog.dismiss();
								}
							}).setNeutralButton("Cancel", null).show();
		} else if (view.getId() == R.id.buttonGps) {
			buttonPressed = "gps";
			gps = new GPSTracker(this);

			// check if GPS enabled
			if (gps.canGetLocation()) {

				double latitude = gps.getLatitude();
				double longitude = gps.getLongitude();

				String gpsString = "Your Location is - \nLat: " + latitude
						+ "\nLong: " + longitude;
				dialog = new AlertDialog.Builder(this).setTitle("GPS")
						.setMessage(gpsString).setNeutralButton("Close", null)
						.show();
			} else {
				// can't get location
				// GPS or Network is not enabled
				// Ask user to enable GPS/network in settings
				gps.showSettingsAlert();
			}

			gps.stopUsingGPS();
		}
	}

	/**
	 * Register a new receiver.
	 */
	public void registerMyReciever() {
		receiver = new WiFiScanReceiver(this);

		registerReceiver((BroadcastReceiver) receiver, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	/**
	 * Attempt to unregister the receiver.
	 */
	public void unregisterMyReceiver() {
		try {
			unregisterReceiver((BroadcastReceiver) receiver);
		} catch (IllegalArgumentException e) {
			System.out.println("register error - do nothing");
		}
	}

	/**
	 * This function makes these fields no longer changeable and sets confirm to
	 * true
	 */
	public void confirmValues() {
		((EditText) findViewById(R.id.editTextOutFile)).setFocusable(false);
		((EditText) findViewById(R.id.editTextAPTFile)).setFocusable(false);
		((Spinner) findViewById(R.id.spinner1)).setEnabled(false);
		((EditText) findViewById(R.id.editTextXaxis)).setFocusable(false);
		((EditText) findViewById(R.id.editTextYaxis)).setFocusable(false);
		((EditText) findViewById(R.id.editTextNumSamples)).setFocusable(false);

		// Load the APTable
		apTable = new APTable(aptablefile);
		apTable.loadTable();

		prog.setSampleFileName(outputfile);
		prog.setDirection(direction);
		prog.setAPFileName(aptablefile);

		writeToConsole("Output file set to: " + outputfile + ".txt");
		writeToConsole("AP Table file set to: " + aptablefile + ".txt");
		writeToConsole("Sampling direction set to: " + direction);
		writeToConsole("Grid dimensions set to: " + prog.getGridSizeX() + "x"
				+ prog.getGridSizeY());
		writeToConsole("Number of samples per cell: " + numSamples);
		valuesConfirmed = 1;
	}

	public void alertError(String msg) {
		dialog = new AlertDialog.Builder(this).setTitle("Error")
				.setMessage(msg).setNeutralButton("Close", null).show();
	}

	public boolean checkCell() {
		for (int cellCheck = 0; cellCheck < prog.getSamples().size(); cellCheck++) {
			if (prog.getGridX() == prog.getSamples().get(cellCheck).getLoc().x
					&& prog.getGridY() == prog.getSamples().get(cellCheck)
							.getLoc().y) {
				return true;
			}
		}
		return false;
	}

	public void deleteCell() {
		Point tstPoint = new Point(prog.getGridX(), prog.getGridSizeY());

		for (int i = 0; i < prog.samples.size(); i++) {
			if (prog.samples.get(i).getLoc().equals(tstPoint)) {
				prog.samples.remove(i);
			}
		}
	}

	/**
	 * This function is the main driver for the wifi scan process
	 */
	public ArrayList<AccessPoint> scan(int ndx) {
		if (ndx == 0) {
			index = 0; // Reset index

			// Create the sample cell
			prog.runCellSample();

			dialog = new AlertDialog.Builder(this).setTitle("TracMe")
					.setMessage("Scanning").show();
			// TODO add a progress bar to this alert
		}
		if (ndx != numSamples) { // This index will be the variable
			dialog.setMessage("Scanning: " + ndx + "/" + numSamples
					+ " Complete");
			wifi.startScan();
			index = ndx;
		} else { // This is after we have completed the scans
			dialog.setMessage("Scanning: " + ndx + "/" + numSamples
					+ " Complete");
			dialog.dismiss();
			writeToConsole("Number of samples done: "
					+ prog.getSamples().size());
			writeToConsole("Number of samples needed: " + (totalSamples));

			buttonPressed = "none";
			// Check if correct amount of samples have been done
			// if( prog.getSamples().size() == totalSamples )
			if (prog.getSamples().size() > 0) {
				buttonSave.setEnabled(true);
			}
		}
		return receiver.getApList();
	}

	public void writeToConsole(String msg) {
		// Add a timestaamp
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		textStatus.append("\n(" + today.format("%k:%M") + "): " + msg);
	}

	public void newList() {
		apList = new ArrayList<AccessPoint>();
	}

	public ArrayList<AccessPoint> getApList() {
		return apList;
	}

	/**
	 * The list of AP data for the current scan.
	 */
	protected ArrayList<AccessPoint> apList;
}