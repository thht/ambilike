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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;

import timber.log.Timber;

/**
 * Created by th on 17.02.2015.
 */

@EService
public class HueNotificationService extends Service
{
  private final IBinder mBinder = new LocalBinder();

  @SystemService
  NotificationManager n_mgr;

  @Bean
  HueNotification hueNotification;

  @Override
  public void onCreate()
  {
    super.onCreate();
    hueNotification.setNotificationText("Started");

    hueNotification.setStartStopListener(new Runnable()
    {
      @Override
      public void run()
      {
        Timber.d("Start/Stop Service");
      }
    });

    hueNotification.setConfigureListener(new Runnable()
    {
      @Override
      public void run()
      {
        Timber.d("Configure");
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        getApplicationContext().sendBroadcast(it);
        startActivity(new Intent(getApplicationContext(), HueConfigureActivity_.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
    });

    hueNotification.setBrightnessChangedListener(new Runnable()
    {
      @Override
      public void run()
      {
        Timber.d("Brightness changed to " + hueNotification.getBrightness() + "%");
      }
    });
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    super.onStartCommand(intent, flags, startId);
    Timber.d("HueNotificationservice started...");
    startForeground(R.integer.hue_notification, hueNotification.getNotification());
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return mBinder;
  }

  public class LocalBinder extends Binder
  {
  }
}
