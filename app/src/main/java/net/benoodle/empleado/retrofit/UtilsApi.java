package net.benoodle.empleado.retrofit;

import android.util.Log;
import android.widget.Toast;

public class UtilsApi {
    //public static String BASE_URL_API = "https://benoodle.net";
    public static ApiService getAPIService(String BASE_URL_API) {
        ApiService instancia;
        try {
            instancia = RetrofitInstance.getRetrofitInstance(BASE_URL_API).create(ApiService.class);
            return instancia;
        } catch (Exception e) {
            Toast.makeText(null, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }
}
