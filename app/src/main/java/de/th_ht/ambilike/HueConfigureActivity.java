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
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;

import de.th_ht.libhue.Errors.DiscoverException;
import de.th_ht.libhue.HueDiscover;
import timber.log.Timber;

import static java.lang.Math.round;

@SuppressLint("Registered")
@EActivity
public class HueConfigureActivity extends ActionBarActivity
{
  static final int CALL_FIND_BRIDGE = 1;
  static final int CALL_AUTHENTICATE = 2;
  static final int DISMISS_AUTHENTICATE = 3;
  static final String EXTRA_INTENT = "ConfigActivityExtra";
  static final String DiscoverDlgFragmentTag = "DiscoverDialog";
  static final String AuthenticateDlgFragmentTag = "AuthDialog";
  static final String IntentAction = "HueConfigIntent";
  protected static int discoveryRun = 1;
  @Bean
  static
  HueController hueController;
  protected final int maxDiscoveryRuns = 4;
  @Pref
  HuePreferences_ preferences;

  FragmentManager fragmentManager = getFragmentManager();
  
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
  protected void onResume()
  {
    super.onResume();
    Timber.d("onResume");
    handleIntent(getIntent());
  }

  @Override
  protected void onNewIntent(Intent intent)
  {
    super.onNewIntent(intent);
    Timber.d("onNewIntent");
    setIntent(intent);
    handleIntent(intent);
  }

  void handleIntent(Intent intent)
  {
    Timber.d("handleIntent");
    switch (intent.getIntExtra(EXTRA_INTENT, 0))
    {
      case CALL_FIND_BRIDGE:
        discoveryRun = 1;
        findBridge();
        break;

      case CALL_AUTHENTICATE:
        new AuthenticateDialogFragment().show(fragmentManager, AuthenticateDlgFragmentTag);
        break;

      case DISMISS_AUTHENTICATE:
        Timber.d("Dismissing autenticate...");
        AuthenticateDialogFragment dlg = (AuthenticateDialogFragment) fragmentManager.findFragmentByTag(AuthenticateDlgFragmentTag);
        if (dlg != null)
          dlg.dismiss();
        break;
    }

    setIntent(intent.putExtra(EXTRA_INTENT, 0));
    super.onNewIntent(intent);
  }

  void authenticate()
  {
    Timber.d("Authenticate");

  }

  void findBridge()
  {
    Timber.d("Find Bridge...");
    final DiscoverDialogFragment dlg = new DiscoverDialogFragment();
    HueDiscover.HueDiscoverListener listener = new HueDiscover.HueDiscoverListener()
    {
      @Override
      public void devicesFound(List<HueDiscover.Device> devices)
      {
        Timber.d("Found " + devices.size() + " devices");
        if (devices.size() > 0)
        {
          DiscoverDialogFragment dlg = (DiscoverDialogFragment) fragmentManager.findFragmentByTag(DiscoverDlgFragmentTag);
          if (dlg != null)
          {
            dlg.dismiss();
          }

          Timber.d("Device has url: " + devices.get(0).url);

          preferences.edit().HueURL().put(devices.get(0).url).apply();
          hueController.connect();
        } else
        {
          if (discoveryRun == 1)
          {
            HueDiscover.discoverNUPNP(this);
            discoveryRun++;
          } else
          {
            DiscoverDialogFragment dlg = (DiscoverDialogFragment) fragmentManager.findFragmentByTag(DiscoverDlgFragmentTag);
            if (dlg != null)
            {
              dlg.dismiss();
            }
            discoveryFailed(false);
          }
        }
      }
    };

    try
    {
      HueDiscover.discover(listener, discoveryRun * 10000);
    } catch (DiscoverException e)
    {
      Timber.d("Discover still running...");
      return;
    }

    dlg.show(fragmentManager, DiscoverDlgFragmentTag);

  }

  @UiThread
  protected void discoveryFailed(boolean canceled)
  {
    if (discoveryRun <= maxDiscoveryRuns && !canceled)
    {
      discoveryRun++;
      //findBridge();
      return;
    }

    /*new AlertDialog.Builder(this)
        .setTitle("Hue Bridge not found")
        .setMessage("Do you want to try again?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            discoveryRun = 1;
            findBridge();
          }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            System.exit(0);
          }
        })
        .create()
        .show();*/
  }

  private interface Validator
  {
    public abstract boolean validate(double value);
  }

  public interface OnCustomSeekbarChangedListener
  {
    void onChanged(double value);

  }

  public static class DiscoverDialogFragment extends DialogFragment
  {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
      ProgressDialog discoverAlertDlg = new ProgressDialog(getActivity());
      discoverAlertDlg.setIndeterminate(true);
      discoverAlertDlg.setMessage("Trying to find your Hue Bridge");
      discoverAlertDlg.setCancelable(true);

      return discoverAlertDlg;
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
      super.onCancel(dialog);
      HueDiscover.cancel();
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
        getDialog().setDismissMessage(null);
      super.onDestroyView();
    }
  }

  public static class AuthenticateDialogFragment extends DialogFragment
  {
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
      ProgressDialog authAlertDlg = new ProgressDialog(getActivity());
      authAlertDlg.setIndeterminate(true);
      authAlertDlg.setMessage("Please press the button on your bridge");
      authAlertDlg.setCancelable(true);

      return authAlertDlg;
    }

    @Override
    public void onCancel(DialogInterface dialog)
    {
      super.onCancel(dialog);
      hueController.cancelAuthentication();
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
        getDialog().setDismissMessage(null);
      super.onDestroyView();
    }
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
