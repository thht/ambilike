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
import android.content.Context;
import android.content.Intent;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by th on 15.02.2015.
 */
@EBean(scope = EBean.Scope.Singleton)
public class HueNotification
{
  @RootContext
  protected Context context;
  @SystemService
  protected NotificationManager n_mgr;
  private HueNotificationView n_view;
  private Runnable startStopListener;
  private Runnable configureListener;
  private Runnable brightnessChangedListener;
  private int notificationIcon;
  private int notificationID;
  private HashMap<Integer, Runnable> listenerMap = new HashMap<>();
  private int brightness;
  private int maxBrightness;
  private int minBrightness;
  private int brightnessStep;
  private Notification notification;

  @AfterInject
  public void AfterInject()
  {
    n_view = new HueNotificationView(context);

    Timber.d("Creating HueNotification");
    notificationIcon = R.drawable.app_icon;
    notificationID = R.integer.hue_notification;
    brightness = 100;
    brightnessStep = 5;
    maxBrightness = 150;
    minBrightness = 0;

    Intent notificationIntent = new Intent(context, HueNotificationReceiver_.class);
    n_view.setIntent(notificationIntent);

    notification = new Notification.Builder(context)
        .setContent(n_view)
        .setSmallIcon(notificationIcon)
        .setOngoing(true)
        .setPriority(Notification.PRIORITY_MAX)
        .build();


    setBrightness(brightness);
  }

  public void updateNotification()
  {
    notification.contentView = n_view;

    n_mgr.notify(notificationID, notification);
  }

  public void cancelNotification()
  {
    n_mgr.cancel(notificationID);

  }

  public void intentReceived(Intent intent)
  {
    fillListenerMap();

    int id = intent.getIntExtra("ID", 0);

    for (int key : listenerMap.keySet())
    {
      if (key == id)
      {
        ListenerRun(listenerMap.get(key));
        return;
      }
    }

    switch (id)
    {
      case R.id.buttonDarker:
        darker();
        break;

      case R.id.buttonBrighter:
        brighter();
        break;
    }
  }

  @UiThread
  protected void ListenerRun(Runnable listener)
  {
    if (listener != null)
      listener.run();

  }

  private void fillListenerMap()
  {
    listenerMap.clear();
    listenerMap.put(R.id.buttonStartStop, startStopListener);
    listenerMap.put(R.id.buttonConfigure, configureListener);
  }

  public void darker()
  {
    setBrightness(brightness - brightnessStep);
  }

  public void brighter()
  {
    setBrightness(brightness + brightnessStep);
  }

  public void setStartStopListener(Runnable startStopListener)
  {
    this.startStopListener = startStopListener;
  }

  public void setConfigureListener(Runnable configureListener)
  {
    this.configureListener = configureListener;
  }

  public void setBrightnessChangedListener(Runnable brightnessChangedListener)
  {
    this.brightnessChangedListener = brightnessChangedListener;
  }

  public void setNotificationText(String text)
  {
    n_view.setText(text);
    updateNotification();

  }

  public int getBrightness()
  {
    return brightness;
  }

  public void setBrightness(int brightness)
  {
    if (brightness >= minBrightness && brightness <= maxBrightness)
    {
      this.brightness = brightness;
      n_view.setBrightnessText(brightness + "%");
      updateNotification();

      ListenerRun(brightnessChangedListener);
    }
  }

  public int getMaxBrightness()
  {
    return maxBrightness;
  }

  public void setMaxBrightness(int maxBrightness)
  {
    if (maxBrightness >= 0)
      this.maxBrightness = maxBrightness;
  }

  public int getMinBrightness()
  {
    return minBrightness;
  }

  public void setMinBrightness(int minBrightness)
  {
    if (minBrightness >= 0)
      this.minBrightness = minBrightness;
  }

  public int getBrightnessStep()
  {
    return brightnessStep;
  }

  public void setBrightnessStep(int brightnessStep)
  {
    if (brightnessStep >= 0)
      this.brightnessStep = brightnessStep;
  }

  public Notification getNotification()
  {
    return notification;
  }
}
