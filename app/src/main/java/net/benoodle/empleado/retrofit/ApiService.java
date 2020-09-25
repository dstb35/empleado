package net.benoodle.empleado.retrofit;

import net.benoodle.empleado.model.LoginData;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;

import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/*Enpoints:
    User login System
    User unlogin System
    GET
    Catalog productos para app eorder, sin uso en empleado /eorders/products
    Types para eorder, sin uso en empleado /eorders/types

    Stock diponible Todos los producto esten visibles o no  /eorders/stock getStock()
    Pedidos sin cobrar SinCobrarResource.php /php/sincobrar getSinCobrar()
    Pedidos sin asignar e incompletos SinAsignarResource.php /eorders/sinasignar getSinAsignar()
    Pedidos asignados a un empleado e incompletos AsignadosResource.php /eorders/asignados getAsignados()
    Pedidos hecho per sin entregar EntregarResource.php /eorders/entregar getEntregar()
    Pedidos completos todos CompleteResource.php /eorders/complete getComplete()

    POST
    Cobrar pedido CobrarResource.php /eorders/cobrar cobrar()
    Asignar pedido a un empleado AsignarResource.php /eorders/asignar asignar()
    Completar y entregar un pedido, recibe el id y el state, no el pedido completo CompletarResource.php /eorders/completar
    Cambiar Stock recibe el catalog stock completo con las mod ChstockResource.php /eorders/chstock changeStock()
    Modificar Pedido recibe el pedido modificado ModificarResource.php /eorders/modificar

    Otros
    Pedido concreto por id, o todos sin filtrar AllResource.php  /eorders/all
    Pedidos incompletos para cualquier empleado o tienda, sin uso /eorders/draft

 */

public interface ApiService {

    @POST("user/login?_format=json")
    @Headers({"Content-type: application/x-www-form-urlencoded"})
    Call<ResponseBody> loginRequest(@Body LoginData body);

    /*@GET("eorder/?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getAllNodes(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);*/

    @GET("eorders/sincobrar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getSinCobrar(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/sinasignar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getSinAsignar(@Query("store_id") String store_id, @Query("modus") Boolean modus, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/asignados?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getAsignados(@Query("uid") String id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/complete?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getComplete(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/cobrar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> cobrar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @POST("/eorders/asignar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> asignar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @POST("/eorders/completar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> completar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, String> body);

    @GET("eorders/all?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> OrderById(@Query("id") String id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/reasignar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<Order> reasignar(@Query("id") String id, @Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/stock?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Node>> getStock(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @GET("eorders/entregar?_format=json")
    @Headers({"Content-type: application/json"})
    Call<ArrayList<Order>> getEntregar(@Query("store_id") String store_id, @Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token);

    @POST("/eorders/chstock")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> changeStock(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);

    @POST("/eorders/modificar")
    @Headers({"Content-type: application/json"})
    Call<ResponseBody> modificar(@Header("Authorization") String user_auth, @Header("X-CSRF-Token") String x_csrf_token, @Body HashMap<String, Object> body);
}