package com.capstone.TracMe;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WiFiScanReceiver extends BroadcastReceiver {
  private static final String TAG = "WiFiScanReceiver";
  private static String EXTRA_INTEGER = "extra integer";
  
  //The scan number we are currently on (access points will use this for
  //filling out the rssi list
  protected static int scanNumber = 0;
  
  
  TracMe wifiDemo;
  String apns; 
  public boolean received = false;
  
  public WiFiScanReceiver(TracMe wifiDemo) {
    super();
    this.wifiDemo = wifiDemo;
    this.scanNumber = 0;
  }

  @Override
  public void onReceive(Context c, Intent intent) {
    if (!intent.getAction().equals("android.net.wifi.SCAN_RESULTS") || wifiDemo.valuesConfirmed == 0) {
      return;
    }
    if (wifiDemo.buttonPressed.equals("scan")) {
      Log.d(TAG, "onClick() wifi.startScan(): " + wifiDemo.buttonPressed);
      int count = 1;
      List<ScanResult> results = wifiDemo.wifi.getScanResults();
      apns = "Networks found:";
      wifiDemo.newList();
      wifiDemo.getRawFile().save("L"+ wifiDemo.getPoint() + "\n{");
      for (ScanResult result : results) {
        apns += "\n" + result.SSID;
        // TODO: Check if the access point is already in the list
        // TODO: If not, create a new access point and set this rssi to the scan
        // we are on
        
        if (!wifiDemo.apTable.lookupAP(result, true, scanNumber))
        { //The access point was not found
        	
        	//Create a new access point and add it to the table
        	wifiDemo.ap = new AccessPoint(wifiDemo.getTotalScans());    
        	wifiDemo.ap.setSSID(result.SSID);
        	wifiDemo.ap.setBSSID(result.BSSID.toUpperCase());
        	wifiDemo.ap.setRSSI(scanNumber, result.level + 100);
        	wifiDemo.ap.setChannel(Integer.valueOf(result.frequency).toString());
        	wifiDemo.apList.add(wifiDemo.ap);
        	wifiDemo.apTable.addAPToTable(wifiDemo.ap);
        }
        
        //Save to the raw output file
        wifiDemo.getRawFile().save(result.BSSID + ":" + result.level);
        if (count < results.size())
        	wifiDemo.getRawFile().save(",");
        else
        	wifiDemo.getRawFile().save("}\n");
        
        count++;
      }
      //wifiDemo.apTable.mapAPsToID(wifiDemo.apList, true);
  
      Sample newSample = new Sample();
      newSample.setScan(wifiDemo.apList);
      // Set the output of the WiFi scanner to the new sample for this
      // cell.
  
      // Add the newest sample to the latest cell.
      wifiDemo.prog.samples.get( wifiDemo.prog.samples.size() - 1 ).getSamples().add( newSample );
  
      wifiDemo.index++;
      
      scanNumber++; //Increment the scan number
      
      wifiDemo.scan(wifiDemo.index);
    }
  }
  
  public ArrayList< AccessPoint > getApList()  {
     return wifiDemo.apList;
  }

  public static int getScanNumber() {
	return scanNumber;
  }

  public static void setScanNumber(int scanNumber) {
	WiFiScanReceiver.scanNumber = scanNumber;
  }
  
}
