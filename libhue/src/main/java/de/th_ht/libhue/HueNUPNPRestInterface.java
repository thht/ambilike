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

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;


interface HueNUPNPRestInterface
{
  @GET("/api/nupnp")
  void getDevices(Callback<List<Device>> callback);

  class Device
  {
    String id;
    String internalipaddress;
    String name;

    public String getURL()
    {
      return "http://" + internalipaddress;
    }

    public String getName()
    {
      return name;
    }
  }
}
