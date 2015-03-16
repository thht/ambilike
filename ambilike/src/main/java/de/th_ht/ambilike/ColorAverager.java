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

package de.th_ht.ambilike;

import java.util.LinkedList;

public class ColorAverager
{
  private final int capacity = 3;
  private LinkedList<Integer> red;
  private LinkedList<Integer> green;
  private LinkedList<Integer> blue;
  private LinkedList<Integer> brightness;

  public ColorAverager()
  {
    red = new LinkedList<Integer>();
    green = new LinkedList<Integer>();
    blue = new LinkedList<Integer>();
    brightness = new LinkedList<Integer>();
  }

  public synchronized void put(int red, int green, int blue, int brightness)
  {
    this.red.add(red);
    this.green.add(green);
    this.blue.add(blue);
    this.brightness.add(brightness);

    while (this.red.size() > capacity)
    {
      this.red.poll();
      this.green.poll();
      this.blue.poll();
      this.brightness.poll();
    }
  }

  public synchronized void clear()
  {
    red.clear();
    green.clear();
    blue.clear();
    brightness.clear();
  }

  public synchronized int red()
  {
    float tmp = 0;
    for (int i : red)
    {
      tmp = tmp + i;
    }
    return (int) (tmp / red.size());
  }

  public synchronized int green()
  {
    float tmp = 0;
    for (int i : green)
    {
      tmp = tmp + i;
    }
    return (int) (tmp / green.size());
  }

  public synchronized int blue()
  {
    float tmp = 0;
    for (int i : blue)
    {
      tmp = tmp + i;
    }
    return (int) (tmp / blue.size());
  }

  public synchronized int brightness()
  {
    float tmp = 0;
    for (int i : brightness)
    {
      tmp = tmp + i;
    }
    return (int) (tmp / brightness.size());
  }

  public synchronized int size()
  {
    return red.size();
  }
}
