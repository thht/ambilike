package de.th_ht.libhue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.th_ht.libhue.Errors.RestReturnError;
import retrofit.RetrofitError;

/**
 * Created by th on 28.02.2015.
 */
public class HueLightGroup extends HueLightObject
{
  List<HueLight> lights;

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
        } catch (RestReturnError e)
        {
        } catch (RetrofitError e)
        {
        }
      }
    }).start();
  }
}
