package de.th_ht.libhue;

import android.graphics.Color;

/**
 * Created by th on 28.02.2015.
 */
public abstract class HueLightObject
{
  protected HueRestInterface.LightUpdate curUpdate;
  protected Hue bridge;

  protected HueLightObject()
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


  public HueLightObject startUpdate()
  {
    curUpdate = new HueRestInterface.LightUpdate();
    return this;
  }

  public HueLightObject setRGB(int color)
  {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);
    setHue((int) (hsv[0] * 65535 / 360));
    setSaturation((int) (hsv[1] * 254));
    return this;
  }
}
