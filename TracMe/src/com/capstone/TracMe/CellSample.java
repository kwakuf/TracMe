package com.capstone.TracMe;

import java.util.ArrayList;

import android.graphics.Point;

public class CellSample
{
   public CellSample()
   {
      loc = new Point( -1, -1 );
      samples = new ArrayList< Sample >();
   }

   public void setLoc( int x, int y )
   {
      loc.x = x;
      loc.y = y;
   }

   public Point getLoc()
   {
      return loc;
   }

   public ArrayList< Sample > getSamples()
   {
      return samples;
   }
   
   public boolean equals(Point testPoint) 
   {
	   if (testPoint.x == loc.x && testPoint.y == loc.y ) 
	   {
		   return true;
	   }
	   
	   return false;
   }

   private Point loc; // The xy location of the cell in the grid.
   private ArrayList< Sample > samples; // The list of samples for the current cell.
}
