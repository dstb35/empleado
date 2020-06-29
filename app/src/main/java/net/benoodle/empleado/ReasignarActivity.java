package net.benoodle.empleado;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
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
import static net.benoodle.empleado.MainActivity.orders;

public class ReasignarActivity  extends AppCompatActivity {
    private TextView orderID, total, item;
    private Button btFinalizar;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private String id, store_id;
    private Order order;
    private Toolbar toolbar;
    private HashMap<String, String> body = new HashMap<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_detail);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        sharedPrefManager = new SharedPrefManager(this);
        new SharedPrefManager(this);
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        this.store_id = sharedPrefManager.getSPStore();
        this.orderID = findViewById(R.id.orderID);
        this.total = findViewById(R.id.total);
        this.btFinalizar = findViewById(R.id.btFinalizar);
        this.item = findViewById(R.id.item);
        AlertDialog.Builder builder = new AlertDialog.Builder(ReasignarActivity.this);
        final EditText input = new EditText(ReasignarActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setTitle("Introduce  el número de pedido que quieras modificar: ");
        builder.setCancelable(true);
        builder.setPositiveButton("Importar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        id = input.getText().toString();
                        reasignar();
                    }
                });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void reasignar (){
        mApiService.reasignar(id, store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Ordercallback);
    }

    Callback<Order> Ordercallback = new Callback<Order>() {
        @Override
        public void onResponse(Call<Order> call, Response<Order> response) {
            try {
                if (response.isSuccessful()) {
                    order = response.body();
                    orderID.setText("Número de pedido: "+order.getOrderId());
                    total.setText("Total: "+order.getTotal()+" €");
                    StringBuilder items = new StringBuilder();
                    for (int j=0; j<order.getOrderItems().size(); j++){
                        OrderItem orderitem = order.getOrderItems().get(j);
                        try {
                            items.append(j+1+". Producto :" + catalog.getNodeById(orderitem.getId()).getTitle() + " Cantidad: " + orderitem.getQuantity() + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            items.append("Producto :" + e.getLocalizedMessage());
                        }
                        if (orderitem.getSelecciones() != null) {
                            items.append("Seleccion menú :");
                            for (String seleccion : orderitem.getSelecciones()) {
                                try {
                                    items.append(catalog.getNodeById(seleccion).getTitle() + ". ");
                                } catch (Exception e) {
                                    items.append(e.getLocalizedMessage() + " ");
                                }
                            }
                            items.append(System.getProperty("line.separator"));
                        }
                        items.append(System.getProperty("line.separator"));
                    }
                    item.setText(items.toString());
                    btFinalizar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Completar(order.getOrderId());
                        }
                    });
                }else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }

        @Override
        public void onFailure(Call<Order> call, Throwable t) {
            t.printStackTrace();
            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    public void Completar (String id){
        body.put("id", id);
        body.put("state", "entregar");
        mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
    }

    public void Cancelar (View v) {
        finish();
    }

    Callback<ResponseBody> Completarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Orden completada", Toast.LENGTH_LONG).show();
                finish();
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
            t.printStackTrace();
        }
    };
}
