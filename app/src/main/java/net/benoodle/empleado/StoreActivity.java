package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.empleado.MainActivity.catalog;

public class StoreActivity extends AppCompatActivity {
    private TextView TxtStore, TxtTotals;
    private EditText zipcode, country, name;
    private Button btSave, btCancel;
    private SwitchCompat swmodus, swactive, swubi, swprice;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    private View mProgressView;
    private Store store;

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
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
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
        if (store != null){
            store.setName(name.getText().toString());
            store.setZipcode(zipcode.getText().toString());
            store.setCountry(country.getText().toString());
            if (swactive.isChecked()){
                store.setActiva("1");
            }else{
                store.setActiva("0");
            }
            if (swmodus.isChecked()){
                store.setModus("1");
            }else{
                store.setModus("0");
            }
            if (swubi.isChecked()){
                store.setSaltarubi("1");
            }else{
                store.setSaltarubi("0");
            }
            if (swprice.isChecked()){
                store.setPricealt("1");
            }else{
                store.setPricealt("0");
            }
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.postStore(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), store)
                    .enqueue(new Callback<ResponseBody>(){
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
