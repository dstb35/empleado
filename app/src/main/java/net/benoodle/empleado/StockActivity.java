package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class StockActivity extends AppCompatActivity {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private User user;
    //public static Catalog stock;
    private Catalog stock;
    private HashMap<String, Object> body = new HashMap<>();
    private Context context;
    //private StockAdaptador adaptador;
    //private RecyclerView recyclerView;
    //private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
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
        mApiService.getStock(sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Stockcallback);
        //recyclerView = findViewById(R.id.recycler_stock);
        //recyclerView.setHasFixedSize(true);
        //layoutManager = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);
    }

    public void doActivateAll (View v) {
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
    }

    public void doDone (View v){
        body.put("user", user);
        body.put("catalog", stock.getCatalog());
        body.put("store_id", sharedPrefManager.getSPStore());
        mApiService.changeStock(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Chstockcallback);
    }

    public void doCancel (View v){
        finish();
    }

    Callback<ArrayList<Node>> Stockcallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                stock = new Catalog(response.body());
                //adaptador = new StockAdaptador(stock, StockActivity.this);
                //recyclerView.setAdapter(adaptador);
                LinearLayout container;
                container = findViewById(R.id.container);
                container.removeAllViews();
                container.setGravity(Gravity.CENTER);
                Float width = new Float(container.getWidth());
                Double switchWidth = width*0.75;
                Double textWidth = width*0.25;
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                //lp.setMargins(64, 8, 8, 8);
                for (Node node : stock.getCatalog()) {
                    LinearLayout nodeLayout = new LinearLayout(context);
                    nodeLayout.setOrientation(LinearLayout.HORIZONTAL);
                    nodeLayout.setLayoutParams(lp);
                    nodeLayout.setGravity(Gravity.CENTER);
                    Switch swStock = new Switch(context);
                    swStock.setLayoutParams(new LinearLayout.LayoutParams(
                            switchWidth.intValue(), LinearLayout.LayoutParams.MATCH_PARENT));
                    swStock.setText(node.getTitle());
                    swStock.setSwitchMinWidth(120);
                    swStock.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    swStock.setChecked(node.getStatus());
                    swStock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            stock.switchStock(node.getProductID(), compoundButton.isChecked());
                        }
                    });
                    EditText stockText = new EditText(context);
                    stockText.setLayoutParams(new LinearLayout.LayoutParams(
                            textWidth.intValue(), LinearLayout.LayoutParams.MATCH_PARENT));
                    stockText.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                    stockText.setGravity(Gravity.CENTER);
                    stockText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                    stockText.setText(node.getStock());
                    stockText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                            stock.setStock(node.getProductID(), stockText.getText().toString());
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                    nodeLayout.addView(swStock);
                    nodeLayout.addView(stockText);
                    container.addView(nodeLayout);
                }
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
                    finish();
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
