package de.th_ht.libhue.Errors;

import de.th_ht.libhue.HueRestInterface;

/**
 * Created by th on 28.02.2015.
 */
public class AuthenticateError extends RestReturnError
{
  public AuthenticateError(String detailMessage, HueRestInterface.Error resterror)
  {
    super(detailMessage, resterror);
  }

  public int getErrorcode()
  {
    return resterror.type;

  }
}
