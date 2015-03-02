package de.th_ht.libhue.Errors;

/**
 * Created by th on 28.02.2015.
 */
public class URLInvalid extends HueException
{
  private String url;

  public URLInvalid(String url)
  {
    super("The provided URL is not valid");
    this.url = url;
  }

  public String getUrl()
  {
    return url;
  }
}
