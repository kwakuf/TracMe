package com.capstone.TracMe;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

import android.net.wifi.ScanResult;

/**
 * Creates and loads an AP table for a specified region. Once loaded, the main
 * function of this class is to map given AP BSSID's into an ID given by the AP
 * table file. New AP's can be automatically added to the table which will
 * increment the ID by 1.
 * 
 * @author James Humphrey
 * @author Kwaku Farkye
 */
public class APTable
{
   /**
    * Creates and initializes an empty AP table.
    */
   public APTable( String tableName )
   {
      // Create and initialize the AP table to 0.
      aps = new ArrayList< AccessPoint >();
      aps.clear();

      tableLog = new AndroidLog(tableName + ".txt");
      tableLog_debug = new AndroidLog(tableName + "_debug.txt");
   }

   /**
    * Adds a new access point to the table.
    * 
    * @param newAp
    *           The new AP to be added to the table
    */
   public void addAPToTable( AccessPoint newAp )
   {
      // Get the ID number of the last entry in the table (should be the highest value).
      int topID = 0;
      ;

      // The first AP added should always begin with a value of 1.
      if( aps.size() > 0 )
      {
         topID = aps.get( aps.size() - 1 ).getID();
      }

      // Increment the next ID so it is unique to the table.
      newAp.setID( topID + 1 );

      // Add the new AP to the loaded table.
      aps.add( newAp );

      // Copy the new AP to the table file.
      tableLog.save( newAp.getID() + " " + newAp.getBSSID() + "\n" );
      
      tableLog_debug.save(newAp.toString());

   }

   /**
    * Search for the access point in the access point table. 
    * 
    * @param apData
    * 	The data for the access point that 
    * 	we are looking to store in the acess point table
    * 
    * @param addValue
    * 	Flag specifying whether the scan result should be added to this ap's rssi list
    * 
    * @param scanNumber
    * 	The scan number we are on
    * 
    * @return
    * 	True if the access point was found in the access point table.
    * 	False otherwise.
    */
   public boolean lookupAP(ScanResult apData, boolean addValue, int scanNumber)
   {
	   for (int i = 0; i < aps.size(); i++)
	   {
		   if (aps.get(i).getBSSID().equals(apData.BSSID.toUpperCase()))
		   {
			   if (addValue)
			   {
				   //Found the correct AP, now add this rssi to the aps rssi list
				   aps.get(i).setRSSI(scanNumber, apData.level + 100);
			   }
			   return true;
		   }
	   }
	   
	   //AP was not found in the list, so return false
	   return false;
   }
   
   /**
    * Iterates through each AP in the provided list and maps its BSSID to the
    * corresponding ID value in the AP table file.
    * 
    * @param apList
    *           The list of access points to map
    * @param autoIncludeNew
    *           Indicates if you want to automatically include each AP into the
    *           table file if no mapping is found (new AP)
    */
   public void mapAPsToID( ArrayList< AccessPoint > apList, boolean autoIncludeNew )
   {
     autoIncludeNew = true;
      for( int i = 0; i < apList.size(); i++ )
      {
         for( int j = 0; j < aps.size(); j++ )
         {
            if( apList.get( i ).getBSSID().equals( aps.get( j ).getBSSID() ) )
            {
               // Set the ID of the mapped AP in the list.
               apList.get( i ).setID( aps.get( j ).getID() );
               System.out.println( "Value mapped with id " + apList.get( i ).getID() + " and BSSID "
                     + apList.get( i ).getBSSID() + " and RSSI " + apList.get( i ).getRSSI() );
               break;
            }
         }
         System.out.println("id is " + apList.get( i ).getID());
         // Check if the AP was not found in the table.
         if( apList.get( i ).getID() < 0 )
         {
            System.out.println( "The AP was not found in the table" );

            // Check if the AP should be added to the table.
            if( autoIncludeNew )
            {
               addAPToTable( apList.get( i ) );
            }
            else
            {
               // Prompt the user if he wants the program to save the access point to the table.///
               addAPToTable( apList.get( i ) );
            }
         }
      }
   }

   /**
    * Loads each registered access point from the AP table into memory. This
    * function assumes the data is in the correct format as it does no error
    * checking on format.
    * 
    * @param tableName
    *           The file name of the AP table to load from
    */
   public void loadTable ()
   {
      String data;
      
      data = tableLog.load();

      // Create a scanner on the input stream so we can parse the file data.
      Scanner tabScan = new Scanner( data );

      // Clear the previously allocated AP table.
      aps.clear();
      
      // Loop through each line in the file which contains AP mapping data.
      while( tabScan.hasNext() )
      {
         // Create a new access point that we will be adding to our table.
         AccessPoint newAp = new AccessPoint();

         // Read the id number used for mapping APs.
         newAp.setID( tabScan.nextInt() );

         // Read the unique BSSID of the AP that will be used to map to an id value.
         newAp.setBSSID( tabScan.next() );

         // Add the AP to the table.
         aps.add( newAp );
      }

      // Output the loaded AP table for reference.
      System.out.println( "Loaded AP table:\n" + this + "\n" );      

   }

   /**
    * Sets the size of the rssi array for each access point in the ap table.
    * Sets the value to the total number of scans to be done in this run of the
    * application
    * 
    * @param totalScans
    * 	the size to initialize each access point array to
    * 	
    */
   public void setTotalScans(int totalScans)
   {
	   for (int i = 0; i < aps.size(); i++)
	   {
		   aps.get(i).setTotalSamples(totalScans);
	   }
   }
   
   /**
    * Create a new file with the specified name
    * 
    * @param fileName
    * 		The File that will be created
    */
   public void createFile(String fileName) {
       File newFile = new File( fileName );
       try {
		  newFile.createNewFile();
       } catch (IOException e) {
		  System.out.println("Couldnt create new file. Exiting program");
		  System.exit(0);
       }
   }
   
   /**
    * Accessor method for the list of access points loaded in from the table.
    * 
    * @return The array list of known access points.
    */
   public ArrayList< AccessPoint > getAPTable()
   {
      return aps;
   }

   /**
    * Outputs the ID and BSSID of every AP in the table for each line.
    */
   public String toString()
   {
      String tableStr = "";
      for( int i = 0; i < aps.size(); i++ )
      {
         tableStr += aps.get( i ).getID() + " " + aps.get( i ).getBSSID() + "\n";
      }

      return tableStr;
   }
   
   public ArrayList< AccessPoint > getAPs()
   {
	   return aps;
   }

   private ArrayList< AccessPoint > aps; // A list of APs stored in the table.
   private String tableName; // The file name of the AP table that is loaded.
   boolean writeDebugFile; // Indicates if we want to write another debug file which will include more information about the APs.
   private boolean multiAttempts = false; //Flag to see if there were multiple attempts of loading the access point table.
   private AndroidLog tableLog;
   private AndroidLog tableLog_debug;
}
