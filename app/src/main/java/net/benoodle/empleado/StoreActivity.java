package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.model.Store;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.empleado.MainActivity.catalog;

public class StoreActivity extends AppCompatActivity {
    private TextView TxtStore, TxtTotals, producto, cantidad;
    private EditText zipcode, country, name;
    private Button btSave, btCancel;
    private SwitchCompat swmodus, swactive, swubi, swprice;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    private View mProgressView;
    private Store store;
    private LinearLayout productos;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_store_detail);
        sharedPrefManager = new SharedPrefManager(this);
        this.context = getApplicationContext();
        try {
            mApiService = UtilsApi.getAPIService();
        } catch (Exception e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        this.TxtStore = findViewById(R.id.TxtStore);
        this.TxtTotals = findViewById(R.id.totals);
        this.name = findViewById(R.id.name);
        this.zipcode = findViewById(R.id.zipcode);
        this.country = findViewById(R.id.country);
        this.swactive = findViewById(R.id.swactive);
        this.swmodus = findViewById(R.id.swmodus);
        this.swubi = findViewById(R.id.swubi);
        this.swprice = findViewById(R.id.swprice);
        this.btSave = findViewById(R.id.btSave);
        this.btCancel = findViewById(R.id.btCancel);
        this.mProgressView = findViewById(R.id.login_progress);
        this.productos = findViewById(R.id.productos);
        this.btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfirmarCambios();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mApiService.getStore(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPStore(), sharedPrefManager.getSPCsrfToken()).enqueue(StoreCallback);
    }

    Callback<Store> StoreCallback = new Callback<Store>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onResponse(Call<Store> call, Response<Store> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    store = response.body();
                    TxtTotals.setText(String.format("%s € Facturados en %s pedidos el día %s", store.getTotal(), store.getCount(), store.getDay()));
                    TxtStore.setText(String.format("Id de tienda: %s", store.getStore_id()));
                    name.setText(store.getName());
                    zipcode.setText(store.getZipcode());
                    country.setText(store.getCountry());
                    if (store.getActiva().compareTo("0") == 0) {
                        swactive.setChecked(false);
                    } else {
                        swactive.setChecked(true);
                    }
                    if (store.getSaltarubi().compareTo("0") == 0) {
                        swubi.setChecked(false);
                    } else {
                        swubi.setChecked(true);
                    }
                    if (store.getModus().compareTo("0") == 0) {
                        swmodus.setChecked(false);
                    } else {
                        swmodus.setChecked(true);
                    }
                    if (store.getPricealt().compareTo("0") == 0) {
                        swprice.setChecked(false);
                    } else {
                        swprice.setChecked(true);
                    }
                    Set<Map.Entry<String, Map<String, String>>> types = store.getProductos().entrySet();
                    Iterator<Map.Entry<String, Map<String, String>>> it = types.iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Map<String, String>> type = it.next();
                        TextView tipo = new TextView(context);
                        tipo.setTextAppearance(R.style.MyCustomTextView);
                        tipo.setTextSize(22);
                        tipo.setText(type.getKey().substring(0, 1).toUpperCase() + type.getKey().substring(1));
                        productos.addView(tipo);
                        Set<Map.Entry<String, String>> products = type.getValue().entrySet();
                        Iterator<Map.Entry<String, String>> iterator= products.iterator();
                        while (iterator.hasNext()){
                            Map.Entry<String, String> product = iterator.next();
                            TextView producto = new TextView(context);
                            producto.setTextAppearance(R.style.MyCustomTextView);
                            producto.setTextSize(14);
                            producto.setText(String.format("%s: %s unidades.", product.getKey(), product.getValue()));
                            productos.addView(producto);
                        }
                    }
                } else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<Store> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(StoreActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void ConfirmarCambios() {
        if (store != null) {
            store.setName(name.getText().toString());
            store.setZipcode(zipcode.getText().toString());
            store.setCountry(country.getText().toString());
            if (swactive.isChecked()) {
                store.setActiva("1");
            } else {
                store.setActiva("0");
            }
            if (swmodus.isChecked()) {
                store.setModus("1");
            } else {
                store.setModus("0");
            }
            if (swubi.isChecked()) {
                store.setSaltarubi("1");
            } else {
                store.setSaltarubi("0");
            }
            if (swprice.isChecked()) {
                store.setPricealt("1");
            } else {
                store.setPricealt("0");
            }
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.postStore(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), store)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            mProgressView.setVisibility(View.GONE);
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Tienda modificada", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                try {
                                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                                    Toast.makeText(context, jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            mProgressView.setVisibility(View.GONE);
                            Toast.makeText(StoreActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
