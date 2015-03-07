package de.th_ht.libhue.Errors;

import de.th_ht.libhue.HueRestInterface;

/**
 * Created by th on 28.02.2015.
 */
public class RestReturnError extends HueException
{
  protected HueRestInterface.Error resterror;

  public RestReturnError(String detailMessage, HueRestInterface.Error resterror)
  {
    super(detailMessage);
    this.resterror = resterror;
  }

  public HueRestInterface.Error getResterror()
  {
    return resterror;
  }
}
