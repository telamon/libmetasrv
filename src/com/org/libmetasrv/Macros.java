package com.org.libmetasrv;

/*
  * Macros.java
  *
  * Created on den 9 januari 2006, 11:52
  * 
  *
 * The stroke object and stroke handling.
 * Copyright (C) 2006 Tony Ivanov
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the 
 * Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
   
   import java.nio.ByteBuffer;
 import java.io.*;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
     /**
  * Abstract class Macro Cannot be instantiated
  * A collection of general static macros and static functions.
  * @author lordtelamon
  */
 public class Macros {
     public static byte[] hexStringToByte(String hex ){
         byte[] bts = new byte[hex.length() / 2];
         for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
         }
         return bts;
     }
       /** u8 to signed java integer*/            
   public static int sign(byte b) {
     return (int) b & 0xFF;
     }
   
   /** u16 to signed java integer*/            
   public static int sign(short s) {
     return ((int)s & 0xFFFF);
     }
     /** u16 to signed java integer*/
   public static int sign(int i){
     return (int) i & 0xFFFF;  
   }
   /** u32 to signed java integer */
   public static long sign(long l){
     return (long) l & 0xffffffff;  
   }
     
   public static String byteToHex(byte b){
   int i = b & 0xFF;
   return i<0x10 ? "0"+Integer.toHexString(i) : Integer.toHexString(i);
   }
   
   public static float pgFloatToFloat(byte a, byte b){
   /*[12:45:38] <henke> float bA = 127;
 [12:45:38] <henke> float bB = 60;
 [12:45:38] <henke> float f = bB+((float)(bA/255));
 [12:45:38] <henke> System.out.println(""+f);//60.49804
    */
       return ((float)sign(b)) +((float)sign(a)/255);
   } 
 public static byte[] floatToPgFloat(float f) {
     byte[] pgfloat=new byte[2];    
     pgfloat[1]=(byte) Macros.unsign((int)f);
     float d = f-((int)f);    
     pgfloat[0]=(byte) Macros.unsign((int)(d*255));
     return pgfloat;
 }
     
     public static String byteArrayToHexView(byte[] inc){
         String DebugLine = new String();
         String Ascii = new String();
         ByteBuffer datb = ByteBuffer.wrap(inc);
         DebugLine += "Data:";
          int newrow =0;
         for(int i =0; i< inc.length; i++){
             if(i >= newrow){
                DebugLine+= Ascii + "\n";
                Ascii = "";
                newrow += 16;
             } 
             byte tmpByte = datb.get();
             DebugLine += Macros.byteToHex(tmpByte) +" ";                        
             Ascii += (char) Macros.sign(tmpByte);
         }
         DebugLine+= Ascii + "\n----------------------------------\n-";        
         return DebugLine;
     }    
     /** Fast & simple file copy. */
     public static void copy(File source, File dest) throws IOException {
          FileChannel in = null, out = null;
          try {          
               in = new FileInputStream(source).getChannel();
               out = new FileOutputStream(dest).getChannel();
                 in.transferTo( 0, in.size(), out);
                 //long size = in.size();
               //MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
               //out.write(buf);
            } finally {
               if (in != null)          in.close();
               if (out != null)     out.close();
          }
     }
     
     public static short unsign(short foo){
         return (short)(foo < 0 ? ((int)(foo >>> 1) << 1) + (foo & 1) : foo);
     }
     public static int unsign(int foo){
         return foo < 0 ? ((int)(foo >>> 1) << 1) + (foo & 1) : foo;
     }
     public static long unsign(long foo) {
         return foo < 0 ? ((long)(foo >>> 1) << 1) + (foo & 1) : foo;        
     }
     public static int[] imgtoarr(java.awt.image.BufferedImage img){
        int data[] = new int[img.getWidth()*img.getHeight()];
        for(int y=0;y<img.getHeight();y++)
            for(int x=0;x < img.getWidth();x++)               
                data[x + y*img.getWidth()]=img.getRGB(x, y);
        return data;
    }
 }
 