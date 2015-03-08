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

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;


@SuppressLint("Registered")
@EService
public class HueService extends Service
{
  private final IBinder mBinder = new LocalBinder();

  @SystemService
  NotificationManager n_mgr;

  @Bean
  HueNotification hueNotification;

  @Bean
  HueController hueController;

  @Pref
  HuePreferences_ preferences;

  @SystemService
  WindowManager windowManager;

  private HueThread hueThread;

  public static void terminate(Context context)
  {
    HueService_.intent(context).stop();
    System.exit(0);
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    //preferences.clear();

    hueNotification.setNotificationText("Initialising");
    hueNotification.setBrightness(preferences.Brightness().get());

    hueNotification.setStartStopListener(new Runnable()
    {
      @Override
      public void run()
      {
        synchronized (HueService.class)
        {
          if (hueThread == null && hueController.isConnected())
          {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            hueThread = new HueThread(metrics.widthPixels, metrics.heightPixels,
                getApplicationContext(), hueController);
            hueThread.start();
            hueNotification.setNotificationText("Running");
          }
          else
          {
            if (hueThread != null)
            {
              hueThread.terminate();
            }
            hueThread = null;
            if (hueController.isConnected)
            {
              hueNotification.setNotificationText("Stopped");
            }
            else
            {
              hueNotification.setNotificationText("Connection Failed");
            }
          }
        }
      }
    });

    hueNotification.setConfigureListener(new Runnable()
    {
      @Override
      public void run()
      {
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(it);
        startActivity(new Intent(getApplicationContext(), HueConfigureActivity_.class).setFlags
            (Intent.FLAG_ACTIVITY_NEW_TASK));
      }
    });

    hueNotification.setBrightnessChangedListener(new Runnable()
    {
      @Override
      public void run()
      {
        hueController.setBriMult(hueNotification.getBrightness());
        preferences.edit().Brightness().put(hueNotification.getBrightness()).apply();
      }
    });
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    super.onStartCommand(intent, flags, startId);
    startForeground(R.integer.hue_notification, hueNotification.getNotification());

    connect();

    return START_STICKY;
  }

  void connect()
  {
    hueController.testRoot();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return mBinder;
  }

  private class LocalBinder extends Binder
  {
  }
}
