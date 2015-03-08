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

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceImpl;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;

import java.util.ArrayList;
import java.util.List;

import de.th_ht.libhue.Errors.DiscoverException;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class HueDiscover
{
  private static final List<Device> devices = new ArrayList<>(100);
  private static HueDiscoverListener listener;
  private static Thread discoverThread = null;

  public static void discover(HueDiscoverListener _listener, final int timeout) throws
      DiscoverException
  {
    if (discoverThread != null && discoverThread.isAlive())
    {
      throw new DiscoverException("Discovery already running");
    }

    HueDiscover.listener = _listener;

    discoverThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        // This will create necessary network resources for UPnP right away
        devices.clear();
        UpnpService upnpService = new UpnpServiceImpl(new AndroidUpnpServiceConfiguration());
        upnpService.getRegistry().addListener(new UPNPListener());

        // Send a search message to all devices and services, they should respond soon
        upnpService.getControlPoint().search(new STAllHeader());

        // Let's wait 10 seconds for them to respond
        try
        {
          Thread.sleep(timeout);
        }
        catch (Exception e)
        {
          upnpService.shutdown();
          return;
        }

        // Release all resources and advertise BYEBYE to other UPnP devices
        upnpService.shutdown();
        HueDiscover.listener.devicesFound(HueDiscover.devices);
      }
    });

    discoverThread.start();
  }

  public static void cancel()
  {
    if (discoverThread != null && discoverThread.isAlive())
    {
      discoverThread.interrupt();
    }
  }

  public static void discoverNUPNP(final HueDiscoverListener listener)
  {
    Callback<List<HueNUPNPRestInterface.Device>> callback = new
        Callback<List<HueNUPNPRestInterface.Device>>()
    {
      @Override
      public void success(List<HueNUPNPRestInterface.Device> devices, Response response)
      {
        for (HueNUPNPRestInterface.Device dev : devices)
        {
          HueDiscover.devices.add(new Device(dev.getURL(), dev.getName()));
        }

        listener.devicesFound(HueDiscover.devices);
      }

      @Override
      public void failure(RetrofitError error)
      {

      }
    };

    devices.clear();
    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint("https://www.meethue.com")
        .setClient(new HueConnectionClient())
            //.setLogLevel(RestAdapter.LogLevel.FULL)
        .build();
    HueNUPNPRestInterface hueNUPNPRestInterface = restAdapter.create(HueNUPNPRestInterface.class);

    hueNUPNPRestInterface.getDevices(callback);
  }

  public interface HueDiscoverListener
  {
    void devicesFound(List<Device> devices);
  }


  public static class Device
  {
    public final String url;
    public final String name;

    public Device(String url, String name)
    {
      this.url = url;
      this.name = name;
    }
  }

  private static class UPNPListener implements RegistryListener
  {
    public void remoteDeviceDiscoveryStarted(Registry registry,
                                             RemoteDevice device)
    {
    }

    public void remoteDeviceDiscoveryFailed(Registry registry,
                                            RemoteDevice device,
                                            Exception ex)
    {
    }

    public void remoteDeviceAdded(Registry registry, RemoteDevice device)
    {
      if (device.getDetails().getModelDetails().getModelName().contains("hue bridge"))
      {
        devices.add(new Device(device.getDetails().getBaseURL().toString(),
            device.getDetails().getFriendlyName()));
      }
    }

    public void remoteDeviceUpdated(Registry registry, RemoteDevice device)
    {
    }

    public void remoteDeviceRemoved(Registry registry, RemoteDevice device)
    {
    }

    public void localDeviceAdded(Registry registry, LocalDevice device)
    {
    }

    public void localDeviceRemoved(Registry registry, LocalDevice device)
    {
    }

    public void beforeShutdown(Registry registry)
    {
    }

    public void afterShutdown()
    {
    }
  }
}
