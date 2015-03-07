package de.th_ht.libhue;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

/**
 * Created by th on 28.02.2015.
 */
public final class HueConnectionClient extends UrlConnectionClient
{
  @Override
  protected HttpURLConnection openConnection(Request request) throws IOException
  {
    HttpURLConnection connection = super.openConnection(request);
    connection.setConnectTimeout(1000);
    connection.setReadTimeout(1000);
    return connection;
  }
}