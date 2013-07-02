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
  
  TracMe wifiDemo;
  String apns; 
  public boolean received = false;
  
  public WiFiScanReceiver(TracMe wifiDemo) {
    super();
    this.wifiDemo = wifiDemo;
  }

  @Override
  public void onReceive(Context c, Intent intent) {
    if (!intent.getAction().equals("android.net.wifi.SCAN_RESULTS") || wifiDemo.valuesConfirmed == 0) {
      return;
    }
    if (wifiDemo.buttonPressed.equals("scan")) {
      Log.d(TAG, "onClick() wifi.startScan(): " + wifiDemo.buttonPressed);
      List<ScanResult> results = wifiDemo.wifi.getScanResults();

      apns = "Networks found:";
      wifiDemo.newList();
      for (ScanResult result : results) {
        apns += "\n" + result.SSID;
        
        wifiDemo.ap = new AccessPoint();
        wifiDemo.ap.setSSID(result.SSID);
        wifiDemo.ap.setBSSID(result.BSSID);
        wifiDemo.ap.setRSSI(result.level + 100);
        
        wifiDemo.apList.add(wifiDemo.ap);
      }
      wifiDemo.apTable.mapAPsToID(wifiDemo.apList, true);
  
      Sample newSample = new Sample();
      newSample.setScan(wifiDemo.apList);
      // Set the output of the WiFi scanner to the new sample for this
      // cell.
  
      // Add the newest sample to the latest cell.
      wifiDemo.prog.samples.get( wifiDemo.prog.samples.size() - 1 ).getSamples().add( newSample );
  
      wifiDemo.index++;
      wifiDemo.scan(wifiDemo.index);
    }
  }
  
  public ArrayList< AccessPoint > getApList()  {
     return wifiDemo.apList;
  }
}
