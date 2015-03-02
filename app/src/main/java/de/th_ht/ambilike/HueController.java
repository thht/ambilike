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

import android.app.ActivityManager;
import android.graphics.Color;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import de.th_ht.libhue.Hue;
import de.th_ht.libhue.HueLightGroup;

/**
 * Created by th on 02.03.2015.
 */

@EBean(scope = EBean.Scope.Singleton)
public class HueController
{
  Hue hue;
  @SystemService
  ActivityManager am;
  @Pref
  HuePreferences_ preferences;
  private int transition;
  private float colorExp;
  private int briMult;
  private int minBri;
  private int maxBri;
  private HueLightGroup lights;

  HueController()
  {
    hue = Hue.getInstance();
    transition = (int) (preferences.Transitiontime().get() * 1000);
    colorExp = preferences.Colorfullness().get();
    briMult = preferences.Brightness().get();
    minBri = preferences.MinBrightness().get();
    maxBri = preferences.MaxBrightness().get();
  }

  public void connect()
  {


  }

  public void setColor(int[] rgb, int brightness)
  {
    try
    {
      // Convert Values...
      brightness = brightness * (briMult / 100);
      if (brightness > maxBri)
        brightness = maxBri;
      if (brightness < minBri)
        brightness = minBri;

      boolean needsLeveling = false;
      float maxrgb = 0;
      for (int i = 0; i < 3; i++)
      {
        rgb[i] = (int) Math.pow(rgb[i], colorExp);
        if (maxrgb < rgb[i])
          maxrgb = rgb[i];
        if (rgb[i] > 255)
          needsLeveling = true;
      }

      if (needsLeveling)
      {
        for (int i = 0; i < 3; i++)
        {
          rgb[i] = (int) (((rgb[i] / maxrgb)) * 255);
        }
      }


      lights.startUpdate();
      lights.setBrightness(brightness);
      lights.setTransitiontime(transition);

      List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
      boolean isNetflix = false;
      if (taskInfo.get(0).topActivity.getClassName().contains(".netflix."))
        isNetflix = true;

      if (isNetflix)
      {
        int tmp = rgb[0];
        rgb[0] = rgb[2];
        rgb[2] = tmp;
      }

      lights.setRGB(Color.rgb(rgb[0], rgb[1], rgb[2]));
      lights.postUpdate();

    } catch (NullPointerException e)
    {
    }
  }

  public int getTransition()
  {
    return transition;
  }

  public void setTransition(int transition)
  {
    this.transition = transition;
  }

  public float getColorExp()
  {
    return colorExp;
  }

  public void setColorExp(float colorExp)
  {
    this.colorExp = colorExp;
  }

  public int getMinBri()
  {
    return minBri;
  }

  public void setMinBri(int minBri)
  {
    this.minBri = minBri;
  }

  public int getMaxBri()
  {
    return maxBri;
  }

  public void setMaxBri(int maxBri)
  {
    this.maxBri = maxBri;
  }

  public void setLights(HueLightGroup lights)
  {
    this.lights = lights;
  }

  public int getBriMult()
  {
    return briMult;
  }

  public void setBriMult(int briMult)
  {
    this.briMult = briMult;
  }
}
