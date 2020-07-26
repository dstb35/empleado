package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.orders;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView orderID, total;
    private Switch cobrado;
    private Button btFinalizar;
    private Order order;
    private Toolbar toolbar;
    private int position;
    private HashMap<String, String> body = new HashMap<>();
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private LinearLayout items;
    private Context context;
    private LinearLayout.LayoutParams lp;
    private Double itemWidth;

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
        this.total = findViewById(R.id.total);
        this.btFinalizar = findViewById(R.id.btFinalizar);
        this.items = findViewById(R.id.items);
        this.cobrado = findViewById(R.id.cobrado);
        this.lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        //El observer es porque no tengo el width del layout hasta que no se cargue.
        items.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                items.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Float width = new Float(items.getWidth());
                itemWidth = width*0.75;
                if (position != -1) {
                    order = orders.get(position);
                    orderID.setText("Número de pedido: "+order.getOrderId());
                    total.setText("Total: "+order.getTotal()+" €");
                    cobrado.setChecked(order.getPagado());
                    for (int j=0; j<order.getOrderItems().size(); j++){
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        itemLayout.setMinimumHeight(200);
                        TextView item = new TextView(context);
                        item.setWidth(itemWidth.intValue());
                        item.setAutoSizeTextTypeUniformWithConfiguration(20, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        OrderItem orderitem = order.getOrderItems().get(j);
                        StringBuilder stringBuilder = new StringBuilder();
                        try {
                            stringBuilder.append(j+1+". Producto :" + catalog.getNodeById(orderitem.getProductID()).getTitle() + " Cantidad: " + orderitem.getQuantity() + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            stringBuilder.append("Producto :" + e.getLocalizedMessage());
                        }
                        if (orderitem.getSelecciones() != null) {
                            stringBuilder.append("Seleccion menú :");
                            for (String seleccion : orderitem.getSelecciones()) {
                                try {
                                    stringBuilder.append(catalog.getNodeById(seleccion).getTitle() + ". ");
                                } catch (Exception e) {
                                    stringBuilder.append(e.getLocalizedMessage() + " ");
                                }
                            }
                            stringBuilder.append(System.getProperty("line.separator"));
                        }
                        stringBuilder.append(System.getProperty("line.separator"));
                        item.setText(stringBuilder.toString());
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
                    }
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
        });
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
            Toast.makeText(OrderDetailActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
