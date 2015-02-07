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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

public class HueService extends Service
{
  private final IBinder mBinder = new LocalBinder();
  private Screenshot screenshot;
  private HueThread huethread;
  private int displaywidth, displayheight;
  private NotificationManager mNM;
  private int NOTIFICATION = 0;

  public HueService()
  {
  }

  @Override
  public void onCreate()
  {
    mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    showNotification();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    Toast.makeText(getApplicationContext(), "Service started...", Toast.LENGTH_SHORT).show();


    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    displaywidth = settings.getInt("DisplayWidth", 0);
    displayheight = settings.getInt("DisplayHeight", 0);

    screenshot = new Screenshot(displaywidth, displayheight, getApplicationContext());

    huethread = new HueThread();
    new Thread(huethread).start();

    return START_STICKY;
  }

  @Override
  public void onDestroy()
  {
    Toast.makeText(getApplicationContext(), "Service killed...", Toast.LENGTH_SHORT).show();
    mNM.cancel(NOTIFICATION);
    huethread.kill();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
    return mBinder;
  }

  private void showNotification()
  {
    // In this sample, we'll use the same text for the ticker and the expanded notification
    CharSequence text = "Hueservice started";
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT);

    // Set the icon, scrolling text and timestamp
    Notification notification = new Notification.Builder(getApplicationContext())
        .setContentTitle("HueService")
        .setContentText("HueService started")
        .setContentIntent(contentIntent)
        .setOngoing(true)
        .setSmallIcon(R.drawable.app_icon)
        .build();


    // Send the notification.
    mNM.notify(NOTIFICATION, notification);
  }

  private class HueThread implements Runnable
  {
    private boolean killed;

    @Override
    public void run()
    {
      killed = false;
      boolean first = true;
      boolean good = true;
      while (!killed)
      {
        if (first)
        {
          good = screenshot.snap();
        }
        if (good)
        {
          int clr = screenshot.getDominantColor();
          int[] rgb = new int[]{Color.red(clr), Color.green(clr), Color.blue(clr)};
          float[] hsv = new float[3];
          Color.colorToHSV(clr, hsv);

          Intent intent = new Intent("com.example.th.testscreenshot01.updateHue");
          Bundle bundle = new Bundle();
          bundle.putIntArray("rgb", rgb);
          bundle.putInt("bri", (int) (hsv[2] * 255));
          intent.putExtra("Stuff", bundle);
          LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }
      }
    }

    public void kill()
    {
      killed = true;
    }
  }

  public class LocalBinder extends Binder
  {
    HueService getService()
    {
      return HueService.this;
    }
  }
}
