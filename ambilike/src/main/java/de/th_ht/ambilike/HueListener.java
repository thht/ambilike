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
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.th_ht.libhue.Hue;

/**
 * Created by th on 02.03.2015.
 */
public class HueListener implements de.th_ht.libhue.HueListener
{
  private static int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP;
  Context context;
  HuePreferences_ preferences;
  HueController hueController;
  HueNotification hueNotification;
  ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

  public HueListener(Context context, HuePreferences_ preferences, HueController hueController,
                     HueNotification hueNotification)
  {
    this.context = context;
    this.preferences = preferences;
    this.hueController = hueController;
    this.hueNotification = hueNotification;
    scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  }

  @Override
  public void onNotAuthenticated(Hue hue)
  {
    hueController.authenticate();
  }

  @Override
  public void onConnectFailed(Hue hue, Exception exception)
  {
    hueController.isConnected = false;
    hueNotification.setNotificationText("Connection Failed");
    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(HueConfigureActivity
        .IsConnectedAction));


    scheduledThreadPoolExecutor.getQueue().clear();
    scheduledThreadPoolExecutor.schedule(new Runnable()
    {
      @Override
      public void run()
      {
        hueController.connect();
      }
    }
        , 10, TimeUnit.SECONDS);
  }

  @Override
  public void onConnect(Hue hue)
  {
    hueNotification.setNotificationText("Stopped");
    hueController.isConnected = true;
    hueController.setLights(preferences.Lights().get());
    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(HueConfigureActivity
        .IsConnectedAction));
  }

  @Override
  public void onAuthenticated(Hue hue, String username)
  {
    HueConfigureActivity.dismissAuthenticate(context);
    preferences.edit().HueUsername().put(username).apply();
  }

  @Override
  public void onAuthenticationFailed(Hue hue, String reason, Exception exception)
  {
    HueConfigureActivity.dismissAuthenticate(context);
    HueConfigureActivity.showAuthFailed(context);
  }

  @Override
  public void onConnectionLost(Hue hue, Exception exception)
  {
  }

  @Override
  public void onConnectionResumed(Hue hue)
  {

  }
}
