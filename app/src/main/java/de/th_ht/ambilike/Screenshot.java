/*
 * Ambilike produces an Ambilight like effect using the Philips Hue system and a rooted Android device
 * Copyright (C) 2015  Thomas Hartmann <thomas.hartmann@th-ht.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.th_ht.ambilike;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Screenshot implements Serializable
{
  protected Bitmap shot, oldshot;
  private String myFilesDir;
  private String cmdline;
  private ByteArrayBuffer buf;
  private int displaywidth, displayheight;
  private Semaphore semaphore;
  private Process sh;
  private Thread shellThread;
  private int oldClr;

  public Screenshot(int _displaywidth, int _displayheight, Context appContext)
  {
    buf = new ByteArrayBuffer(0);
    shot = Bitmap.createBitmap(1920, 1280, Bitmap.Config.ARGB_8888);
    displaywidth = _displaywidth;
    displayheight = _displayheight;

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(appContext);
    myFilesDir = settings.getString("MyFilesDir", "0");

    cmdline = "/system/bin/screencap " + myFilesDir + "/test.raw\n";
    //cmdline = "/system/bin/screencap\n";

    semaphore = new Semaphore(1);
    try
    {
      semaphore.acquire();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    shellThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          //System.out.println("Trying to start shell...");
          sh = Runtime.getRuntime().exec("su", null, null);
          //System.out.println("Waiting for shell...");
          //sh.waitFor();
          //System.out.println("Done...");
          //System.out.println("All good?");
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });

    shellThread.start();
    //System.out.println("Waiting for thread...");
    try
    {
      shellThread.join();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    //System.out.println("Done");
    semaphore.release();
  }

  /*public boolean snap()
  {
    int nBytes = (displayheight * displaywidth) * 4;
    shot.setHeight(displayheight);
    shot.setWidth(displaywidth);
    shot.setHasAlpha(true);

    if (!semaphore.tryAcquire())
    {
      System.out.println("Could not get semaphore...");
      return false;
    }
    long oldtime = System.nanoTime();
    OutputStream os = sh.getOutputStream();
    InputStream in = sh.getInputStream();

    try
    {
      //System.out.println("Writing command...");
      os.write(cmdline.getBytes("ASCII"));
      //System.out.println("Wrote command...");
      byte[] allbytes = new byte[nBytes];
      in.skip(12);
      int bytesRead = 0;
      while(bytesRead < nBytes*2)
      {
        int thistime = in.read(allbytes, 0+bytesRead, nBytes-bytesRead);
        *//*if(thistime == -1)
        {
          semaphore.release();
          return false;
        }*//*
        if(thistime != -1)
          bytesRead = bytesRead + thistime;
        System.out.println("BytesRead " + bytesRead + " nBytes" + nBytes);
      }
      shot.copyPixelsFromBuffer(ByteBuffer.wrap(allbytes));

    }
    catch (Exception e)
    {
      System.out.println("Something went wrong writing to shell...");
      e.printStackTrace();
      semaphore.release();
      return false;
    }
    long newtime = System.nanoTime();
    System.out.println("This took " + (newtime - oldtime) / 1000000 + "ms");

    semaphore.release();
    return true;
  }*/

  public boolean snap()
  {
    if (!semaphore.tryAcquire())
    {
      System.out.println("Could not get semaphore...");
      return false;
    }
    long oldtime = System.nanoTime();
    //System.out.println("Startin command...");

    try
    {
      //sh = Runtime.getRuntime().exec("su", null, null);
      //File lockfile = new File(myFilesDir + "/lock.file");
      File lockfile = new File(myFilesDir + "/test.raw");
      lockfile.delete();

      OutputStream os = sh.getOutputStream();
      os.write(cmdline.getBytes("ASCII"));
      //os.write(("touch " + lockfile.getAbsolutePath() + "\n").getBytes("ASCII"));
      os.flush();

      lockfile = new File(lockfile.getAbsolutePath());
      while (!lockfile.exists() || lockfile.length() < (displayheight * displaywidth) * 4 + 12)
      {
        //System.out.println("Waiting for lockfile...");
        //System.out.println(lockfile.getAbsolutePath());
        Thread.sleep(50);
      }

      //System.out.println("file has size: " + lockfile.length());

      //os.close();
      //sh.waitFor();
    } catch (Exception e)
    {
      System.out.println("Something went wrong writing to shell...");
      e.printStackTrace();
      semaphore.release();
      return false;
    }

    long newtime = System.nanoTime();

    //System.out.println("Command finished...");
    //System.out.println("It took " + (newtime - oldtime)/1000.0 + "ms");

    boolean isProcessed = processImage();

    semaphore.release();

    return (isProcessed);
  }

  private boolean processImage()
  {
    //System.out.println("Command completed...");
    shot.setHeight(displayheight);
    shot.setWidth(displaywidth);
    shot.setHasAlpha(true);
    int nBytes = (displayheight * displaywidth) * 4;

    File infile = null;
    InputStream in = null;

    try
    {
      infile = new File(myFilesDir + "/test.raw");
      in = new BufferedInputStream(new FileInputStream(infile));
    } catch (Exception e)
    {
      System.out.println("Error!!!");
      e.printStackTrace();
      return false;
    }

    //System.out.println("infile has length" + infile.length());

    byte[] allbytes = new byte[nBytes];

    try
    {
      in.skip(12);
      in.read(allbytes, 0, nBytes);
    } catch (Exception e)
    {
      System.out.println("Problem...");
      System.out.println(infile.length());
      e.printStackTrace();
      return false;
    }

    //System.out.println("File read...");

    shot.copyPixelsFromBuffer(ByteBuffer.wrap(allbytes));

    return true;


    //System.out.println("Converted???");
  }

  /*public boolean snap(){
    if(!semaphore.tryAcquire()){
      System.out.println("Could not get semaphore...");
      return false;
    }

    System.out.println("Got semaphore...");

    Command cmd = new Command(0, cmdline) {

      @Override
      public void commandOutput(int id, String line)
      {
        try
        {
          buf.append(line.getBytes("ASCII"), 0, line.length());
        }
        catch(Exception e) {
          System.out.println("Something went wrong...");
        }
        super.commandOutput(id, line);
      }

      @Override
      public void commandCompleted(int id, int exitcode) {

        System.out.println("Command completed... with exitcode " + exitcode);
        shot.setHeight(displayheight);
        shot.setWidth(displaywidth);
        shot.setHasAlpha(true);

        File infile = null;
        InputStream in = null;

        try
        {
          infile = new File(MainActivity.getMyFilesDir() + "/test.raw");
          in = new BufferedInputStream(new FileInputStream(infile));
        }
        catch(Exception e) {
          System.out.println("Error!!!");
          e.printStackTrace();
        }

        byte[] allbytes = new byte[(int) infile.length() - 12];

        try
        {
          in.skip(12);
          in.read(allbytes, 0, (int) infile.length() - 12);
        }
        catch(Exception e) {
          System.out.println("Problem...");
          System.out.println(infile.length());
          e.printStackTrace();
        }

        System.out.println("File read...");

        shot.copyPixelsFromBuffer(ByteBuffer.wrap(allbytes));


        System.out.println("Converted???");

        semaphore.release();
      }

      @Override
      public void commandTerminated(int id, String reason) {
        System.out.println("COmmand terminated " + reason);
        semaphore.release();
      }
    };

    buf.clear();
    try
    {
      System.out.println("Starting command...");
      RootShell.getShell(true).add(cmd);
      System.out.println("Starting command done...");
      //RootShell.getShell(true).add(new Command(0, "chmod 0666 /extSdCard/test.png"));
    }
    catch(Exception e) {
      System.out.println("Something went wrong...");
      e.printStackTrace();
    }
    return true;
  }
*/
  public void show(ImageView view)
  {
    try
    {
      semaphore.acquire();
      view.setImageBitmap(shot);
      semaphore.release();
    } catch (Exception e)
    {
      System.out.println("show could not acquire semaphore...");
    }
  }

  public int getDominantColor()
  {
    Bitmap scaledShot = null;
    try
    {
      semaphore.acquire();
      scaledShot = Bitmap.createScaledBitmap(shot, 1, 1, true);
      semaphore.release();
    } catch (Exception e)
    {
    }

    return (scaledShot.getPixel(0, 0));
  }

  public int getDominantColor2()
  {
    int clr = 0;
    try
    {
      semaphore.acquire();
      Bitmap scaledShot = null;
      final int targetWidth = 400;
      int targetHeight = (int) (shot.getHeight() / (shot.getWidth() / (double) targetWidth));
      scaledShot = Bitmap.createScaledBitmap(shot, targetWidth, targetHeight, true);
      if (oldshot != null)
      {
/*        if(oldshot.sameAs(scaledShot))
          System.out.println("Pics are equal...");
        else
          System.out.println("Shots are not equal...");*/
      }
      oldshot = scaledShot;
      Palette pal = Palette.generate(scaledShot, 20);
      List<Palette.Swatch> swatches = pal.getSwatches();

      int maxpop = 0;
      Palette.Swatch curSwatch = null;
      try
      {
        curSwatch = pal.getVibrantSwatch();
        maxpop = curSwatch.getPopulation();
      } catch (Exception e2)
      {

      }

      //System.out.println("Got " + swatches.size() + " Swatches");

      for (Palette.Swatch i : swatches)
      {
        //System.out.println("Swatch has " + i.getPopulation() + " pixels");
        if (i.getPopulation() > maxpop)
        {
          maxpop = i.getPopulation();
          curSwatch = i;
        }
      }

      clr = curSwatch.getRgb();
      oldClr = clr;

      semaphore.release();
    } catch (Exception e)
    {
      e.printStackTrace();
      semaphore.release();
      return oldClr;
    }

    return clr;
  }

}
