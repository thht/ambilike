/*
 * Ambilike produces an Ambilight like effect using the Philips Hue system and a rooted Android 
 * * device
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
import android.graphics.Color;


class HueThread extends Thread
{
  private final HueController hueController;
  private final Screenshot screenshot;
  private boolean killed;
  private int nLights;

  public HueThread(int displaywidth, int displayheight, Context context,
                   HueController hueController)
  {
    super();
    this.hueController = hueController;
    screenshot = new Screenshot(displaywidth, displayheight, context);
    nLights = hueController.getAllLights().size();
  }

  @Override
  public void run()
  {
    killed = false;
    boolean good = true;
    ColorAverager colorAverager = new ColorAverager();
    long oldtime = System.nanoTime();
    long curtime = oldtime;
    long timediff = 100 * nLights * 1000000;

    setPriority(MIN_PRIORITY);
    android.os.Process.setThreadPriority(19);

    while (!killed)
    {
      try
      {
        sleep(50);
      }
      catch (InterruptedException e)
      {
        killed = true;
      }
      good = screenshot.snap();
      if (good)
      {
        int clr = screenshot.getDominantColor();
        float[] hsv = new float[3];
        Color.colorToHSV(clr, hsv);

        colorAverager.put(Color.red(clr), Color.green(clr), Color.blue(clr), (int) (hsv[2] * 255));
      }

      curtime = System.nanoTime();
      if ((curtime - oldtime) > timediff)
      {
        int[] rgb = new int[]{colorAverager.red(), colorAverager.green(), colorAverager.blue()};
        hueController.setColor(rgb, colorAverager.brightness());
        //Timber.d("averager has " + colorAverager.size() + " items.");
        //colorAverager.clear();
        oldtime = curtime;
      }
    }
  }

  public void terminate()
  {
    killed = true;
  }
}
