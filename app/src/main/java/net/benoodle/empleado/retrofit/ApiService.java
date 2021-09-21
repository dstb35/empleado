package net.benoodle.empleado.retrofit;

import net.benoodle.empleado.model.Cuppon;
import net.benoodle.empleado.model.LoginData;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.Store;
import net.benoodle.empleado.model.Tipo;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("user/login?_format=json")
    @Headers({"Content-type: application/x-www-form-urlencoded"})
    Call<ResponseBody> loginRequest(@Body LoginData body);

    @GET("eorders/sincobrar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getSinCobrar(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/sinasignar?_format=json")
    @Headers({"Content-type: application/json"})
    //Call<ArrayList<Order>> getSinAsignar(@Query("store_id") String store_id, @Query("modus") Boolean modus, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);
    Call<ArrayList<Order>> getSinAsignar(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/asignados?_format=json")
    @Headers({"Content-type: application/json"})
    //Call<ArrayList<Order>> getAsignados(@Query("uid") String id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);
    //Call<ArrayList<Order>> getAsignados(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);
    Call<ArrayList<Order>> getAsignados(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/complete?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getComplete(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/cobrar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> cobrar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @POST("/eorders/asignar")
    @Headers({"Content-type: application/json"})
    Call<Order> asignar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @POST("/eorders/completar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> completar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @GET("eorders/all?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getOrders(@Query("id") String id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/reasignar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<Order> reasignar(@Query("id") String id, @Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/tracing?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<ArrayList<Order>>> getTracing(@Query("product_id") String product_id, @Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/stock?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getStock(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/entregar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getEntregar(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/chstock")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> changeStock(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);

    @POST("/eorders/addcuppon")
    @Headers({"Content-type: application/json"})
    //Call<ResponseBody> addCuppon(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);
    Call<Cuppon> addCuppon(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body Cuppon cuppon);

    @POST("/eorders/modificar")
    @Headers({"Content-type: application/json"})
    Call<Order> modificar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);

    @HTTP(method = "DELETE", path = "/eorders/modificar?_format=json", hasBody=true)
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> borrar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @GET("eorders/stores?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Store>> getStores(@Header("Authorization") String user_auth, @Query("zipcode") String zipcode, @Query("country") String country, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("/eorders/getstore?_format=json")
    @Headers({"Content-type: application/json"})
    Call<Store> getStore(@Header("Authorization") String user_auth, @Query("store_id") String store_id, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/poststore")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> postStore(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body Store store);

    @GET("eorders/types?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Tipo>> getTypes(@Header("Authorization") String user_auth, @Query("langcode") String langcode, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/create?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> addOrder(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);

    @GET("eorders/products?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getAllNodes(@Query("store_id") String store_id, @Query("langcode") String langcode, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

}