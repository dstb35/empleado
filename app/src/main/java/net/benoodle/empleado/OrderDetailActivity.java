package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.orders;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
//import android.widget.Switch;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
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

public class OrderDetailActivity extends AppCompatActivity  {
    private TextView orderID, total, estado;
    private SwitchCompat cobrado;
    private Button btCompletar, btEntregar;
    private Order order;
    private Toolbar toolbar;
    private int position;
    private HashMap<String, String> body = new HashMap<>();
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    private View mProgressView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderItemAdaptador adaptador;
    private Boolean small;

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
        this.position = intent.getIntExtra("i", -1);
        this.context = getApplicationContext();
        this.recyclerView = findViewById(R.id.recycler_view);
        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setLayoutManager(layoutManager);
        this.orderID = findViewById(R.id.orderID);
        this.estado = findViewById(R.id.estado);
        this.total = findViewById(R.id.total);
        this.btCompletar = findViewById(R.id.btCompletarAct);
        this.btEntregar = findViewById(R.id.btEntregarAct);
        this.cobrado = findViewById(R.id.cobrado);
        this.mProgressView = findViewById(R.id.login_progress);
        this.adaptador = new OrderItemAdaptador(new ArrayList<OrderItem>(), "order");
        this.recyclerView.setAdapter(adaptador);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (position != -1) {
            order = orders.get(position);
            orderID.setText(String.format("Pedido: %s", order.getOrderId()));
            estado.setText(String.format("Estado: %s. Empleado: %s. Tienda: %s. Cliente: %s", order.getState(),order.getEmpleado(), order.getStore(), order.getCustomer()));
            if (!order.getVoluntario().isEmpty()){
                estado.append("   Voluntario: " + order.getVoluntario());
            }
            total.setText(String.format("Importe: %S €", order.getTotalasString()));
            cobrado.setChecked(order.getPagado());
            adaptador.updateOrderItems(order.getOrderItems());

            btCompletar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    body.put("state", "deliver");
                    Completar(order.getOrderId());
                }
            });
            btEntregar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    body.put("state", "completed");
                    Completar(order.getOrderId());
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Orden no recibida", Toast.LENGTH_LONG).show();
        }
    }

    public void Completar(String id) {
        body.put("id", id);
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
    }

    public void Cancelar(View v) {
        setResult(0);
        finish();
    }

    Callback<ResponseBody> Completarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Pedido completado", Toast.LENGTH_LONG).show();
                setResult(1);
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
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(OrderDetailActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
