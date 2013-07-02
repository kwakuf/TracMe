package com.capstone.TracMe;

import java.util.ArrayList;

public class Sample
{
   public ArrayList< AccessPoint > getScan()
   {
      return aps;
   }

   public void setScan( ArrayList< AccessPoint > aps )
   {
      this.aps = aps;
   }

   private ArrayList< AccessPoint > aps; // The list of access points generated for the current sample.
}