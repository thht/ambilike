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

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.stericson.RootShell.RootShell;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends ActionBarActivity
{
  private static String myFilesDir;
  private static Hue hue;
  private static DisplayMetrics metric;
  private Button buttonConnect;
  private Button buttonStart;
  private Button buttonStop;
  private NumberPicker numberTransition;
  private NumberPicker numberColorExp;
  private NumberPicker numberBriExp;
  private NumberPicker numberMinBri;
  private NumberPicker numberMaxBri;
  private HueReceiver huereceiver;

  static public String getMyFilesDir()
  {
    return myFilesDir;
  }

  public static Hue getHue()
  {
    return hue;
  }

  public static DisplayMetrics getMetric()
  {
    return metric;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    hue = new Hue(this, getApplicationContext());
    List<Integer> allLights = Arrays.asList(1, 2);
    hue.setLights(allLights);


    myFilesDir = getExternalCacheDir().toString();
    getExternalCacheDir().mkdirs();

    buttonConnect = (Button) findViewById(R.id.buttonConnect);
    buttonStart = (Button) findViewById(R.id.buttonStart);
    buttonStop = (Button) findViewById(R.id.buttonStop);

    numberBriExp = (NumberPicker) findViewById(R.id.numberPickerBriExp);
    numberBriExp.setMinValue(0);
    numberBriExp.setMaxValue(100);

    numberColorExp = (NumberPicker) findViewById(R.id.numberPickerColorExp);
    numberColorExp.setMinValue(0);
    numberColorExp.setMaxValue(100);

    numberMaxBri = (NumberPicker) findViewById(R.id.numberPickerMaxBri);
    numberMaxBri.setMinValue(0);
    numberMaxBri.setMaxValue(25);

    numberMinBri = (NumberPicker) findViewById(R.id.numberPickerMinBri);
    numberMinBri.setMinValue(0);
    numberMinBri.setMaxValue(25);

    numberTransition = (NumberPicker) findViewById(R.id.numberPickerTransition);
    numberTransition.setMinValue(0);
    numberTransition.setMaxValue(100);

    Toast.makeText(getApplicationContext(), "trying to get root", Toast.LENGTH_SHORT).show();
    if (!RootShell.isAccessGiven())
      Toast.makeText(getApplicationContext(), "no root...", Toast.LENGTH_SHORT).show();
    else
      Toast.makeText(getApplicationContext(), "root acquired", Toast.LENGTH_SHORT).show();

    metric = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(metric);
    SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt("DisplayWidth", metric.widthPixels);
    editor.putInt("DisplayHeight", metric.heightPixels);
    editor.putString("MyFilesDir", myFilesDir);
    editor.commit();

    numberColorExp.setValue(settings.getInt("ColorExp", 10));
    numberBriExp.setValue(settings.getInt("BriExp", 10));
    numberTransition.setValue(settings.getInt("Transition", 10));
    numberMaxBri.setValue(settings.getInt("MaxBri", 25));
    numberMinBri.setValue(settings.getInt("MinBri", 0));

    hue.setTransition(numberTransition.getValue() * 10);
    hue.setBriExp((float) (numberBriExp.getValue()) / 10);
    hue.setColorExp((float) (numberColorExp.getValue()) / 10);
    hue.setMaxBri(numberMaxBri.getValue() * 10);
    hue.setMinBri(numberMinBri.getValue() * 10);

    buttonConnect.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        hue.connect();
      }
    });

    buttonStart.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        Intent intent = new Intent(getApplicationContext(), HueService.class);
        Bundle bundle = new Bundle();
        bundle.putInt("displaywidth", metric.widthPixels);
        bundle.putInt("displayheight", metric.heightPixels);
        intent.putExtra("Stuff", bundle);

        getApplicationContext().startService(intent);
        System.out.println("buttonStart.OnClick exit...");
      }
    });

    buttonStop.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View view)
      {
        getApplicationContext().stopService(new Intent(getApplicationContext(), HueService.class));
      }
    });

    numberColorExp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
    {
      @Override
      public void onValueChange(NumberPicker numberPicker, int i, int i2)
      {
        hue.setColorExp((float) (i2) / 10);
      }
    });

    numberBriExp.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
    {
      @Override
      public void onValueChange(NumberPicker numberPicker, int i, int i2)
      {
        hue.setBriExp((float) (i2) / 10);
      }
    });

    numberTransition.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
    {
      @Override
      public void onValueChange(NumberPicker numberPicker, int i, int i2)
      {
        hue.setTransition(i2 * 10);
      }
    });

    numberMinBri.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
    {
      @Override
      public void onValueChange(NumberPicker numberPicker, int i, int i2)
      {
        if (i2 >= numberMaxBri.getValue())
          numberMinBri.setValue(i);
        else
          hue.setMinBri(i2 * 10);
      }
    });

    numberMaxBri.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
    {
      @Override
      public void onValueChange(NumberPicker numberPicker, int i, int i2)
      {
        if (i2 <= numberMinBri.getValue())
          numberMaxBri.setValue(i);
        else
          hue.setMaxBri(i2 * 10);
      }
    });
  }

  public void connected(boolean isconnected)
  {
    buttonStart.setEnabled(isconnected);
    buttonStop.setEnabled(isconnected);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings)
    {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  public void doSnap()
  {

  }

  @Override
  public void onResume()
  {
    super.onResume();
    if (huereceiver != null)
    {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(huereceiver);
    }
    huereceiver = new HueReceiver();
    LocalBroadcastManager.getInstance(this).registerReceiver(huereceiver, new IntentFilter("com.example.th.testscreenshot01.updateHue"));
  }

  @Override
  public void onRestart()
  {
    super.onRestart();
    hue.connect();
  }

  @Override
  public void onPause()
  {
    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(new HueReceiver());
  }

  @Override
  public void onStop()
  {
    super.onStop();
    SharedPreferences settings = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putInt("ColorExp", numberColorExp.getValue());
    editor.putInt("BriExp", numberBriExp.getValue());
    editor.putInt("Transition", numberTransition.getValue());
    editor.putInt("MaxBri", numberMaxBri.getValue());
    editor.putInt("MinBri", numberMinBri.getValue());
    editor.commit();

    numberColorExp.setValue(settings.getInt("ColorExp", 10));
    numberBriExp.setValue(settings.getInt("BriExp", 10));
    numberTransition.setValue(settings.getInt("Transition", 10));
    numberMaxBri.setValue(settings.getInt("MaxBri", 255));
    numberMinBri.setValue(settings.getInt("MinBri", 0));
  }
}
