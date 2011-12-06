package biz.navius.saltroad.utils;

import android.util.Log; 

import java.io.BufferedOutputStream;
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.util.zip.ZipEntry; 
import java.util.zip.ZipInputStream; 
 
public class Decompress { 
  private String _zipFile; 
  private String _location; 
 
  public Decompress(String zipFile, String location) { 
    _zipFile = zipFile; 
    _location = location; 
 
    _dirChecker(""); 
  } 
 
  public boolean unzip() { 
      final int SIZE = 1024*1024;
      int size;
      byte[] buffer = new byte[SIZE];

      try  { 
          FileInputStream fin = new FileInputStream(_zipFile); 
          ZipInputStream zin = new ZipInputStream(fin); 
          ZipEntry ze = null; 

          while ((ze = zin.getNextEntry()) != null) { 
              Log.v("Decompress", "Unzipping " + ze.getName()); 
       
              if(ze.isDirectory()) { 
                _dirChecker(ze.getName()); 
              } else { 
                FileOutputStream fout = new FileOutputStream(_location + ze.getName()); 
                BufferedOutputStream bufferOut = new BufferedOutputStream(fout, buffer.length);
                
                while((size = zin.read(buffer, 0, buffer.length)) != -1) {
                    bufferOut.write(buffer, 0, size);
                }
                bufferOut.flush();
                bufferOut.close();
               } 
            } 
          zin.close();
          return true;
        } catch(Exception e) { 
          Log.e("Decompress", "unzip", e); 
          return false;
        } 
  } 
 
  private void _dirChecker(String dir) { 
    File f = new File(_location + dir); 

    if(!f.isDirectory()) { 
      f.mkdirs(); 
    } 
  } 
}