package de.th_ht.libhue;

import android.webkit.URLUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import de.th_ht.libhue.Errors.AuthenticateError;
import de.th_ht.libhue.Errors.RestReturnError;
import de.th_ht.libhue.Errors.URLInvalid;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class Hue
{
  private static Hue instance = null;
  private HueRestInterface hueRestInterface;
  private Thread authThread = null;
  private HueListener hueListener = null;

  private String username = "";
  private String url = "";
  private boolean connectionGood;
  private Map<Integer, HueLight> lights = null;
  private boolean isConnected;

  private Hue()
  {
    isConnected = false;
  }

  public static Hue getInstance()
  {
    if (instance == null)
    {
      instance = new Hue();
    }

    return instance;
  }

  public void setHueListener(HueListener hueListener)
  {
    this.hueListener = hueListener;
  }

  public void setURL(String url, String username) throws URLInvalid
  {
    // check whether the URL is valid... otherwise there is no use going on....
    if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpUrl(url))
    {
      throw new URLInvalid(url);
    }

    if (this.url.contentEquals(url) && this.username.contentEquals(username) && !username.isEmpty())
    {
      return;
    }

    RestAdapter restAdapter = new RestAdapter.Builder()
        .setEndpoint(url)
        .setClient(new HueConnectionClient())
            //.setLogLevel(RestAdapter.LogLevel.FULL)
        .build();
    hueRestInterface = restAdapter.create(HueRestInterface.class);
    this.url = url;

    connectionGood = false;
    lights = null;
    this.username = username;
    isConnected = false;
  }

  public void connect()
  {
    // We use a thread here because this operation is non-blocking...
    if (isConnected)
    {
      hueListener.onConnect(this);
      return;
    }

    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          updateAllLights();
        }
        catch (RestReturnError e)
        {
          if (e.getResterror().type == 1)
          {
            hueListener.onNotAuthenticated(Hue.getInstance());
            return;
          }
        }
        catch (RetrofitError e)
        {
          if (e.getCause() instanceof IOException)
          {
            hueListener.onConnectFailed(Hue.getInstance(), e);
            return;
          }
          else if (e.getKind() == RetrofitError.Kind.HTTP || e.getKind() == RetrofitError.Kind
              .NETWORK)
          {
            hueListener.onConnectFailed(Hue.getInstance(), e);
            return;
          }
          else
          {
            throw e;
          }
        }

        hueListener.onConnect(Hue.getInstance());
        isConnected = true;
      }
    }).start();
  }

  /**
   * Tries to authenticate
   *
   * @param devicename
   * @param username
   * @return username if authenticated
   */
  private String _tryAuthenticate(String devicename, String username) throws AuthenticateError
  {
    List<HueRestInterface.PostPutResponse> responseList = null;
    try
    {
      responseList = hueRestInterface.createUser(new HueRestInterface.User(devicename, username));
    }
    catch (RetrofitError e)
    {
      throw e;
    }

    connectionResumed();

    HueRestInterface.PostPutResponse response = responseList.get(0);
    if (response.success != null)
    {
      this.username = response.success.get("username");
      return this.username;
    }

    throw new AuthenticateError("Authenticate Error", response.error);
  }

  public void tryAuthenticate()
  {
    tryAuthenticate(30);
  }


  /**
   * Tries to authenticate for 30 seconds. Calls the callback methods on success and failure...
   *
   * @param timeout
   * @return
   */
  public void tryAuthenticate(int timeout)
  {
    if (authThread != null)
    {
      authThread.interrupt();
    }

    authThread = new Thread(new AuthRunnable("", username, timeout));
    authThread.start();
  }

  public void cancelAuthenticate()
  {
    if (authThread != null)
    {
      authThread.interrupt();
    }
  }

  public Map<Integer, HueLight> getAllLights()
  {
    return lights;
  }

  synchronized protected void updateAllLights() throws RestReturnError
  {
    Map<String, HueRestInterface.LightState> retLights = null;
    try
    {
      retLights = hueRestInterface.getLights(username);
    }
    catch (RetrofitError e)
    {
      if (e.getKind() == RetrofitError.Kind.CONVERSION)
      {
        // Probably the bridge returned an error.... we check for it here....
        List<HueRestInterface.PostPutResponse> resp = hueRestInterface.getLightsError(username);
        if (resp.get(0).error != null)
        {
          throw new RestReturnError("updateAllLights failed", resp.get(0).error);
        }
        else
        {
          throw e;
        }
      }
      if (e.getCause() instanceof IOException)
      {
        connectionLost(e);
        throw e;
      }
      else if (e.getKind() == RetrofitError.Kind.HTTP || e.getKind() == RetrofitError.Kind.NETWORK)
      {
        connectionLost(e);
        throw e;
      }
      else
      {
        // Something else is wrong....
        throw e;
      }
    }

    if (lights == null || lights.size() != retLights.size())
    {
      lights = new HashMap<>(retLights.size());

      for (String key : retLights.keySet())
      {
        lights.put(Integer.parseInt(key), new HueLight(retLights.get(key), Integer.parseInt(key)));
      }
    }
    else
    {
      for (String key : retLights.keySet())
      {
        lights.get(Integer.parseInt(key)).state = retLights.get(key);
      }
    }
  }

  protected HueRestInterface.LightState getLightState(int id)
  {
    return lights.get(id).state;
  }

  protected void postUpdate(int id, HueRestInterface.LightUpdate newstate, final boolean doUpdate)
  {
    hueRestInterface.setLightState(username, id, newstate, new Callback<List<HueRestInterface
        .PostPutResponse>>()
    {
      @Override
      public void success(List<HueRestInterface.PostPutResponse> responseList, Response response)
      {
        connectionResumed();
        if (doUpdate)
        {
          new Thread(new Runnable()
          {
            @Override
            public void run()
            {
              try
              {
                updateAllLights();
              }
              catch (RestReturnError e)
              {
              }
            }
          }).start();
        }
      }

      @Override
      public void failure(RetrofitError error)
      {
        if (error.getCause() instanceof IOException)
        {
          connectionLost(error);
        }
        else if (error.getKind() == RetrofitError.Kind.HTTP || error.getKind() == RetrofitError
            .Kind.NETWORK)
        {
          connectionLost(error);
        }
      }
    });
  }

  public String getUsername()
  {
    return username;
  }

  protected void connectionLost(Exception exception)
  {
    if (connectionGood && isConnected)
    {
      connectionGood = false;
      hueListener.onConnectionLost(this, exception);
    }

    if (!isConnected)
    {
      hueListener.onConnectFailed(this, exception);
    }
  }

  protected void connectionResumed()
  {
    if (!connectionGood && isConnected)
    {
      connectionGood = true;
      hueListener.onConnectionResumed(this);
    }
  }

  class AuthRunnable implements Runnable
  {
    String devicename;
    String username;
    int timeout;

    AuthRunnable(String devicename, String username, int timeout)
    {
      this.devicename = devicename;
      this.username = username;
      this.timeout = timeout;
    }

    @Override
    public void run()
    {
      long starttime = System.nanoTime();
      String finalusername = "";
      while (TimeUnit.SECONDS.convert(System.nanoTime() - starttime, 
          TimeUnit.NANOSECONDS) < timeout)
      {
        try
        {
          finalusername = _tryAuthenticate(devicename, username);
        }
        catch (AuthenticateError error)
        {
          if (error.getErrorcode() == 101)
          {
            connectionResumed();
            try
            {
              Thread.sleep(1000L);
            }
            catch (InterruptedException e)
            {
              hueListener.onAuthenticationFailed(Hue.getInstance(), "Aborted", e);
              return;
            }
            continue;
          }
        }
        catch (RetrofitError error)
        {
          hueListener.onAuthenticationFailed(Hue.getInstance(), "Retrofit Error", error);
          connectionLost(error);
          return;
        }

        connectionResumed();
        try
        {
          updateAllLights();
        }
        catch (RestReturnError error)
        {
          hueListener.onAuthenticationFailed(Hue.getInstance(), "Error in return from the " +
              "Bridge", error);
        }
        hueListener.onAuthenticated(Hue.getInstance(), finalusername);
        hueListener.onConnect(Hue.getInstance());
        isConnected = true;
        return;
      }

      hueListener.onAuthenticationFailed(Hue.getInstance(), "Linkbutton not pressed", null);
    }
  }
}
