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

/**
 * Created by th on 28.02.2015.
 */
public interface HueListener
{
  public void onNotAuthenticated(Hue hue);

  public void onConnectFailed(Hue hue, Exception exception);

  public void onConnect(Hue hue);

  public void onAuthenticated(Hue hue, String username);

  public void onAuthenticationFailed(Hue hue, String reason, Exception exception);

  public void onConnectionLost(Hue hue, Exception exception);

  public void onConnectionResumed(Hue hue);
}
