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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by th on 12.02.2015.
 */
public class HueNotificationView extends RemoteViews
{
  private final List<Integer> buttons = new ArrayList<>(Arrays.asList(R.id.buttonStartStop, R.id.buttonBrighter, R.id.buttonDarker, R.id.buttonConfigure));
  private Context app_context;

  public HueNotificationView(Context _app_context)
  {
    super(_app_context.getPackageName(), R.layout.notification_layout);
    app_context = _app_context;
  }

  void setIntent(Intent _intent)
  {
    for (int i : buttons)
    {
      setOnClickPendingIntent(i, PendingIntent.getBroadcast(app_context, i, new Intent(_intent).putExtra("ID", i), PendingIntent.FLAG_UPDATE_CURRENT));
    }
  }

  public void setText(String text)
  {
    setTextViewText(R.id.textViewNotificationStatus, text);
  }

  public void setBrightnessText(String text)
  {
    setTextViewText(R.id.textViewBrightness, text);

  }


}
