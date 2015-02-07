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
  protected Bitmap shot;
  private String myFilesDir;
  private String cmdline;
  private int displaywidth, displayheight;
  private Semaphore semaphore;
  private Process sh;
  private Thread shellThread;
  private int oldClr;

  public Screenshot(int _displaywidth, int _displayheight, Context appContext)
  {
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
          sh = Runtime.getRuntime().exec("su", null, null);
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });

    shellThread.start();
    try
    {
      shellThread.join();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    semaphore.release();
  }

  public boolean snap()
  {
    if (!semaphore.tryAcquire())
    {
      return false;
    }
    long oldtime = System.nanoTime();

    try
    {
      File lockfile = new File(myFilesDir + "/test.raw");
      lockfile.delete();

      OutputStream os = sh.getOutputStream();
      os.write(cmdline.getBytes("ASCII"));
      os.flush();

      lockfile = new File(lockfile.getAbsolutePath());
      while (!lockfile.exists() || lockfile.length() < (displayheight * displaywidth) * 4 + 12)
      {
        Thread.sleep(50);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      semaphore.release();
      return false;
    }

    long newtime = System.nanoTime();

    boolean isProcessed = processImage();

    semaphore.release();

    return (isProcessed);
  }

  private boolean processImage()
  {
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
      e.printStackTrace();
      return false;
    }

    byte[] allbytes = new byte[nBytes];

    try
    {
      in.skip(12);
      in.read(allbytes, 0, nBytes);
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }

    shot.copyPixelsFromBuffer(ByteBuffer.wrap(allbytes));

    return true;
  }

  public void show(ImageView view)
  {
    try
    {
      semaphore.acquire();
      view.setImageBitmap(shot);
      semaphore.release();
    } catch (Exception e)
    {
    }
  }

  public int getDominantColor()
  {
    int clr = 0;
    try
    {
      semaphore.acquire();
      Bitmap scaledShot = null;
      final int targetWidth = 400;
      int targetHeight = (int) (shot.getHeight() / (shot.getWidth() / (double) targetWidth));
      scaledShot = Bitmap.createScaledBitmap(shot, targetWidth, targetHeight, true);
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

      for (Palette.Swatch i : swatches)
      {
        if (i.getPopulation() > maxpop)
        {
          maxpop = i.getPopulation();
          curSwatch = i;
        }
      }

      try
      {
        clr = curSwatch.getRgb();
      } catch (NullPointerException e)
      {
        clr = oldClr;
      }
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
