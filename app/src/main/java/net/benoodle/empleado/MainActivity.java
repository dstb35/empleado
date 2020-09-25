package net.benoodle.empleado;

import androidx.appcompat.app.AppCompatActivity;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.User;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends OptionsMenuActivity implements MainAdaptador.AsignarListener {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    public static final String MENU = "menu";
    public static ArrayList<Order> orders = new ArrayList<>();
    public User user;
    public static Catalog catalog;
    private HashMap<String, String> body = new HashMap<>();
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    public static String boton = "";
    private TextView totalPedidos, store;
    private String store_id;
    private SearchView searchView;
    private int itemSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName(), sharedPrefManager.getSPUserId());
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.searchView);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        totalPedidos = findViewById(R.id.totalpedidos);
        store = findViewById(R.id.store);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        store_id = sharedPrefManager.getSPStore();
        store.setText("Tienda : "+store_id);
        mApiService.getStock(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
        this.itemSelected = 0;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.itemSelected = item.getItemId();
        invalidateOptionsMenu();
        switch (item.getItemId()) {
            case R.id.getOrdersNoPaid:
                boton = "Cobrar";
                mApiService.getSinCobrar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.getOrders:
                boton = "Asignar";
                mApiService.getSinAsignar(store_id, sharedPrefManager.getSPModus(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.MyOrders:
                boton = "Completar";
                mApiService.getAsignados(user.getUid(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.EntregarOrders:
                boton = "Entregar";
                mApiService.getEntregar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.CompleteOrders:
                boton = "";
                mApiService.getComplete(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.Preferencias:
                boton = "";
                lanzarPreferencias();
                return true;
            case R.id.Stock:
                boton = "";
                lanzarStock();
                return true;
            case R.id.Reasignar:
                boton = "";
                lanzarReasignar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void cambiarAdaptador (){
        if (boton.compareTo("") == 0) {
            totalPedidos.setText("Pedidos entregados : "+ String.valueOf(orders.size()));
        }else {
            totalPedidos.setText("Pedidos para " + boton.toLowerCase() + " :" + String.valueOf(orders.size()));
        }
        adaptador = new MainAdaptador(orders, MainActivity.this, MainActivity.this);
        recyclerView.setAdapter(adaptador);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adaptador.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void lanzarPreferencias() {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        this.startActivity(intent);
    }

    public void lanzarStock() {
        Intent intent = new Intent(this, StockActivity.class);
        this.startActivity(intent);
    }

    public void lanzarReasignar() {
        Intent intent = new Intent(this, ReasignarActivity.class);
        this.startActivity(intent);
    }

    /*
    Método que asignar una order a un empleado y lanza la activity con el order.
    Si el resultado es satisfactorio actualizamos el order en la variable orders para
    evitar recargar.
    */
    @Override
    public void Asignar (int i) {
        body.put("mail", user.getMail());
        body.put("id", orders.get(i).getOrderId());
        body.put("position", String.valueOf(i));
        mApiService.asignar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Asignarcallback);
    }

    /*Método para cobrar un pedido*/
    @Override
    public void Cobrar (int i) {
        body.put("id", orders.get(i).getOrderId());
        body.put("position", String.valueOf(i));
        mApiService.cobrar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Cobrarcallback);
    }

    /*Método para marcar como entregado un pedido.*/
    @Override
    public void Entregar (int i){
        body.put("id", orders.get(i).getOrderId());
        body.put("position", String.valueOf(i));
        body.put("state", "completed");
        mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Entregarcallback);
    }

    @Override
    public void Completar (int i){
        lanzarOrderDetailActivity(i);
    }

    public void lanzarOrderDetailActivity(int position){
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("i", position);
        this.startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (itemSelected == 0){
            this.itemSelected = menu.getItem(1).getItemId();
        }
        android.text.SpannableString s = new android.text.SpannableString(menu.findItem(itemSelected).toString());
        s.setSpan(new ForegroundColorSpan(getColor(R.color.menu_item_selected)), 0, s.length(), 0);
        menu.findItem(itemSelected).setTitle(s);
        return super.onPrepareOptionsMenu(menu);
    }


    Callback<ArrayList<Order>> Orderscallback = new Callback<ArrayList<Order>>() {
        @Override
        public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
            try {
                if (response.isSuccessful()) {
                    orders = response.body();
                }else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    orders.clear();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            cambiarAdaptador();
        }

        @Override
        public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Asignarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody>response) {
            if (response.isSuccessful()) {
                orders.remove(Integer.valueOf(body.get("position"))); //Lo elimino porque orders son los pedidos sin empleado.
                Toast.makeText(getApplicationContext(), "Pedido asignado.", Toast.LENGTH_LONG).show();
                lanzarOrderDetailActivity(Integer.valueOf(body.get("position").toString()));
            }else{
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    int position = Integer.valueOf(body.get("position"));
                    orders.remove(position);
                    body.remove("position");
                    adaptador.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Cobrarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody>response) {
            if (response.isSuccessful()) {
                int position = Integer.valueOf(body.get("position"));
                orders.remove(position); //Lo elimino porque orders son los pedidos sin cobrar.
                body.remove("position");
                Toast.makeText(getApplicationContext(), "Pedido cobrado.", Toast.LENGTH_LONG).show();
                adaptador.notifyDataSetChanged();
                //cambiarAdaptador();
            }else{
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    int position = Integer.valueOf(body.get("position"));
                    orders.remove(position);
                    body.remove("position");
                    adaptador.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                catalog = new Catalog(response.body());
                /*Con el catálogo cargado se pasa a mostrar los pedidos sin asignar*/
                boton = "Asignar";
                mApiService.getSinAsignar(store_id, sharedPrefManager.getSPModus(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
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
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Entregarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Orden entregada", Toast.LENGTH_LONG).show();
                int position = Integer.valueOf(body.get("position")); //Lo elimino porque orders son los pedidos sin entregar.
                orders.remove(position);
                cambiarAdaptador();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }
        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
