package com.capstone.TracMe;

import java.util.Comparator;

/**
 * Represents various information that can be obtained from a wireless access
 * point. The most relevant information for this project is the ID, BSSID, and
 * RSSI data. Please consult the source code for more information about what
 * each accessor/mutator methods represents.
 * 
 * @author James Humphrey
 * @author Kwaku Farkye
 */
public class AccessPoint
{

	public AccessPoint()
	{
		this.totalSamples = 0;
	}
	
	public AccessPoint(int totalSamples)
	{
		this.totalSamples = totalSamples;
		initRSSIArray();
	}
	
   /**
    * Mutator for the AP ID value assigned by the custom AP table.
    * 
    * @param id
    *           The new ID value to set
    */
   public void setID( int id )
   {
      this.id = id;
   }

   /**
    * Accessor for the AP assigned ID value.
    * 
    * @return The ID value for this access point.
    */
   public int getID()
   {
      return id;
   }

   /**
    * Mutator for the AP SSID value received from the broadcast.
    * 
    * @param ssid
    *           The received SSID value to set
    */
   public void setSSID( String ssid )
   {
      this.ssid = ssid;
   }

   /**
    * Accessor for the AP assigned SSID.
    * 
    * @return
    */
   public String getSSID()
   {
      return ssid;
   }

   /**
    * Mutator for AP BSSID value received from the broadcast.
    * 
    * @param bssid
    *           The received BSSID value to set
    */
   public void setBSSID( String bssid )
   {
      this.bssid = bssid;
   }

   /**
    * Accessor for the AP assigned BSSID value.
    * 
    * @return The BSSID value for this access point
    */
   public String getBSSID()
   {
      return bssid;
   }
   
   /**
    * Sets the total amount of samples this access point will hold
    * 
    * @param totalSamples
    * 	The total number of samples this AP holds
    */
   public final void setTotalSamples(int totalSamples)
   {
	   this.totalSamples = totalSamples;
	   initRSSIArray();
   }

   private void initRSSIArray()
   {
	   this.rssiList = new int[totalSamples];
	   //Initialize the list to zeros
	   for (int i = 0; i < totalSamples; i++)
	   {
		   rssiList[i] = 0;
	   }
   }
   
   /**
    * Mutator for AP RSSI value received from the broadcast.
    * 
    * @param rssi
    *           The received RSSI value to set
    */
   public void setRSSI( int rssi )
   {
      this.rssi = rssi;
   }

   /**
    * Set the RSSI value of this access point at the current index
    * 
    * @param index
    * 	The sample number (so we know where to put the rssi value in the list)
    * 
    * @param rssi
    * 	The RSSI value to set
    */
   public void setRSSI(int index, int rssi)
   {
	   this.rssiList[index] = rssi;
   }
   
   /**
    * Accessor for the AP assigned RSSI value.
    * 
    * @return The most recent RSSI value for this access point
    */
   public int getRSSI()
   {
      return rssi;
   }
   
   /**
    * Get the RSSI value at the specified index
    * 
    * @param index
    * 	The index (aka sample number) that we want the RSSI value for
    * 
    * @return
    * 	The RSSI value of the sample number
    */
   public int getRSSI(int index)
   {
	   return rssiList[index];
   }
   
   /**
    * Mutator for AP channel received from the broadcast.
    * 
    * @param channel
    *           The received channel value to set
    */
   public void setChannel( String channel )
   {
      this.channel = channel;
   }

   /**
    * Accessor for the AP assigned channel value.
    * 
    * @return The channel value(s) for this access point
    */
   public String getChannel()
   {
      return channel;
   }

   /**
    * Mutator for AP HT received from the broadcast.
    * 
    * @param ht
    *           The received HT value to set
    */
   public void setHT( boolean ht )
   {
      this.ht = ht;
   }

   /**
    * Accessor for the AP assigned HT value.
    * 
    * @return The HT value for this access point
    */
   public boolean getHT()
   {
      return ht;
   }

   /**
    * Mutator for AP CC received from the broadcast.
    * 
    * @param cc
    *           The received CC value to set
    */
   public void setCC( String cc )
   {
      this.cc = cc;
   }

   /**
    * Accessor for the AP assigned CC value.
    * 
    * @return The CC value for this access point
    */
   public String getCC()
   {
      return cc;
   }

   /**
    * Mutator for AP security received from the broadcast.
    * 
    * @param security
    *           The received security value to set
    */
   public void setSecurity( String security )
   {
      this.security = security;
   }

   /**
    * Accessor for the AP assigned security value.
    * 
    * @return The security value for this access point
    */
   public String getSecurity()
   {
      return security;
   }

   /**
    * Outputs each data separated by spaces in one line.
    */
   public String toString()
   {
      return new String( id + " " + ssid + " " + bssid + " " + channel + " " + ht + " " + cc + " "
            + security + "\n" );
   }

   private int id = -1; // Unique id associated with each AP and can be looked up in table. (GETS Defaulted to 0 on mobile so I set this to -1 to make the code work)
   private String ssid; // SSID (Service Set Identifier) is a string name of the AP.
   private String bssid; // BSSID (Basic Service Set Identification) is an ideally unique number for each access point. We will use this value to identify the different APs.
   private int rssi; // RSSI (Received Signal Strength Indication) is the signal measurement of power in dB.
   private String channel; // The range of frequencies in use.
   private boolean ht; // Indicates if this AP supports a home theater receiver.
   private String cc; // CC (Country Code) can be used to identify the country the AP is located in.
   private String security; // Indicates the type of security used by the AP and its (auth/unicast/group).
   ///private coord loc; // Physical location of the AP.
   
   //This access point's rssi values for each scan. The length of this list
   //is the amount of coords in a sample set * number of samples per coordinate
   private int[] rssiList;
   
   private int totalSamples; //The total number of times this access point is sampled for
   
}

/**
 * Comparator class used for sorting access points by increasing ID value.
 * 
 * @author James Humphrey
 */
class AccessPointIDComparator implements Comparator< AccessPoint >
{
   /**
    * The main compare function which looks at the ID value of each AP and sorts
    * it in increasing order.
    */
   public int compare( AccessPoint ap1, AccessPoint ap2 )
   {
      // Convert the IDs of the access points to the Integer class so we can easily compare the values.
      Integer ap1ID = ap1.getID();
      Integer ap2ID = ap2.getID();

      // Uses the compareTo method of Integer class to compare the IDs of the AP.
      return ap1ID.compareTo( ap2ID );
   }
}
