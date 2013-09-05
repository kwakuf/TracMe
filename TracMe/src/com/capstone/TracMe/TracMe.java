package com.capstone.TracMe;

import java.util.ArrayList;

import com.actionbarsherlock.view.MenuItem;
import com.capstone.TracMe.R;
import com.slidingmenu.lib.app.SlidingActivity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
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

/**
 * Class that represents an android Activity. This activity is the main activity for
 * the TracMe Sampling app. This Activity handles inputting sample program information,
 * running scans, and saving output to file
 * 
 * @author Kwaku Farkye
 * @author Ken Ugo
 *
 */
public class TracMe extends SlidingActivity implements OnClickListener {
	private static final String TAG = "WiFiDemo";

	/** Wifi scan manager used for scanning phone for Access points */
	WifiManager wifi;
	
	/** Broadcast receiver for Wifi Scanner */
	BroadcastReceiver receiver2;
	
	/** Wifi receiver for Wifi Scan Service */
	WiFiScanReceiver receiver;
	
	/** Access point object that saves each access point's rssis received */
	AccessPoint ap;
	
	/** Text view representing the program console. */
	TextView textStatus;
	
	/** Button used for starting a scan */
	Button buttonScan;
	
	/** Button used for setting input data */
	Button buttonSet;
	
	/** Button used for displaying GPS information */
	Button buttonGps;
	
	/** Button used for saving results */
	Button buttonSave;

	/** Access point table that holds all access points received from this run */
	APTable apTable;
	
	/** Instance of SampleProgram Object for saving results */
	SampleProgram prog = new SampleProgram();
	
	/** AlertDialog for keeping track of scans */
	AlertDialog dialog;
	
	/** GPS Tracker for getting GPS information */
	GPSTracker gps;

	/** Name of the output file to write data to */
	String outputfile;
	
	/** Name of access point table file to write access point data to */
	String aptablefile;
	
	/** String for checking if button was pressed */
	String buttonPressed = "none";
	
	/** Number of samples per point */
	int numSamples = 0;
	
	/** Direction facing while sampling (North, South, East, West, Northeast, etc..)*/
	String direction;
	
	/** Number of points to be sampled in x direction */
	int maxX;
	
	/** Max number of points in y direction */
	int maxY;
	
	/** Total number of sample points to be done for this run of the program */
	int totalSamplePoints;
	
	/** The total number of scans to be done in this instance of the application */
	private static int totalScans;
	 
    /** Point that will be sampled */
    private int point;
    
    /** The raw file itself */
    private AndroidLog rawFile;

    /** Index of current scan */
	int index = 0;
	
	/** Flag to see whether input values have been confirmed */
	int valuesConfirmed = 0;
	
	/** The list of AP data for the current scan. */
	protected ArrayList<AccessPoint> apList;

	@Override
	/**
	 * Called when the activity is first created
	 * 
	 * @param savedInstanceState Bundled information that was saved on the previous run.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Setup Action Bar Sherlock and Sliding Menu
		setBehindContentView(R.layout.activity_menu);

		// Setup the sliding menu
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
				
				// Read what point we are sampling
				String inStr = ((EditText) findViewById(R.id.editPoint))
						.getText().toString();
				
				if (inStr.length() <= 0)
				{
					alertError("Please enter a name for this point");
				}
				try {
					int point = Integer.parseInt(inStr);
					if (point < 0)
					{
						alertError("Invalid point");
						return;
					}
					setPoint(point);
				} catch (NumberFormatException ex)
				{
					alertError("Please enter a valid input for the point. It must be an integer");
					return;
				}
				
				writeToConsole("Point being sampled is: " + point);
				
				//Run the scan
				scan(0);
			}
		} else if (view.getId() == R.id.buttonSet) {
			// This is the submit button for all the unchangeable variables

			if (valuesConfirmed == 1) {
				Toast.makeText(this, "These values have already been set",
						Toast.LENGTH_LONG).show();
				return;
			}

			// Get the name of the output file we will write to
			String inputStr = ((EditText) findViewById(R.id.editTextOutFile))
					.getText().toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter the output file.");
				return;
			} else {
				outputfile = new String(inputStr);
			}

			// Get the access point table file we will use for this run
			inputStr = ((EditText) findViewById(R.id.editTextAPTFile))
					.getText().toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter the APtable file.");
				return;
			} else {
				aptablefile = new String(inputStr);
			}

			// Get the direction
			inputStr = ((Spinner) findViewById(R.id.spinner1))
					.getSelectedItem().toString();
			direction = new String(inputStr);

			// Get the number of points to be sampled
			inputStr = ((EditText) findViewById(R.id.editNumPoints)).getText()
					.toString();
			if (inputStr.length() <= 0) {
				alertError("Please enter a valid number of points");
				return;
			}
			try {
				int maxPoints = Integer.parseInt(inputStr);
				prog.setNumPoints(maxPoints);
			} catch (NumberFormatException e) {
				alertError("Please enter a valid number of points");
				return;
			}
			
			totalSamplePoints = prog.getNumPoints();

			// Get the number of samples per point
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
										int which)
								{	
									// Run our finish and save method to product output file
									// NOTE: THIS MAY TAKE SOME TIME
									// TODO: Make this a separate thread from the main thread
									prog.finishAndSave("Campus Center", "Sample Output File"
											,apTable);
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
	 * Register a new receiver for scanning wifi adapter.
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
		((EditText) findViewById(R.id.editNumPoints)).setFocusable(false);
		//((EditText) findViewById(R.id.editTextYaxis)).setFocusable(false);
		((EditText) findViewById(R.id.editTextNumSamples)).setFocusable(false);

		// Load the APTable
		apTable = new APTable(aptablefile);
		apTable.loadTable();
		
		
		prog.setSampleFileName(outputfile);
		setRawFile(outputfile);
		prog.setDirection(direction);
		prog.setAPFileName(aptablefile);
		prog.setNumSamples(numSamples);
		
		writeToConsole("Output file set to: " + outputfile + ".txt");
		writeToConsole("AP Table file set to: " + aptablefile + ".txt");
		writeToConsole("Sampling direction set to: " + direction);
		writeToConsole("Number of points set to: " + prog.getNumPoints());
		writeToConsole("Number of samples per cell: " + numSamples);
		valuesConfirmed = 1;
		
		//Set the total number of scans to be done this run of the application
		totalScans = prog.getNumPoints() * prog.getNumSamples();
		//Set the total number of scans for each access point in the array list
		apTable.setTotalScans(totalScans);
		
	}

	/**
	 * Method to alert the user of an error while running the program.
	 * 
	 * @param msg Message to alert the user
	 */
	public void alertError(String msg) {
		dialog = new AlertDialog.Builder(this).setTitle("Error")
				.setMessage(msg).setNeutralButton("Close", null).show();
	}

	/**
	 * Main driver for scanning and receiving results from wifi adapter. 
	 * 
	 * @param ndx Scan number we will start on. 
	 * This is necessary because this method is recursive.
	 * 
	 */
	public void scan(int ndx) {
		if (ndx == 0) {
			index = 0; // Reset index

			prog.addPoint(point);
			
			dialog = new AlertDialog.Builder(this).setTitle("TracMe")
					.setMessage("Scanning").show();
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
					+ prog.getPointsSampled());
			writeToConsole("Number of samples needed: " + (totalSamplePoints));
			buttonPressed = "none";
			// Check if correct amount of samples have been done
			if (prog.getPointsSampled() >= totalSamplePoints) {
				buttonSave.setEnabled(true);
				// Disable the scan button
				buttonScan.setEnabled(false);
			}
		}
	}

    /**
     * Set the name of the raw ouptut file
     * 
     * @param val
     * 	Value to set the file name to ("_tran" will be appended to the end)
     */
    public void setRawFile( String val )
    {
    	String rawFileName;
    	rawFileName = val + "_tran";
    	rawFile = new AndroidLog(rawFileName + ".txt");
    }
    
    /**
     * Get the name of the raw output file
     * 
     */
    public AndroidLog getRawFile()
    {
    	return rawFile;
    }
	
    /**
     * Set the point that will be sampled next
     * 
     * @param point
     * 	The name of the point we will be sampling next
     */
    public void setPoint(int point)
    {
    	this.point = point;
    }
    
    /**
     * Get the point that will be sampled next
     * 
     * @return
     * 	The name of the point that is to be sampled next
     */
    public int getPoint()
    {
    	return this.point;
    }
    
    /**
     * Writes the message to the app's console
     * 
     * @param msg Message to write to console
     */
	public void writeToConsole(String msg) {
		// Add a time stamp
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		textStatus.append("\n(" + today.format("%k:%M") + "): " + msg);
	}

	/**
	 * Method that re-initializes the Access Point List to a new ArrayList
	 * 
	 */
	public void newList() {
		apList = new ArrayList<AccessPoint>();
	}

	/**
	 * Getter for the access point list
	 * 
	 * @return An ArrayList of access points
	 */
	public ArrayList<AccessPoint> getApList() {
		return apList;
	}
	
	/**
	 * Getter for the total number of scans for this run of the program
	 * 
	 * @return Total number of scans for this run of the program
	 */
	public int getTotalScans()
	{
		return totalScans;
	}
}