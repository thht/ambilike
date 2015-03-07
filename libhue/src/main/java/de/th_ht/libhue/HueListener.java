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
