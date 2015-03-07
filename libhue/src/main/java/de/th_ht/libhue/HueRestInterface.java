package de.th_ht.libhue;

import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by th on 23.02.2015.
 */
public interface HueRestInterface
{
  @POST("/api")
  List<PostPutResponse> createUser(@Body User user);

  @GET("/api/{username}/lights")
  Map<String, LightState> getLights(@Path("username") String username);

  @GET("/api/{username}/lights")
  List<PostPutResponse> getLightsError(@Path("username") String username);

  @GET("/api/{username}/lights/{id}")
  void getLight(@Path("username") String username, @Path("id") int id, Callback<LightState> callback);

  @PUT("/api/{username}/lights/{id}/state")
  void setLightState(@Path("username") String username, @Path("id") int id, @Body LightUpdate newLightState, Callback<List<PostPutResponse>> callback);

  class LightStateDetails
  {
    boolean on;
    int bri;
    int hue;
    int sat;
    float[] xy;
    int ct;
    String alert;
    String effect;
    String colormode;
    boolean reachable;
  }

  class LightState extends Response
  {
    LightStateDetails state;
    String type;
    String name;
    String modelid;
    String uniqueid;
    String swversion;
  }

  class PostPutResponse extends Response
  {
    Map<String, String> success;
  }

  class Response
  {
    Error error;
  }

  class Error
  {
    public Integer type;
    public String address;
    public String description;
  }

  public class User
  {
    String devicetype;
    String username;

    public User(String devicetype, String username)
    {
      this.devicetype = devicetype;
      this.username = username;
    }
  }

  public class LightUpdate
  {
    Boolean on;
    Integer bri;
    Integer hue;
    Integer sat;
    Float[] xy;
    Integer ct;
    String alert;
    String effect;
    Integer transitiontime;
  }
}
