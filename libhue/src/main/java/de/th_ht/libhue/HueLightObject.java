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

package de.th_ht.libhue;

import android.graphics.Color;


public abstract class HueLightObject
{
  protected final double x1 = 0.674;
  protected final double y1 = 0.322;
  protected final double x2 = 0.408;
  protected final double y2 = 0.517;
  protected final double x3 = 0.168;
  protected final double y3 = 0.041;
  final Hue bridge;
  HueRestInterface.LightUpdate curUpdate;

  HueLightObject()
  {
    this.bridge = Hue.getInstance();
    curUpdate = new HueRestInterface.LightUpdate();
  }

  public abstract void postUpdate();

  public HueLightObject setTransitiontime(int ms)
  {
    curUpdate.transitiontime = (int) (ms / 100.0);
    return this;
  }

  public HueLightObject setOn(boolean on)
  {
    curUpdate.on = on;
    return this;
  }

  public HueLightObject setHue(int hue)
  {
    curUpdate.hue = hue;
    return this;
  }

  public HueLightObject setBrightness(int bri)
  {
    curUpdate.bri = bri;
    return this;
  }

  public HueLightObject setSaturation(int sat)
  {
    curUpdate.sat = sat;
    return this;
  }

  HueLightObject setXY(float[] xy)
  {
    Float[] tmp = new Float[2];
    tmp[0] = xy[0];
    tmp[1] = xy[1];
    curUpdate.xy = tmp;
    return this;
  }


  public HueLightObject startUpdate()
  {
    curUpdate = new HueRestInterface.LightUpdate();
    return this;
  }

  public HueLightObject setRGB(int color)
  {
    double red = Color.red(color) / 255f;
    double green = Color.green(color) / 255f;
    double blue = Color.blue(color) / 255f;

    red = (red > 0.04045f) ? Math.pow((red + 0.055f) / (1.0f + 0.055f), 2.4f) : (red / 12.92f);
    green = (green > 0.04045f) ? Math.pow((green + 0.055f) / (1.0f + 0.055f),
        2.4f) : (green / 12.92f);
    blue = (blue > 0.04045f) ? Math.pow((blue + 0.055f) / (1.0f + 0.055f), 2.4f) : (blue / 12.92f);

    double X = red * 0.649926f + green * 0.103455f + blue * 0.197109f;
    double Y = red * 0.234327f + green * 0.743075f + blue * 0.022598f;
    double Z = red * 0.0000000f + green * 0.053077f + blue * 1.035763f;

    double x = X / (X + Y + Z);
    double y = Y / (X + Y + Z);

    float[] xy = new float[2];
    xy[0] = (float) x;
    xy[1] = (float) y;

    setXY(xy);

    return this;
  }
}
