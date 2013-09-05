package com.capstone.TracMe;

import java.util.ArrayList;
import java.text.DateFormat;
import java.util.Date;

import com.tracme.util.*;

/**
 * The main sampling program that handles the GUI, data sampling, data parsing,
 * and other attributes for the Tracme project.
 * 
 * @author James Humphrey
 * @author Kwaku Farkye
 * @author Ken Ugo
 * 
 */
public class SampleProgram
{

    /**
     * The raw data file that holds all of the
     * generated samples for the current data
     * set.
     */
    private AndroidLog sampleFile;
    
    /**
     * The extended data file with more information/comments 
     * about each sample and about the entire sample set.
     */
    private AndroidLog sampleFileExt;

    /**
     * The name of the file that stores the AP mapping between 
     * ID and BSSID.
     */
    private String apTableFileName;

    /** The number of points to sample */
    private int numPoints;

    /** The number of samples we need to do for the current position */
    private int numSamples;

    /** name of the output file we write to */
    private String sampleFileName;
    
    /** the direction faced while sampling is done */
    private String direction;
    
    /** Array list of sampled points */
    protected ArrayList<Integer> samplePoints;
    
    /**
     * Initializes objects with default values.
     */
    public SampleProgram( )
    {
        sampleFile = null;
        sampleFileExt = null;

        apTableFileName = "";
        sampleFileName = "";
        
        numPoints = 1;
        numSamples = 20;
        
        samplePoints = new ArrayList<Integer>();

    }
    
    /**
     * Method is called after all scans are complete. Finishes all 
     * interpolation of access point data and writes information to
     * files, saving the results of the sample. Currently this method
     * will only save the amount of points specified initially to the
     * output file.
     * 
     * @param locDesc
     *        A short description of the location (i.e. "Dexter's Lawn").
     *        
     * @param fileComment
     *        A comment that can be added to the top of the extended file.
     *        
     * @param apts APTable Object containing access point information
     * 		from this run.
     */
    public void finishAndSave(String locDesc, String fileComment, APTable apts)
    {
    	
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
        printString = ( getNumPoints() + 1 ) + "\n" + ( getNumPoints() + 1 )
                + "\n" + apts.getAPTable().size() + "\n";
        sampleFile.save( printString );
        
        // Run this loop for the amount of points we sampled
        for( int i = 0; i < getNumPoints(); i++ )
        {
            // Get the location of the current point.
            int loc = samplePoints.get(i);
            
            // Output the location information to file.
            
            //In this case, we only need to output one location (because we are
            // using a point system instead of a coordinate system
            sampleFile.save( "###" + loc + "\n" );
            sampleFileExt.save( "###" + loc + "\n" );
            
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
            
            // Increase our offset for finding the correct rssis of an access point
            offset += this.getNumSamples();
            
        }
        
    }

    /**
     * Accessor method for grid size in x direction
     * 
     * @return grid size in x direction
     */
    public int getNumPoints()
    {
        return numPoints;
    }

    /**
     * Setter method for grid size in x direction
     * 
     * @param val
     *            Value to set numPoints to
     */
    public void setNumPoints( int val )
    {
        numPoints = val;
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

    /**
     * Adds the point currently being sampled to the array list of points
     * 
     * @param point The point to add to the list
     */
    public void addPoint(int point)
    {
    	samplePoints.add(point);
    }
    
    /**
     * Getter for the amount of points sampled
     * 
     * @return The amount of samples done
     */
    public int getPointsSampled()
    {
    	return samplePoints.size();
    }
    
}
