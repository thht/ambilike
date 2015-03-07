package de.th_ht.libhue;

/**
 * Created by th on 23.02.2015.
 */
public class HueLight extends HueLightObject
{
  protected HueRestInterface.LightState state;
  private int id;

  protected HueLight(HueRestInterface.LightState state, int id)
  {
    super();
    this.state = state;
    this.id = id;
  }

  @Override
  public void postUpdate()
  {
    postUpdate(true);
  }

  public void postUpdate(boolean doUpdate)
  {
    bridge.postUpdate(id, curUpdate, doUpdate);
  }


  public boolean isOn()
  {
    return state.state.on;

  }

  public String getName()
  {
    return state.name;
  }

  protected void setUpdate(HueRestInterface.LightUpdate update)
  {
    this.curUpdate = update;
  }

}
