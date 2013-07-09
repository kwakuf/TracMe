package com.capstone.TracMe;

import java.util.ArrayList;
import java.util.Collections;
import java.text.DateFormat;
import java.util.Date;

import android.graphics.Point;

/**
 * The main sampling program that handles the GUI, data sampling, data parsing,
 * and other attributes for the tracme project.
 * 
 * @author James Humphrey
 * @author Kwaku Farkye
 */
public class SampleProgram
{

    /**
     * Initializes objects with default values.
     */
    public SampleProgram( )
    {
        sampleFile = null;
        sampleFileExt = null;

        apTableFileName = "";
        apTable = null;

        wifiScanner = null;

        samples = new ArrayList<CellSample>();

    }

    /**
     * Runs the sampling WiFi scanner for the current cell.
     * 
     */
    public void runCellSample() //As of right now this won't be able to run because we can't obtain the aps from the scan
    {
        // Create a new cell sample to add to the sample scanner.
        CellSample newCellSample = new CellSample();
        newCellSample.setLoc( gridx, gridy );
        samples.add( newCellSample );
    }
    
    public void finishAndSave(String locDesc, String fileComment, APTable apts)
    {
        //apTable = new APTable(getAPFileName());
        //apTable.loadTable();
    	
    	int offset = 0; //The offset for the rssi array index of an access point

        // Create and open the sample file if it doesn't already exist.
        sampleFile = new AndroidLog( sampleFileName + ".txt" );

        // Create and open the extended sample file if it doesn't already exist.
        sampleFileExt = new AndroidLog( sampleFileName + "_ext.txt" );

        // Get the current date so we can record that in the file.
        Date date = new Date();
        String dateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG,
                DateFormat.LONG ).format( date );

        // Write the header information for the sample file.
        sampleFileExt
                .save( "//-------------------------------------------------------------------------------\n" );
        sampleFileExt.save( "// Sample File Name:        "
                + sampleFileName + "\n" );
        sampleFileExt.save( "// Date:                    " + dateFormat
                + "\n" );
        sampleFileExt.save( "// Location:                " + locDesc
                + "\n" );
        sampleFileExt.save( "// GPS Coordinates:         " + "???"
                + "\n" );
        sampleFileExt.save( "// GIS Map Coordinates:     " + "???"
                + "\n" );
        sampleFileExt.save( "// Comment:                 " + fileComment
                + "\n" );
        sampleFileExt.save( "// Direction:               " + direction
                + "\n" );
        sampleFileExt
                .save( "//-------------------------------------------------------------------------------\n\n" );

        String printString = new String();

        // Write the max x/y coordinates and the number of access points at top
        // of file.
        printString = ( getGridSizeX() + 1 ) + "\n" + ( getGridSizeY() + 1 )
                + "\n" + apts.getAPTable().size() + "\n";
        sampleFile.save( printString );
        
        for( int i = 0; i < samples.size(); i++ )
        {
            // Get the xy location of the current cell.
            int locx = samples.get( i ).getLoc().x;
            int locy = samples.get( i ).getLoc().y;
            
            // Output the location information to file.
            
            //In this case, we only need to output one location (because we are
            // using a point system instead of a coordinate system
            sampleFile.save( "###" + locx + "\n" );
            sampleFileExt.save( "###" + locx + "," + locy + "\n" );
            
            // For each coordinate output the rssi data for each access point
            for (int j = 0; j < this.getNumSamples(); j++)
            {
            	for (int k = 0; k < apts.getAPs().size(); k++)
            	{
            		printString = apts.getAPs().get( k ).getID() + ":"
                        + apts.getAPs().get( k  ).getRSSI(j + offset) + ";";

            		sampleFile.save( printString );
            		sampleFileExt.save( printString );
            		
            	}
        		
                System.out.println( "" );
                sampleFile.save( "\n" );
                sampleFileExt.save( "\n" );
                
            }
            
            offset += this.getNumSamples();
            
        }
        
    }

    /**
     * Outputs the results of the scan to file.
     * 
     * @param locDesc
     *            A short description of the location (i.e. "Dexter's Lawn").
     * @param fileComment
     *            A comment that can be added to the top of the extended file.
     */
    public void finishSampling( String locDesc, String fileComment )
    {
        apTable = new APTable(getAPFileName());
        apTable.loadTable();
        // Add any APs to the sample list that have 0 signal strength.
        addZeroAPs();

        // Create and open the sample file if it doesn't already exist.
        sampleFile = new AndroidLog( sampleFileName + ".txt" );

        // Create and open the extended sample file if it doesn't already exist.
        sampleFileExt = new AndroidLog( sampleFileName + "_ext.txt" );

        // Get the current date so we can record that in the file.
        Date date = new Date();
        String dateFormat = DateFormat.getDateTimeInstance( DateFormat.LONG,
                DateFormat.LONG ).format( date );

        // Write the header information for the sample file.
        sampleFileExt
                .save( "//-------------------------------------------------------------------------------\n" );
        sampleFileExt.save( "// Sample File Name:        "
                + sampleFileName + "\n" );
        sampleFileExt.save( "// Date:                    " + dateFormat
                + "\n" );
        sampleFileExt.save( "// Location:                " + locDesc
                + "\n" );
        sampleFileExt.save( "// GPS Coordinates:         " + "???"
                + "\n" );
        sampleFileExt.save( "// GIS Map Coordinates:     " + "???"
                + "\n" );
        sampleFileExt.save( "// Comment:                 " + fileComment
                + "\n" );
        sampleFileExt.save( "// Direction:               " + direction
                + "\n" );
        sampleFileExt
                .save( "//-------------------------------------------------------------------------------\n\n" );

        String printString = new String();

        // Write the max x/y coordinates and the number of access points at top
        // of file.
        printString = ( getGridSizeX() + 1 ) + "\n" + ( getGridSizeY() + 1 )
                + "\n" + apTable.getAPTable().size() + "\n";
        sampleFile.save( printString );

        for( int i = 0; i < samples.size(); i++ )
        {
            // Get the xy location of the current cell.
            int locx = samples.get( i ).getLoc().x;
            int locy = samples.get( i ).getLoc().y;

            // Output the location information to file.
            // System.out.println( "###" + locx + "," + locy + "\n" );
//            printArea.append( "###" + locx + "," + locy + "\n" );
            sampleFile.save( "###" + locx + "," + locy + "\n" );
            sampleFileExt.save( "###" + locx + "," + locy + "\n" );

            // Write the current sample to the file.
            for( int j = 0; j < samples.get( i ).getSamples().size(); j++ )
            {
                ArrayList<AccessPoint> aps = samples.get( i ).getSamples()
                        .get( j ).getScan();
                for( int k = 0; k < aps.size(); k++ )
                {
                    printString = aps.get( k ).getID() + ":"
                            + aps.get( k ).getRSSI() + ";";

//                    printArea.append( printString );
                    // printArea.updateUI();
                    // System.out.print( printString );
                    sampleFile.save( printString );
                    sampleFileExt.save( printString );
                }

                // Move to the next line.
//                printArea.append( "\n" );
                System.out.println( "" );
                sampleFile.save( "\n" );
                sampleFileExt.save( "\n" );
            }
        }
    }

    /**
     * Go back to the beginning of the sample list and add the APs with 0 signal
     * strength that didn't register for some samples.
     */
    private void addZeroAPs()
    {
        // Loop through the AP table and make sure they all exist on each sample
        // line.
        for( int i = 0; i < samples.size(); i++ )
        {
            for( int j = 0; j < samples.get( i ).getSamples().size(); j++ )
            {
                // Sort the AP list by increasing ID value.
                Collections.sort( samples.get( i ).getSamples().get( j )
                        .getScan(), new AccessPointIDComparator() );

                if( samples.get( i ).getSamples().get( j ).getScan().size() != apTable
                        .getAPTable().size() )
                {
                    for( int k = 0; k < /*
                                         * samples.get( i ).getSamples().get( j
                                         * ).getScan().size()
                                         */apTable.getAPTable().size(); k++ )
                    {
                        if( k > samples.get( i ).getSamples().get( j )
                                .getScan().size() - 1
                                || samples.get( i ).getSamples().get( j )
                                        .getScan().get( k ).getID() != k + 1 )
                        {
                            AccessPoint zeroAP = apTable.getAPTable().get( k );
                            zeroAP.setRSSI( 0 );
                            samples.get( i ).getSamples().get( j ).getScan()
                                    .add( k, zeroAP );
                            System.out.println( zeroAP.getID() + ":" + 0 + ";" );
                            System.out.println( "Adding zero sample" );
                        }
                    }
                }
            }
        }
    }

    /**
     * Clears the current scan so we can start over with a different sample set.
     */
    public void clearScan()
    {
        samples.clear();
    }

    /**
     * Accessor method for all of the cell samples in the current sample set.
     * 
     * @return The ArrayList of the sample set for all cells.
     */
    public ArrayList<CellSample> getSamples()
    {
        return samples;
    }

    /**
     * Accessor method for grid size in x direction
     * 
     * @return grid size in x direction
     */
    public int getGridSizeX()
    {
        return gridSizeX;
    }

    /**
     * Setter method for grid size in x direction
     * 
     * @param val
     *            Value to set gridSizeX to
     */
    public void setGridSizeX( int val )
    {
        gridSizeX = val;
    }

    /**
     * Setter method for grid size in y direction
     * 
     * @param val
     *            Value to set gridSizeY to
     */
    public void setGridSizeY( int val )
    {
        gridSizeY = val;
    }

    /**
     * Accessor method for grid size in y direction
     * 
     * @return grid size in y direction
     */
    public int getGridSizeY()
    {
        return gridSizeY;
    }

    /**
     * Setter method for number of samples
     * 
     * @param val
     *            Value to set numSamples to
     */
    public void setNumSamples( int val )
    {
        numSamples = val;
    }

    /**
     * Accessor method for number of samples
     * 
     * @return desired number of samples
     */
    public int getNumSamples()
    {
        return numSamples;
    }

    /**
     * Accessor method for grid location in x direction
     * 
     * @return grid location in x direction
     */
    public int getGridX()
    {
        return gridx;
    }

    /**
     * Setter method for grid location in x direction
     * 
     * @param val
     *            Value to set gridx to
     */
    public void setGridX( int val )
    {
        gridx = val;
    }

    /**
     * Setter method for grid location in y direction
     * 
     * @param val
     *            Value to set gridy to
     */
    public void setGridY( int val )
    {
        gridy = val;
    }

    /**
     * Accessor method for grid location in y direction
     * 
     * @return cell location in y direction
     */
    public int getGridY()
    {
        return gridy;
    }
    
    /**
     * Setter method for name of sample file
     * 
     * @param val
     *            Value to set sampleFileName to
     */
    public void setSampleFileName( String val )
    {
        sampleFileName = val;
    }

    /**
     * Accessor method for sample file name
     * 
     * @return name of sample file
     */
    public String getSampleFileName()
    {
        return sampleFileName;
    }

    /**
     * Setter method for name of access point table file
     * 
     * @param val
     *            Value to set access point file name to
     */
    public void setAPFileName( String val )
    {
        apTableFileName = val;
    }

    /**
     * Accessor method for access point file name
     * 
     * @return name of access point table file
     */
    public String getAPFileName()
    {
        return apTableFileName;
    }

    /**
     * Setter method for direction of sampling
     * 
     * @param val
     *            Value to set direction to
     */
    public void setDirection( String val )
    {
        direction = val;
    }

    /**
     * Accessor method for direction of sampling
     * 
     * @return direction we are doing the sampling
     */
    public String getDirection()
    {
        return direction;
    }

    private AndroidLog sampleFile; // The raw data file that holds all of the
                                  // generated samples for the current data
                                  // set.
    private AndroidLog sampleFileExt; // The extended data file with more
                                     // information/comments about each
                                     // sample and about the entire sample
                                     // set.

    private String apTableFileName; // The name of the file that stores the AP
                                    // mapping between ID and BSSID.
    private APTable apTable; // The table of access points loaded from a file.

    private TracMe wifiScanner; // The generic WiFi scanner used for
                                     // generating a sample list of access
                                     // points with RSSID values.

    public ArrayList<CellSample> samples; // The list of cells that are being
                                           // sampled.


    /** Size of grid in the X direction */
    private int gridSizeX = 3;

    /** Size of grid in the Y direction */
    private int gridSizeY = 3;

    /** The number of samples we need to do for the current position */
    private int numSamples = 20;

    /** The current Cell Location in X direction */
    private int gridx = 1;

    /** The current Cell Location in Y direction */
    private int gridy = 1;

    /** name of the output file we write to */
    private String sampleFileName = ""; //will be in tracme

    /** the direction faced while sampling is done */
    private String direction;

}
