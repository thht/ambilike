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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.th_ht.libhue.Errors.RestReturnError;
import retrofit.RetrofitError;


public class HueLightGroup extends HueLightObject
{
  private final List<HueLight> lights;

  public HueLightGroup()
  {
    super();
    lights = new ArrayList<>(100);
  }

  public HueLightGroup(List<HueLight> lights)
  {
    super();
    this.lights = lights;
  }

  public HueLightGroup(Map<Integer, HueLight> lights)
  {
    super();
    this.lights = new ArrayList<>(lights.values());
  }

  public void clear()
  {
    lights.clear();
  }

  public void addLight(HueLight light)
  {
    lights.add(light);
  }

  @Override
  public void postUpdate()
  {
    for (HueLight light : lights)
    {
      light.setUpdate(curUpdate);
      light.postUpdate(false);
    }

    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          bridge.updateAllLights();
        }
        catch (RestReturnError | RetrofitError ignored)
        {
        }
      }
    }).start();
  }
}
