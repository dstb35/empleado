package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.orders;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
//import android.widget.Switch;
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
    private TextView orderID, total, estado;
    private SwitchCompat cobrado;
    private Button btCompletar, btEntregar;
    private Order order;
    private Toolbar toolbar;
    private int position;
    private HashMap<String, String> body = new HashMap<>();
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private LinearLayout items;
    private Context context;
    private Double itemWidth;
    private View mProgressView;
    private String string;
    private LinearLayout.LayoutParams lp;

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
        this.context = getApplicationContext();
        this.orderID = findViewById(R.id.orderID);
        //this.customer = findViewById(R.id.customer);
        this.estado = findViewById(R.id.estado);
        this.total = findViewById(R.id.total);
        this.btCompletar = findViewById(R.id.btCompletarAct);
        this.btEntregar =  findViewById(R.id.btEntregarAct);
        this.items = findViewById(R.id.items);
        this.cobrado = findViewById(R.id.cobrado);
        this.mProgressView = findViewById(R.id.login_progress);
        this.lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16, 8, 8, 0);
        //El observer es porque no tengo el width del layout hasta que no se cargue.
        items.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                float width = (float) items.getWidth();
                itemWidth = width*0.75;
                if (position != -1) {
                    order = orders.get(position);
                    orderID.setText("Pedido: "+order.getOrderId());
                    estado.setText("Estado: "+order.getState()+". Empleado: "+order.getEmpleado()+". Tienda: "+order.getStore()+" . Cliente: "+order.getCustomer());
                    //customer.setText(order.getCustomer());
                    total.setText("Total: "+order.getTotal()+" €");
                    cobrado.setChecked(order.getPagado());
                    int j= 1;
                    for (OrderItem orderitem : order.getOrderItems()) {
                        string = "";
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        itemLayout.setGravity(Gravity.CENTER);
                        TextView item = new TextView(context);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.setTextAppearance(R.style.MyCustomTextView);
                            item.setAutoSizeTextTypeUniformWithConfiguration(14, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }
                        //item.setWidth(itemWidth.intValue());
                        item.setLayoutParams(new LinearLayout.LayoutParams(itemWidth.intValue(), LinearLayout.LayoutParams.WRAP_CONTENT));
                        //item.setLayoutParams(lp);
                        try {
                            string = string.concat(j +". Producto :" + catalog.getNodeById(orderitem.getProductID()).getTitle() + " Cantidad: " + orderitem.getQuantity() + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            string = string.concat("Producto :" + e.getLocalizedMessage());
                        }
                        if (orderitem.getSelecciones() != null) {
                            string = string.concat("Seleccion menú :");
                            for (String seleccion : orderitem.getSelecciones()) {
                                try {
                                    string = string.concat(catalog.getNodeById(seleccion).getTitle() + ". ");
                                } catch (Exception e) {
                                    string = string.concat(e.getLocalizedMessage() + " ");
                                }
                            }
                            string = string.concat(System.getProperty("line.separator"));
                        }
                        string = string.concat(System.getProperty("line.separator"));
                        item.setText(string);
                        CheckBox checkBox = new CheckBox(context);
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                                if (isChecked){
                                    item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                } else{
                                    item.setPaintFlags(item.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                                }
                            }
                        });
                        itemLayout.addView(item);
                        itemLayout.addView(checkBox);
                        items.addView(itemLayout);
                        j++;
                    }
                    btCompletar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            body.put("state", "deliver");
                            Completar(order.getOrderId(), position);
                        }
                    });
                    btEntregar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            body.put("state", "completed");
                            Completar(order.getOrderId(), position);
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Orden no recibida", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void Completar (String id, int i){
        body.put("id", id);
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
    }

    public void Cancelar (View v) {
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
