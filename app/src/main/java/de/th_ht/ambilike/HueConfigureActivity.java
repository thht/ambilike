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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.widget.SeekBar;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import static java.lang.Math.round;

@SuppressLint("Registered")
@EActivity
public class HueConfigureActivity extends ActionBarActivity
{
  @Pref
  HuePreferences_ preferences;

  @Bean
  HueController hueController;
  
  @ViewById
  SeekBar seekBarConfigureTransition;
  @ViewById
  SeekBar seekBarConfigureColorfulness;
  @ViewById
  SeekBar seekBarConfigureMinBrightness;
  @ViewById
  SeekBar seekBarConfigureMaxBrightness;

  @ViewById
  TextView textConfigureTransition;
  @ViewById
  TextView textConfigureColorfulness;
  @ViewById
  TextView textConfigureMinBrightness;
  @ViewById
  TextView textConfigureMaxBrightness;

  SeekbarSettings transitionSettings, colorfulnessSettings, minBrightnessSettings, maxBrightnessSettings;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_hue_configure);
  }

  @AfterViews
  protected void init()
  {
    transitionSettings = new SeekbarSettings(seekBarConfigureTransition, textConfigureTransition, 0, 5, preferences.Transitiontime().get(), true, new OnCustomSeekbarChangedListener()
    {
      @Override
      public void onChanged(double value)
      {
        hueController.setTransition((int) (value * 1000));
        preferences.edit().Transitiontime().put((float) value).apply();
      }
    });

    colorfulnessSettings = new SeekbarSettings(seekBarConfigureColorfulness, textConfigureColorfulness, 0, 2, preferences.Colorfullness().get(), true, new OnCustomSeekbarChangedListener()
    {
      @Override
      public void onChanged(double value)
      {
        hueController.setColorExp((float) value);
        preferences.edit().Colorfullness().put((float) value).apply();
      }
    });

    minBrightnessSettings = new SeekbarSettings(seekBarConfigureMinBrightness, textConfigureMinBrightness, 0, 255, preferences.MinBrightness().get(), false, new OnCustomSeekbarChangedListener()
    {
      @Override
      public void onChanged(double value)
      {
        hueController.setMinBri((int) value);
        preferences.edit().MinBrightness().put((int) value).apply();
      }
    });
    minBrightnessSettings.setValidator(new Validator()
    {
      @Override
      public boolean validate(double value)
      {
        if (maxBrightnessSettings != null && maxBrightnessSettings.getCurrent() <= value)
          return false;

        return true;
      }
    });

    maxBrightnessSettings = new SeekbarSettings(seekBarConfigureMaxBrightness, textConfigureMaxBrightness, 0, 255, preferences.MaxBrightness().get(), false, new OnCustomSeekbarChangedListener()
    {
      @Override
      public void onChanged(double value)
      {
        hueController.setMaxBri((int) value);
        preferences.edit().MaxBrightness().put((int) value).apply();
      }
    });
    maxBrightnessSettings.setValidator(new Validator()
    {
      @Override
      public boolean validate(double value)
      {
        if (minBrightnessSettings != null && minBrightnessSettings.getCurrent() >= value)
          return false;

        return true;
      }
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    // Inflate the menu; this adds items to the action bar if it is present.
    return true;
  }

  private interface Validator
  {
    public abstract boolean validate(double value);
  }

  public interface OnCustomSeekbarChangedListener
  {
    void onChanged(double value);

  }

  private class SeekbarSettings
  {
    private SeekBar seekbar;
    private TextView textView;
    private double min;
    private double max;
    private double current;
    private Validator validator;
    private boolean usesFloat;
    private OnCustomSeekbarChangedListener listener;

    private SeekbarSettings(SeekBar seekbar, final TextView textView, double min, double max, final double current, boolean usesFloat, OnCustomSeekbarChangedListener listener)
    {
      this.seekbar = seekbar;
      this.textView = textView;
      this.min = min;
      this.max = max;
      this.usesFloat = usesFloat;
      this.listener = listener;

      setCurrent(current);

      seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
      {
        @Override
        public void onProgressChanged(SeekBar seekBar, int newValue, boolean fromUser)
        {
          setCurrent(progress2double(newValue), true);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {

        }
      });
    }

    protected int double2progress(double val)
    {
      int seekmax = seekbar.getMax();
      double prog_range = seekmax;
      double orig_range = max - min;
      double factor = prog_range / orig_range;
      double offset = -min;

      return (int) ((val + offset) * factor);
    }

    protected double progress2double(int progress)
    {
      int seekmax = seekbar.getMax();
      double prog_range = seekmax;
      double orig_range = max - min;
      double factor = prog_range / orig_range;
      double offset = -min;

      return (progress / factor) - offset;

    }

    public double getCurrent()
    {
      return current;
    }

    public void setCurrent(double current)
    {
      setCurrent(current, true);
    }

    public void setCurrent(double current, boolean update)
    {
      if (!usesFloat)
        current = round(current);

      if (!validate(current))
      {
        update();
        return;
      }

      this.current = current;

      if (update)
        update();
    }

    protected boolean validate(double value)
    {
      if (current > max || current < min)
        return false;

      if (validator != null && !validator.validate(value))
        return false;

      return true;

    }

    protected void update()
    {
      seekbar.setProgress(double2progress(current));

      String textString;

      if (usesFloat)
      {
        textString = String.format("%.1f", current);
      } else
      {
        textString = String.format("%d", (int) current);
      }

      textView.setText(textString);

      listener.onChanged(current);
    }

    public void setValidator(Validator validator)
    {
      this.validator = validator;
    }
  }
}
