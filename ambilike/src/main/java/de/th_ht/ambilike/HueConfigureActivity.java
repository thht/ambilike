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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Receiver;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.th_ht.libhue.HueDiscover;
import de.th_ht.libhue.HueLight;

import static java.lang.Math.round;

@SuppressLint("Registered")
@EActivity
@OptionsMenu(R.menu.hue_configure_activity)
public class HueConfigureActivity extends ActionBarActivity
{
  static final String IsConnectedAction = "IsConnected";
  private static final int SHOW_FIND_BRIDGE = 1;
  private static final int DISMISS_FIND_BRIDGE = 2;
  private static final int SHOW_AUTHENTICATE = 3;
  private static final int DISMISS_AUTHENTICATE = 4;
  private static final int SHOW_FIND_BRIDGE_FAILED = 5;
  private static final int DISMISS_FIND_BRIDGE_FAILED = 6;
  private static final int SHOW_AUTH_FAILED = 7;
  private static final int DISMISS_AUTH_FAILED = 8;
  private static final int SHOW_ROOT_FAILED = 9;
  private static final String DiscoverDlgFragmentTag = "DiscoverDialog";
  private static final String DiscoverFailedDlgFragmentTag = "DiscoverFailedDialog";
  private static final String AuthenticateDlgFragmentTag = "AuthDialog";
  private static final String AuthenticationFailedDlgFragmentTag = "AuthFailedDialog";
  private static final String RootFailedDlgFragmentTag = "RootFailedDialog";
  private static final int intentFlags = Intent.FLAG_ACTIVITY_NEW_TASK | Intent
      .FLAG_ACTIVITY_SINGLE_TOP;
  @Bean
  private static
  HueController hueController;
  @Extra
  private final
  int showDialog = 0;
  private final FragmentManager fragmentManager = getFragmentManager();
  @Pref
  private
  HuePreferences_ preferences;
  private AlertDialog aboutDialog = null;

  @ViewById
  private
  SeekBar seekBarConfigureTransition;
  @ViewById
  private
  SeekBar seekBarConfigureColorfulness;
  @ViewById
  private
  SeekBar seekBarConfigureMinBrightness;
  @ViewById
  private
  SeekBar seekBarConfigureMaxBrightness;

  @ViewById
  private
  TextView textConfigureTransition;
  @ViewById
  private
  TextView textConfigureColorfulness;
  @ViewById
  private
  TextView textConfigureMinBrightness;
  @ViewById
  private
  TextView textConfigureMaxBrightness;

  @ViewById
  private
  Button buttonConfigureLights;

  private SeekbarSettings transitionSettings;
  private SeekbarSettings colorfulnessSettings;
  private SeekbarSettings minBrightnessSettings;
  private SeekbarSettings maxBrightnessSettings;

  public static void showFindBridge(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(SHOW_FIND_BRIDGE).flags(intentFlags).start();
  }

  public static void dismissFindBridge(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(DISMISS_FIND_BRIDGE).flags(intentFlags)
        .start();
  }

  public static void showFindBridgeFailed(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(SHOW_FIND_BRIDGE_FAILED).flags(intentFlags)
        .start();
  }

  public static void showAuthenticate(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(SHOW_AUTHENTICATE).flags(intentFlags).start();
  }

  public static void dismissAuthenticate(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(DISMISS_AUTHENTICATE).flags(intentFlags)
        .start();
  }

  public static void showAuthFailed(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(SHOW_AUTH_FAILED).flags(intentFlags).start();
  }

  public static void dismissAuthFailed(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(DISMISS_AUTH_FAILED).flags(intentFlags)
        .start();
  }

  public static void showRootFailed(Context context)
  {
    HueConfigureActivity_.intent(context).showDialog(SHOW_ROOT_FAILED).flags(intentFlags).start();
  }

  @Click
  void buttonConfigureLights()
  {
    Set<String> curLights = preferences.Lights().getOr(new HashSet<String>());
    Map<Integer, HueLight> allLights = hueController.getAllLights();

    CharSequence[] lights_strings = new CharSequence[allLights.size()];
    final boolean[] lights_checked = new boolean[allLights.size()];

    int counter = 0;
    for (int i : allLights.keySet())
    {
      lights_strings[counter] = allLights.get(i).getName();
      if (curLights.contains(String.valueOf(i)))
      {
        lights_checked[counter] = true;
      }

      counter++;
    }

    new AlertDialog.Builder(this)
        .setTitle("Please choose lights")
        .setMultiChoiceItems(lights_strings, lights_checked, new DialogInterface
            .OnMultiChoiceClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i, boolean b)
          {
            lights_checked[i] = b;
          }
        })
        .setPositiveButton("Ok", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            Set<String> lights_chosen = new HashSet<>();
            for (int j = 0; j < lights_checked.length; j++)
            {
              if (lights_checked[j])
              {
                lights_chosen.add(Integer.toString(j + 1));
              }
            }

            hueController.setLights(lights_chosen);
            preferences.edit().Lights().put(lights_chosen).apply();
          }
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
          }
        })
        .show();
  }

  @OptionsItem(R.id.find_bridge)
  void buttonConfigureBridge()
  {
    new AlertDialog.Builder(this)
        .setMessage("Do you want to do a new search for the Bridge?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialogInterface, int i)
          {
            buttonConfigureLights.setEnabled(false);
            hueController.findBridge();
          }
        })
        .setNegativeButton("No", null)
        .show();
  }

  @OptionsItem(R.id.about)
  void showAbout()
  {
    TextView msg = new TextView(this);
    msg.setText(Html.fromHtml(getString(R.string.about_text)));
    msg.setMovementMethod(LinkMovementMethod.getInstance());
    msg.setClickable(true);

    aboutDialog = new AlertDialog.Builder(this)
        .setTitle("Ambilike " + BuildConfig.VERSION_NAME)
        .setView(msg)
        .show();
  }

  @OptionsItem(R.id.exit)
  void exit()
  {
    hueController.terminate();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_hue_configure);
  }

  @AfterViews
  protected void init()
  {
    transitionSettings = new SeekbarSettings(seekBarConfigureTransition, textConfigureTransition,
        0, 5, preferences.Transitiontime().get(), true, new OnCustomSeekbarChangedListener()
    {
      @Override
      public void onChanged(double value)
      {
        hueController.setTransition((float) value);
        preferences.edit().Transitiontime().put((float) value).apply();
      }
    });

    colorfulnessSettings = new SeekbarSettings(seekBarConfigureColorfulness,
        textConfigureColorfulness, 0, 2, preferences.Colorfullness().get(), true,
        new OnCustomSeekbarChangedListener()
        {
          @Override
          public void onChanged(double value)
          {
            hueController.setColorExp((float) value);
            preferences.edit().Colorfullness().put((float) value).apply();
          }
        });

    minBrightnessSettings = new SeekbarSettings(seekBarConfigureMinBrightness,
        textConfigureMinBrightness, 0, 255, preferences.MinBrightness().get(), false,
        new OnCustomSeekbarChangedListener()
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
        {
          return false;
        }

        return true;
      }
    });

    maxBrightnessSettings = new SeekbarSettings(seekBarConfigureMaxBrightness,
        textConfigureMaxBrightness, 0, 255, preferences.MaxBrightness().get(), false,
        new OnCustomSeekbarChangedListener()
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
        {
          return false;
        }

        return true;
      }
    });

    if (hueController.isConnected())
    {
      buttonConfigureLights.setEnabled(true);
    }
    else
    {
      buttonConfigureLights.setEnabled(false);
    }
  }

  @AfterExtras
  void handleIntent()
  {
    try
    {
      switch (showDialog)
      {
        case SHOW_AUTHENTICATE:
          new AuthenticateDialogFragment().show(fragmentManager, AuthenticateDlgFragmentTag);
          break;

        case DISMISS_AUTHENTICATE:
          ((AuthenticateDialogFragment) fragmentManager.findFragmentByTag
              (AuthenticateDlgFragmentTag)).dismiss();
          break;

        case SHOW_FIND_BRIDGE:
          new DiscoverDialogFragment().show(fragmentManager, DiscoverDlgFragmentTag);
          break;

        case DISMISS_FIND_BRIDGE:
          ((DiscoverDialogFragment) fragmentManager.findFragmentByTag(DiscoverDlgFragmentTag))
              .dismiss();
          break;

        case SHOW_FIND_BRIDGE_FAILED:
          new DiscoverFailedDialogFragment().show(fragmentManager, DiscoverFailedDlgFragmentTag);
          break;

        case DISMISS_FIND_BRIDGE_FAILED:
          ((DiscoverFailedDialogFragment) fragmentManager.findFragmentByTag
              (DiscoverFailedDlgFragmentTag)).dismiss();
          break;

        case SHOW_AUTH_FAILED:
          new AuthFailedDialogFragment().show(fragmentManager, AuthenticationFailedDlgFragmentTag);
          break;

        case DISMISS_AUTH_FAILED:
          ((AuthFailedDialogFragment) fragmentManager.findFragmentByTag
              (AuthenticationFailedDlgFragmentTag)).dismiss();
          break;

        case SHOW_ROOT_FAILED:
          new RootFailedDialogFragment().show(fragmentManager, RootFailedDlgFragmentTag);
          break;
      }
    }
    catch (NullPointerException ignored)
    {
    }

    if (showDialog != 0)
    {
      setIntent(getIntent().putExtra("showDialog", 0));
    }
  }

  @Override
  protected void onDestroy()
  {
    if (aboutDialog != null)
    {
      aboutDialog.dismiss();
    }
    super.onDestroy();
  }

  @Receiver(actions = HueConfigureActivity.IsConnectedAction, local = true, registerAt = Receiver
      .RegisterAt.OnCreateOnDestroy)
  void isConnectedReceiver()
  {
    buttonConfigureLights.setEnabled(hueController.isConnected());
  }

  private interface Validator
  {
    public abstract boolean validate(double value);
  }

  public interface OnCustomSeekbarChangedListener
  {
    void onChanged(double value);
  }

  public static class AuthFailedDialogFragment extends DialogFragment
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
      AlertDialog dlg = new AlertDialog.Builder(getActivity())
          .setTitle("Hue Bridge not found")
          .setMessage("Do you want to try again?")
          .setPositiveButton("Yes", new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              hueController.authenticate();
            }
          })
          .setNegativeButton("No", new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              hueController.terminate();
            }
          })
          .create();

      return dlg;
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
      {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
  }

  public static class RootFailedDialogFragment extends DialogFragment
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
      AlertDialog dlg = new AlertDialog.Builder(getActivity())
          .setTitle("Could not get Root access.")
          .setMessage("Do you want to try again?")
          .setNegativeButton("Bye", new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              hueController.terminate();
            }
          })
          .create();

      return dlg;
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
      {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
  }

  public static class DiscoverFailedDialogFragment extends DialogFragment
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
      AlertDialog dlg = new AlertDialog.Builder(getActivity())
          .setTitle("Hue Bridge not found")
          .setMessage("Do you want to try again?")
          .setPositiveButton("Yes", new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              hueController.findBridge();
            }
          })
          .setNegativeButton("No", new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
              hueController.terminate();
            }
          })
          .create();

      return dlg;
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
      {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
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
      hueController.discoveryFailed();
    }

    @Override
    public void onDestroyView()
    {
      if (getDialog() != null && getRetainInstance())
      {
        getDialog().setDismissMessage(null);
      }
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
      {
        getDialog().setDismissMessage(null);
      }
      super.onDestroyView();
    }
  }

  private class SeekbarSettings
  {
    private final SeekBar seekbar;
    private final TextView textView;
    private final double min;
    private final double max;
    private final boolean usesFloat;
    private final OnCustomSeekbarChangedListener listener;
    private double current;
    private Validator validator;

    private SeekbarSettings(SeekBar seekbar, final TextView textView, double min, double max,
                            final double current, boolean usesFloat,
                            OnCustomSeekbarChangedListener listener)
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

    int double2progress(double val)
    {
      int seekmax = seekbar.getMax();
      double prog_range = seekmax;
      double orig_range = max - min;
      double factor = prog_range / orig_range;
      double offset = -min;

      return (int) ((val + offset) * factor);
    }

    double progress2double(int progress)
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
      {
        current = round(current);
      }

      if (!validate(current))
      {
        update();
        return;
      }

      this.current = current;

      if (update)
      {
        update();
      }
    }

    boolean validate(double value)
    {
      if (current > max || current < min)
      {
        return false;
      }

      if (validator != null && !validator.validate(value))
      {
        return false;
      }

      return true;
    }

    void update()
    {
      seekbar.setProgress(double2progress(current));

      String textString;

      if (usesFloat)
      {
        textString = String.format("%.1f", current);
      }
      else
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
