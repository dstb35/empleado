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
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;

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

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.orders;

public class ReasignarActivity  extends AppCompatActivity {
    private TextView orderID, total;
    private Button btModificar;
    private Switch cobrado;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private String id, store_id;
    private Order order;
    private Context context;
    private HashMap<String, Object> body = new HashMap<>();
    //private ArrayList<Integer> orderItemsDelete = new ArrayList<>();
    private ArrayList<OrderItem>  orderItemsDelete = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_detail);
        sharedPrefManager = new SharedPrefManager(this);
        new SharedPrefManager(this);
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        this.context = getApplicationContext();
        this.store_id = sharedPrefManager.getSPStore();
        this.orderID = findViewById(R.id.orderID);
        this.total = findViewById(R.id.total);
        this.btModificar = findViewById(R.id.btFinalizar);
        this.cobrado = findViewById(R.id.cobrado);
        this.cobrado.setClickable(true);
        AlertDialog.Builder builder = new AlertDialog.Builder(ReasignarActivity.this);
        final EditText input = new EditText(ReasignarActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setTitle("Introduce  el número de pedido que quieras modificar : ");
        builder.setCancelable(true);
        builder.setPositiveButton("Aceptar",
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
                    LinearLayout items = findViewById(R.id.items);
                    items.removeAllViews();
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //Anchura del layout para dividir el espacio
                    Float width = new Float(items.getWidth());
                    Double itemWidth = width*0.75;
                    Double quantityWidth = width*0.25;
                    orderID.setText("Número de pedido: "+order.getOrderId());
                    total.setText("Total: "+order.getTotal()+" €");
                    cobrado.setChecked(order.getPagado());
                    cobrado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            order.setPagado(compoundButton.isChecked());
                        }
                    });
                    for (int j=0; j<order.getOrderItems().size(); j++){
                        final int pos = j;
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        itemLayout.setMinimumHeight(200);
                        TextView item = new TextView(context);
                        item.setWidth(itemWidth.intValue());
                        EditText quantity = new EditText(context);
                        quantity.setMaxWidth(quantityWidth.intValue());
                        quantity.setMinWidth(80);
                        quantity.setInputType(InputType.TYPE_CLASS_NUMBER);
                        StringBuilder string = new StringBuilder();
                        OrderItem orderitem = order.getOrderItems().get(j);
                        try {
                            string.append(j+1+". Producto :" + catalog.getNodeById(orderitem.getProductID()).getTitle() + " Cantidad: " + orderitem.getQuantity() + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            string.append(j+1+". Producto :" + e.getLocalizedMessage() + System.getProperty("line.separator"));
                        }
                        if (orderitem.getSelecciones() != null) {
                            string.append("Seleccion menú :");
                            for (String seleccion : orderitem.getSelecciones()) {
                                try {
                                    string.append(catalog.getNodeById(seleccion).getTitle() + ". ");
                                } catch (Exception e) {
                                    string.append(e.getLocalizedMessage() + " ");
                                }
                            }
                            string.append(System.getProperty("line.separator"));
                        }
                        string.append(System.getProperty("line.separator"));
                        quantity.setText(String.valueOf(orderitem.getQuantity()));
                        quantity.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                                int newQuantity;
                                Float price = catalog.getPriceById(orderitem.getProductID());
                                try {
                                    newQuantity = Integer.valueOf(charSequence.toString());
                                } catch (NumberFormatException e) {
                                    newQuantity = 0;
                                }
                                Float oldSum = price*orderitem.getQuantity();
                                Float newSum = price*newQuantity;
                                ActualizarTotal(oldSum-newSum, false);
                                order.changeQuantity(pos, newQuantity);
                                //ActualizarTotal();
                            }
                            @Override
                            public void afterTextChanged(Editable editable) {
                            }
                        });
                        item.setText(string.toString());
                        item.setAutoSizeTextTypeUniformWithConfiguration(20, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        itemLayout.addView(item);
                        itemLayout.addView(quantity);
                        ToggleButton btEliminar = new ToggleButton(context);
                        LinearLayout.LayoutParams btParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT);
                        LinearLayout btLayout = new LinearLayout(context);
                        btLayout.setOrientation(LinearLayout.HORIZONTAL);
                        btLayout.setLayoutParams(btParams);
                        btLayout.setGravity(Gravity.RIGHT);
                        btEliminar.setText("Borrar");
                        btEliminar.setTextOn("Borrado");
                        btEliminar.setTextOff("Borrar");
                        /*btEliminar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //order.removeOrderItem(pos);
                                btEliminar.setChecked(true);
                                //ActualizarTotal();
                            }
                        });*/
                        btEliminar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                                Float sum = new Float(0);
                                try{
                                    Float price = catalog.getPriceById(orderitem.getProductID());
                                    sum = price * orderitem.getQuantity();
                                } catch (Exception e){
                                    e.getLocalizedMessage();
                                }
                                if (isChecked){
                                    orderItemsDelete.add(orderitem);
                                    item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    //quantity.setClickable(false);
                                    //quantity.setFocusable(false);
                                    quantity.setVisibility(View.INVISIBLE);
                                    ActualizarTotal(sum, false);
                                } else{
                                    orderItemsDelete.remove(orderitem);
                                    item.setPaintFlags(item.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                                    quantity.setVisibility(View.VISIBLE);
                                    ActualizarTotal(sum, true);
                                }
                            }
                        });
                        btLayout.addView(btEliminar);
                        itemLayout.addView(btLayout);
                        items.addView(itemLayout);
                    }
                    btModificar.setText("Modificar");
                    btModificar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           ConfirmarCambios();
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
            Toast.makeText(ReasignarActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void ConfirmarCambios (){
        order.removeOrderItem(orderItemsDelete);
        order.removeOrderItemsOnZero();
        body.put("order", order);
        mApiService.modificar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
    }

    /*public void ActualizarTotal(){
        try {
            order.recalculateTotal();
            total.setText("");
            total.setText(String.format("%s%s €", "Total: ", order.getTotal()));
        }catch (Exception e) {
            total.setText(e.getLocalizedMessage());
        }
    }*/

    /*Float sum es la cantidad a sumar o restar, signo es true para sumar y false para restar*/
    public void ActualizarTotal(float sum, boolean signo){
        Float totalValue = Float.parseFloat(total.getText().toString().replaceAll("[^0-9\\.]", ""));
        if (signo){
            total.setText(String.format("%s%s €", "Total: ", totalValue+sum));
        }else{
            total.setText(String.format("%s%s €", "Total: ", totalValue-sum));
        }
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
            Toast.makeText(ReasignarActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
