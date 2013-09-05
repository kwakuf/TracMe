package com.capstone.TracMe;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.tracme.util.*;

/**
 * The WiFiScanReceiver class is a broadcast receiver for
 * the wifi adapter scanner for this program.
 * 
 * This class will receive the results of the completed wifi
 * scan and continue to analyze and organize the results.
 * 
 * @author Kwaku Farkye
 * @author Ken Ugo
 *
 */
public class WiFiScanReceiver extends BroadcastReceiver {
  private static final String TAG = "WiFiScanReceiver";
  
  /** 
   * The scan number we are currently on (access points will use this for
   *  filling out the rssi list
   */  
  private static int scanNumber = 0;
  
  /** The calling activity */
  private TracMe wifiDemo;
  
  /** Flag specifying if scans have been received */
  public boolean received = false;
  
  /**
   * Constructor for WiFiScanReceiver Object. Initializes class's scanNumber to 0.
   * 
   * @param wifiDemo The activity that initialized this Object
   */
  public WiFiScanReceiver(TracMe wifiDemo) {
    super();
    
    this.wifiDemo = wifiDemo;
    scanNumber = 0;
  }

  @Override
  public void onReceive(Context c, Intent intent) {
    if (!intent.getAction().equals("android.net.wifi.SCAN_RESULTS") || wifiDemo.valuesConfirmed == 0) {
      return;
    }
    if (wifiDemo.buttonPressed.equals("scan")) {
      Log.d(TAG, "onClick() wifi.startScan(): " + wifiDemo.buttonPressed);
      int count = 1;
      
      // Get the scan results from scanning the wifi adapter
      List<ScanResult> results = wifiDemo.wifi.getScanResults();
      
      // Make a new list 
      wifiDemo.newList();
      
      // Save the point to the raw file (for _tran.txt file)
      wifiDemo.getRawFile().save("L"+ wifiDemo.getPoint() + "\n{");
      
      // Run this loop for each access point in the result list
      for (ScanResult result : results)
      {
        
        // Check if the access point is already in the list
        if (wifiDemo.apTable.lookupAP(result, true, scanNumber) == 0)
        { //The access point was not found
        	
        	// Create a new access point and add it to the table
        	wifiDemo.ap = new AccessPoint(wifiDemo.getTotalScans());    
        	wifiDemo.ap.setSSID(result.SSID);
        	wifiDemo.ap.setBSSID(result.BSSID.toUpperCase());
        	wifiDemo.ap.setRSSI(scanNumber, result.level + 100); // Make value positive
        	wifiDemo.ap.setChannel(Integer.valueOf(result.frequency).toString());
        	wifiDemo.apList.add(wifiDemo.ap);
        	wifiDemo.apTable.addAPToTable(wifiDemo.ap);
        }
        
        //Save to the raw output file (_tran.txt file)
        wifiDemo.getRawFile().save(result.BSSID + ":" + result.level);
        
        if (count < results.size())
        	wifiDemo.getRawFile().save(",");
        else
        	wifiDemo.getRawFile().save("}\n");
        
        count++;
      }
  
      wifiDemo.index++;
      
      scanNumber++; //Increment the scan number
      
      // Re-scan
      wifiDemo.scan(wifiDemo.index);
    }
  }
  
}
