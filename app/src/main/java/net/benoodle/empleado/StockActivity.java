package net.benoodle.empleado;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.User;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockActivity extends OptionsMenuActivity implements StockAdaptador.StockListener {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private User user;
    public static Catalog stock;
    private HashMap<String, Object> body = new HashMap<>();
    private StockAdaptador adaptador;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName(), sharedPrefManager.getSPUserId());
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        mApiService.getStock(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Stockcallback);
        recyclerView = findViewById(R.id.recycler_stock);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void setStock(String productID, String quantity){
        stock.setStock(productID, quantity);
    }

    public void switchStock(String productID, Integer status){
        stock.switchStock(productID, status);
    }

    public void doDone (View v){
        /*TODO
        Cambiar stock aqui y mover el finish()
         */
        body.put("user", user);
        body.put("catalog", stock.getCatalog());
        mApiService.changeStock(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Chstockcallback);
        finish();
    }

    public void doCancel (View v){
        finish();
    }

    Callback<ArrayList<Node>> Stockcallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                stock = new Catalog(response.body());
                adaptador = new StockAdaptador(stock, StockActivity.this, StockActivity.this);
                recyclerView.setAdapter(adaptador);
            }else{
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Node>> call, Throwable t) {
            t.printStackTrace();
            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    Callback<ResponseBody> Chstockcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            try {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Stock cambiado correctamente", Toast.LENGTH_LONG).show();
                }else {
                    //JSONObject jObjError = new JSONObject(response.errorBody().string());
                    String error = response.errorBody().string();
                    //Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            t.printStackTrace();
            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
        }
    };
}
