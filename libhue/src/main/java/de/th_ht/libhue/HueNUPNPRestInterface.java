package de.th_ht.libhue;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by th on 28.02.2015.
 */
public interface HueNUPNPRestInterface
{
  @GET("/api/nupnp")
  void getDevices(Callback<List<Device>> callback);

  class Device
  {
    String id;
    String internalipaddress;
    String name;

    public String getURL()
    {
      return "http://" + internalipaddress;
    }

    public String getName()
    {
      return name;
    }
  }
}
