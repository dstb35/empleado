package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.orders;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView orderID, total, item;
    private Switch cobrado;
    private Button btFinalizar;
    private Order order;
    private Toolbar toolbar;
    private int position;
    private HashMap<String, String> body = new HashMap<>();
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_detail);
        sharedPrefManager = new SharedPrefManager(this);
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        Intent intent = getIntent();
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        position = intent.getIntExtra("i", -1);
        this.orderID = findViewById(R.id.orderID);
        this.total = findViewById(R.id.total);
        this.btFinalizar = findViewById(R.id.btFinalizar);
        this.item = findViewById(R.id.item);
        this.cobrado = findViewById(R.id.cobrado);
        if (position != -1) {
            order = orders.get(position);
            orderID.setText("Número de pedido: "+order.getOrderId());
            total.setText("Total: "+order.getTotal()+" €");
            cobrado.setChecked(order.getPagado());
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
                    Completar(order.getOrderId(), position);
                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Orden no recibida", Toast.LENGTH_LONG).show();
        }
    }

    public void Completar (String id, int i){
        body.put("id", id);
        body.put("state", "entregar");
        mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
        orders.get(i).setState("entregar");
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
