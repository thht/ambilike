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
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHBridgeSearchManager;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResourcesCache;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.UUID;

public class Hue
{
  private PHHueSDK phHueSDK;
  private PHBridge bridge;
  private PHBridgeResourcesCache cache;
  private List<Integer> lights;
  private int transition;
  private float colorExp;
  private float briExp;
  private int minBri;
  private int maxBri;
  private String username;

  private boolean connected;
  private PHSDKListener listener = new PHSDKListener()
  {

    @Override
    public void onAccessPointsFound(List accessPoint)
    {
      // Handle your bridge search results here.  Typically if multiple results are returned you will want to display them in a list
      // and let the user select their bridge.   If one is found you may opt to connect automatically to that bridge.
      doToast("Bridge found", Toast.LENGTH_SHORT);
      PHAccessPoint ap = (PHAccessPoint) accessPoint.get(0);
      ap.setUsername(username);
      if (!phHueSDK.isAccessPointConnected(ap))
      {
        phHueSDK.connect(ap);
      } else
      {
        mainwin.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {
            mainwin.connected(connected);
          }
        });
      }
    }

    @Override
    public void onCacheUpdated(List cacheNotificationsList, PHBridge bridge)
    {
      // Here you receive notifications that the BridgeResource Cache was updated. Use the PHMessageType to
      // check which cache was updated, e.g.
      //System.out.println("onCacheUpdate");
      if (cacheNotificationsList.contains(PHMessageType.LIGHTS_CACHE_UPDATED))
      {
        //System.out.println("Lights Cache Updated ");
      }
    }

    @Override
    public void onBridgeConnected(PHBridge b)
    {
      phHueSDK.setSelectedBridge(b);
      phHueSDK.enableHeartbeat(b, PHHueSDK.HB_INTERVAL);
      bridge = b;
      cache = b.getResourceCache();
      doToast("Bridge connected", Toast.LENGTH_SHORT);
      connected = true;

      mainwin.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          mainwin.connected(connected);
        }
      });


      // Here it is recommended to set your connected bridge in your sdk object (as above) and start the heartbeat.
      // At this point you are connected to a bridge so you should pass control to your main program/activity.
      // Also it is recommended you store the connected IP Address/ Username in your app here.  This will allow easy automatic connection on subsequent use.
    }

    @Override
    public void onAuthenticationRequired(PHAccessPoint accessPoint)
    {
      doToast("Please press the button on the Bridge", Toast.LENGTH_LONG);
      phHueSDK.startPushlinkAuthentication(accessPoint);
    }

    @Override
    public void onConnectionResumed(PHBridge bridge)
    {
      System.out.println("onConnectionResme");
    }

    @Override
    public void onConnectionLost(PHAccessPoint accessPoint)
    {
      // Here you would handle the loss of connection to your bridge.
      System.out.println("onConnectionLost");
    }

    @Override
    public void onError(int code, final String message)
    {
      // Here you can handle events such as Bridge Not Responding, Authentication Failed and Bridge Not Found
      doToast("Hue Error: " + message, Toast.LENGTH_SHORT);
    }

    @Override
    public void onParsingErrors(List parsingErrorsList)
    {
      // Any JSON parsing errors are returned here.  Typically your program should never return these.
      System.out.println("onParsingError");
    }

    private void doToast(final String text, final int length)
    {
      mainwin.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          Toast.makeText(appContext, text, length).show();
        }
      });
    }
  };
  private MainActivity mainwin;
  private Context appContext;

  public Hue(MainActivity _mainwin, Context _appContext)
  {
    System.out.println("In Hue Constructur...");
    connected = false;
    mainwin = _mainwin;
    appContext = _appContext;
    phHueSDK = PHHueSDK.getInstance();
    phHueSDK.getNotificationManager().registerSDKListener(listener);

    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(appContext);
    username = settings.getString("HueUser", UUID.randomUUID().toString());
    SharedPreferences.Editor editor = settings.edit();
    editor.putString("HueUser", username);
    editor.commit();
    mainwin.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        mainwin.connected(connected);
      }
    });
  }

  public void connect()
  {
    if (connected)
    {
      Toast.makeText(appContext, "Already connected", Toast.LENGTH_LONG).show();
      mainwin.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          mainwin.connected(connected);
        }
      });
      return;
    }
    Toast.makeText(appContext, "Connecting to bridge", Toast.LENGTH_LONG).show();

    PHBridgeSearchManager sm = (PHBridgeSearchManager) phHueSDK.getSDKService(PHHueSDK.SEARCH_BRIDGE);
    sm.search(true, true);

  }

  public void setTransition(int _transition)
  {
    if (_transition < 5)
      _transition = 5;
    transition = _transition;
  }

  public void setColorExp(float _colorexp)
  {
    colorExp = _colorexp;
  }

  public void setBriExp(float _briexp)
  {
    briExp = _briexp;
  }

  public void setMinBri(int _minBri)
  {
    minBri = _minBri;
  }

  public void setMaxBri(int _maxBri)
  {
    maxBri = _maxBri;
  }

  public void setColor(int[] rgb, int brightness)
  {
    try
    {
      // Convert Values...
      brightness = (int) Math.pow(brightness, briExp);
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


      List hueLights = bridge.getResourceCache().getAllLights();
      PHLightState newstate = new PHLightState();
      newstate.setBrightness(brightness);
      newstate.setTransitionTime(transition);

      //System.out.println("Setting lights to " + rgb[0] + " " + rgb[1] + " " + rgb[2] + " bri: " + brightness);
      ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
      List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
      boolean isNetflix = false;
      //if(taskInfo.get(0).topActivity.getClassName().equals("com.netflix.mediaclient.ui.player.PlayerActivity"))
      if (taskInfo.get(0).topActivity.getClassName().contains(".netflix."))
        isNetflix = true;

      if (isNetflix)
      {
        int tmp = rgb[0];
        rgb[0] = rgb[2];
        rgb[2] = tmp;
      }

      for (int i : lights)
      {
        PHLight light = (PHLight) hueLights.get(i);
        //System.out.println("Setting lights:" + rgb[0] + " " + rgb[1] + " " + rgb[2]);
        float[] xy = PHUtilities.calculateXYFromRGB(rgb[0], rgb[1], rgb[2], light.getModelNumber());
        newstate.setX(xy[0]);
        newstate.setY(xy[1]);

        bridge.updateLightState(light, newstate);
      }
    } catch (NullPointerException e)
    {
      System.out.println("Hue class does not seem to be ready....");
    }
  }

  public List<PHLight> getLights()
  {
    List<PHLight> list = cache.getAllLights();

    return list;
  }

  public void setLights(List<Integer> _lights)
  {
    lights = _lights;
  }
}
