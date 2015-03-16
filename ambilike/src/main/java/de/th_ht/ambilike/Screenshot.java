/*
 * Ambilike produces an Ambilight like effect using the Philips Hue system and a rooted Android
 * device
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
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Semaphore;

class Screenshot implements Serializable
{
  private final Bitmap shot;
  private final String cmdline;
  private final int displaywidth;
  private final int displayheight;
  private final Semaphore semaphore;
  private final Thread shellThread;
  private Process sh;
  private int oldClr;
  private int nBytes;
  private byte[] allbytes;
  private BufferedInputStream in;

  public Screenshot(int _displaywidth, int _displayheight, Context appContext)
  {
    shot = Bitmap.createBitmap(1920, 1280, Bitmap.Config.ARGB_8888);
    displaywidth = _displaywidth;
    displayheight = _displayheight;

    shot.setHeight(displayheight);
    shot.setWidth(displaywidth);
    shot.setHasAlpha(true);
    nBytes = (displayheight * displaywidth) * 4;
    allbytes = new byte[nBytes];
    cmdline = "/system/bin/busybox nice -n 19 /system/bin/screencap\n";

    semaphore = new Semaphore(1);
    try
    {
      semaphore.acquire();
    }
    catch (Exception e)
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
          in = new BufferedInputStream(sh.getInputStream());
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });

    shellThread.start();
    try
    {
      shellThread.join();
    }
    catch (Exception e)
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

      OutputStream os = sh.getOutputStream();
      os.write(cmdline.getBytes("ASCII"));
      os.flush();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      semaphore.release();
      return false;
    }

    long newtime = System.nanoTime();

    boolean isProcessed = processImage();

    //Timber.d("Processing took " + ((newtime / 1000000 - oldtime / 1000000)) + "ms");

    semaphore.release();

    return (isProcessed);
  }

  private boolean processImage()
  {
    try
    {
      in.skip(12);
      int readbytes = 0;
      while (readbytes < nBytes)
      {
        int tmp = in.read(allbytes, readbytes, nBytes - readbytes);
        if (tmp > 0)
        {
          readbytes = readbytes + tmp;
        }
        Thread.yield();
      }
    }
    catch (Exception e)
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
    }
    catch (Exception ignored)
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
      }
      catch (Exception ignored)
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
      }
      catch (NullPointerException e)
      {
        clr = oldClr;
      }
      oldClr = clr;

      semaphore.release();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      semaphore.release();
      return oldClr;
    }

    return clr;
  }
}
