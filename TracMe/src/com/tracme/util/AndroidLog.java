package com.tracme.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import android.os.Environment;

/**
 * Class Representing and AndroidLog Object. The AndroidLog Object is a file
 * that is stored in the android file system. 
 * 
 * @author Kwaku Farkye
 * @author Ken Ugo
 *
 */
public class AndroidLog {
  
  /** File that will be accessed */
  public File file;
  
  /** Path to the file */
  public File path;
  
  /** Input stream for predictions log */
  public FileInputStream inputStream;
  
  /**
   * Constructor for AndroidLog object. Initializes directory to store log in and the name of the file.
   * 
   * @param filename The name of the file being accessed.
   */
  public AndroidLog(String filename) {
	// Path is currently going to DOWNLOADS Directory
    path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);    
    file = new File(path, filename);
  }
  
  /**
   * Constructor called when Log file is needed for prediction
   * 
   * @param filename
   * 	The name of the file being loaded
   * 
   * @param predict
   *  Flag specifying whether this file is used for prediction/localization
   *  
   */
  public AndroidLog(String filename, boolean predict)
  {
	  path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);    
	  file = new File(path, filename);
	  
	  // In the case of prediction, make another log file
	  if (predict == true)
	  {
		  try {
			  inputStream = new FileInputStream(file);
		  } catch (Exception e)
		  {
			  System.out.println("Error loading file: " + e.getLocalizedMessage());
			  System.exit(-1);
		  }
	  }
  }
  
  /**
   * Writes the data given to the file
   * 
   * @param data String of data to write to the file
   * 
   */
  public void save(String data)
  {
    try
    {
      // Make sure the Downloads directory exists.
      path.mkdirs();
      FileOutputStream fos = new FileOutputStream(file, true);
      fos.write(data.getBytes());
      fos.close();
      System.out.println("File successfully saved.");
    }
    catch (Exception ex)
    {
      System.out.println("Error saving file: " + ex.getLocalizedMessage());
    }
  }

  /**
   * Reads data from the file.
   * 
   * @return The data read from the file in string format
   * 
   */
  public String load()
  {
    try
    {
      FileInputStream fis = new FileInputStream(file);
      BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
      String line = null, input="";
      while ((line = reader.readLine()) != null)
          input += line + "\n";
      reader.close();
      fis.close();
      System.out.println("File successfully loaded.");
      return input;
    }
    catch (Exception ex)
    {
      System.out.println("Error loading file: " + ex.getLocalizedMessage());
      return "";
    }
  } 
}