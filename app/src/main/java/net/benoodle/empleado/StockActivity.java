package net.benoodle.empleado;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.AppCompatEditText;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.widget.TextViewCompat;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
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

public class StockActivity extends AppCompatActivity {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private Catalog stock;
    private HashMap<String, Object> body = new HashMap<>();
    private Context context;
    private View mProgressView;
    ContextThemeWrapper ctx;
    //private StockAdaptador adaptador;
    //private RecyclerView recyclerView;
    //private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        this.ctx = new ContextThemeWrapper(context, R.style.MyCustomSwitch);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_stock);
        sharedPrefManager = new SharedPrefManager(this);
        mProgressView = findViewById(R.id.login_progress);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        //user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName(), sharedPrefManager.getSPUserId());
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.getStock(sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Stockcallback);
    }

    public void doActivateAll(View v) {
        if (sharedPrefManager.getSPEncargado()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Esto activará todos los productos en stock y pondrá el stock a -1 para desactivar el control de stock. ¿Desea continuar?");
            builder.setCancelable(true);
            builder.setPositiveButton("Si",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stock.doActivateAll();
                            doDone(v);
                        }
                    });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            Toast.makeText(context, "Solo los encargados pueden modificar el stock.", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDone(View v) {
        if (sharedPrefManager.getSPEncargado()) {
            body.put("catalog", stock.getCatalog());
            body.put("store_id", sharedPrefManager.getSPStore());
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.changeStock(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Chstockcallback);
        } else {
            Toast.makeText(context, "Solo los encargados pueden modificar el stock.", Toast.LENGTH_SHORT).show();
        }
    }

    public void doCancel(View v) {
        finish();
    }

    Callback<ArrayList<Node>> Stockcallback = new Callback<ArrayList<Node>>() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                stock = new Catalog(response.body());
                stock.CrearTypes();
                ArrayList<String> types = stock.getTypes();
                //Quitar el kakigori genérico con id 0 y poner todos los kakigoris
                stock.restaurarKakigoris();
                LinearLayout container;
                container = findViewById(R.id.container);
                container.removeAllViews();
                //container.setGravity(Gravity.CENTER);
                float width = (float) container.getWidth();
                Double switchWidth = width * 0.5;
                Double textWidth = width * 0.1;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                        textWidth.intValue(),
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                textParams.setMargins(8, 0, 8,0);
                for (String type : types) {
                    ArrayList<Node> catalogByType = stock.TypeCatalog(type);
                    TextView txtType = new TextView(context);
                    txtType.setText(type);
                    txtType.setTextSize(28);
                    txtType.setAllCaps(true);
                    txtType.setPadding(0, 24, 0, 0);
                    container.addView(txtType);
                    for (Node node : catalogByType) {
                        LinearLayout nodeLayout = new LinearLayout(context);
                        nodeLayout.setOrientation(LinearLayout.HORIZONTAL);
                        nodeLayout.setLayoutParams(lp);
                        //nodeLayout.setGravity(Gravity.CENTER);
                        Switch swStock = new Switch(context);
                        //SwitchCompat swStock = new SwitchCompat(StockActivity.this);
                        swStock.setLayoutParams(new LinearLayout.LayoutParams(
                                switchWidth.intValue(), LinearLayout.LayoutParams.MATCH_PARENT));
                        swStock.setText(node.getTitle());
                        swStock.setSwitchMinWidth(120);
                        if (Build.VERSION.SDK_INT >= 26) {
                            swStock.setAutoSizeTextTypeUniformWithConfiguration(2, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        } else {
                            swStock.setTextSize(18
                            );
                        }
                        //swStock.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                        swStock.setChecked(node.getStatus());
                        swStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                stock.switchStock(node.getProductID(), compoundButton.isChecked());
                            }
                        });
                        EditText price = new EditText(context);
                        price.setText(node.getPrice());
                        price.setLayoutParams(textParams);
                        price.setTextSize(14);
                        price.setInputType(EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
                        price.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                                try {
                                    stock.setPrice(node.getProductID(), price.getText().toString());
                                } catch (NumberFormatException e) {

                                }
                            }
                            @Override
                            public void afterTextChanged (Editable editable){
                            }
                        });
                        EditText stockText = new EditText(context);
                        stockText.setLayoutParams(textParams);
                        /*if (Build.VERSION.SDK_INT >= 26) {
                            stockText.setAutoSizeTextTypeUniformWithConfiguration(2, 14, 2, TypedValue.COMPLEX_UNIT_DIP);
                        } else {
                            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(stockText, 2, 14, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }*/
                        stockText.setTextSize(14);
                        //stockText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                        //stockText.setGravity(Gravity.CENTER);
                        stockText.setInputType(EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
                        stockText.setText(String.valueOf(node.getStock()));
                        stockText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                                try {
                                    stock.setStock(node.getProductID(), Integer.valueOf(stockText.getText().toString()));
                                } catch (NumberFormatException e) {

                                }
                            }
                                @Override
                                public void afterTextChanged (Editable editable){
                                }
                            });
                        nodeLayout.addView(swStock);
                        nodeLayout.addView(price);
                        nodeLayout.addView(stockText);
                        container.addView(nodeLayout);
                        }
                    }
                } else{
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure (Call < ArrayList < Node >> call, Throwable t){
                mProgressView.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        ;

        Callback<ResponseBody> Chstockcallback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                mProgressView.setVisibility(View.GONE);
                try {
                    if (response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Stock cambiado correctamente", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String error = response.errorBody().string();
                        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                mProgressView.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }
