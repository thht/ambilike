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
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import static java.lang.Math.round;

@SuppressLint("Registered")
@EActivity
public class HueConfigureActivity extends ActionBarActivity
{
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
    transitionSettings = new SeekbarSettings(seekBarConfigureTransition, textConfigureTransition, 0, 20, 10, true);
    colorfulnessSettings = new SeekbarSettings(seekBarConfigureColorfulness, textConfigureColorfulness, 0, 2, 1, true);

    minBrightnessSettings = new SeekbarSettings(seekBarConfigureMinBrightness, textConfigureMinBrightness, 0, 255, 0, false);
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

    maxBrightnessSettings = new SeekbarSettings(seekBarConfigureMaxBrightness, textConfigureMaxBrightness, 0, 255, 255, false);
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

  private class SeekbarSettings
  {
    private SeekBar seekbar;
    private TextView textView;
    private double min;
    private double max;
    private double current;
    private Validator validator;
    private boolean usesFloat;

    private SeekbarSettings(SeekBar seekbar, final TextView textView, double min, double max, final double current, boolean usesFloat)
    {
      this.seekbar = seekbar;
      this.textView = textView;
      this.min = min;
      this.max = max;
      this.usesFloat = usesFloat;

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
    }

    public void setValidator(Validator validator)
    {
      this.validator = validator;
    }
  }
}
