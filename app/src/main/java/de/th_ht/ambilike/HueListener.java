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

import de.th_ht.libhue.Hue;
import timber.log.Timber;

/**
 * Created by th on 02.03.2015.
 */
public class HueListener implements de.th_ht.libhue.HueListener
{
  private static int flags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP;
  Context context;
  HuePreferences_ preferences;
  HueController hueController;

  public HueListener(Context context, HuePreferences_ preferences, HueController hueController)
  {
    this.context = context;
    this.preferences = preferences;
    this.hueController = hueController;
  }

  @Override
  public void onNotAuthenticated(Hue hue)
  {
    Timber.d("onNotAuth...");
    hueController.authenticate();
  }

  @Override
  public void onConnectFailed(Hue hue, Exception exception)
  {
    Timber.d("onConnectFailed");
  }

  @Override
  public void onConnect(Hue hue)
  {
    Timber.d("onConnect");
  }

  @Override
  public void onAuthenticated(Hue hue, String username)
  {
    Timber.d("onAuthenticated");
    HueConfigureActivity.dismissAuthenticate(context);
    preferences.edit().HueUsername().put(username).apply();
  }

  @Override
  public void onAuthenticationFailed(Hue hue, String reason, Exception exception)
  {
    Timber.d("Authentication failed");
    HueConfigureActivity.dismissAuthenticate(context);
    HueConfigureActivity.showAuthFailed(context);
  }

  @Override
  public void onConnectionLost(Hue hue, Exception exception)
  {
    Timber.d("onConnectionLost");
  }

  @Override
  public void onConnectionResumed(Hue hue)
  {
    Timber.d("onConnectionResumed");
  }
}
