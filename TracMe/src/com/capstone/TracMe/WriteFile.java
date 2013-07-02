package com.capstone.TracMe;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

/**
 * Class for reading and writing to files in a cross-platform way. This class is
 * based on code from homeandlearn.co.uk/java/write_to_textfile.html.
 * 
 * @author James Humphrey
 */
public class WriteFile
{
   /**
    * Initializes the path of the file and automatically sets to append mode for
    * file.
    * 
    * @param path
    *           The file name including its path
    */
   public WriteFile( String path )
   {
      this( path, true );
   }

   /**
    * Initializes the path of the file and sets the append mode for the file.
    * 
    * @param path
    *           The file name including its path
    * @param append
    *           Indicates if we want to append to the file when writing
    */
   public WriteFile( String path, boolean append )
   {
      this.path = path;
      try
      {
         print_line = new PrintWriter( new FileWriter( path, append ) );
      }
      catch( IOException e )
      {
         e.printStackTrace();
         System.out.println( "Failed to open file " + path );
      }
   }

   /**
    * Write a text string to the file indicated by the parameter.
    * 
    * @param text
    *           The text to write to the file.
    */
   public void writeToFile( String text )
   {
      try
      {
         print_line.print( text );

         // We need to manually check for any errors on Android because it doesn't throw an IOException.
         if( print_line.checkError() )
         {
            // A problem has occurred in this writer.
            // TODO: Implement an error message in Android.
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
         System.out.println( "Failed to write to file " + path );
      }
   }

   public String getPath()
   {
      return path;
   }

   private String path; // The file name including its path.
   PrintWriter print_line; // Interface for writing a lint to the file.
}
