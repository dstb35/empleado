package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ReasignarActivity extends AppCompatActivity implements ReasignarAdaptador.ModificarListener {
    private TextView orderID, total, estado;
    private Button btModificar, btAsignar;
    private SwitchCompat cobrado;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private String id, store_id;
    private Order order;
    private Context context;
    private HashMap<String, Object> body = new HashMap<>();
    private ArrayList<OrderItem> orderItemsDelete = new ArrayList<>();
    private View mProgressView;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ReasignarAdaptador adaptador;

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
        recyclerView = findViewById(R.id.recycler_view);
        //recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        this.orderID = findViewById(R.id.orderID);
        //this.customer = findViewById(R.id.customer);
        this.total = findViewById(R.id.total);
        this.estado = findViewById(R.id.estado);
        this.btModificar = findViewById(R.id.btCompletarAct);
        this.btModificar.setText("Modificar");
        this.btAsignar = findViewById(R.id.btEntregarAct);
        this.btAsignar.setText("Asignarme pedido");
        this.cobrado = findViewById(R.id.cobrado);
        this.cobrado.setClickable(true);
        //this.order = new Order();
        this.mProgressView = findViewById(R.id.login_progress);
        askPedido();
    }

    public void askPedido() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReasignarActivity.this);
        final EditText input = new EditText(ReasignarActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setMessage("Introduce  el número de pedido que quieras modificar.");
        builder.setCancelable(true);
        builder.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        id = input.getText().toString();
                        if (id.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "El número de pedido no puede estar vacío", Toast.LENGTH_SHORT).show();
                            askPedido();
                        } else {
                            mApiService.getOrders(id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Ordercallback);
                        }
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

    public void reasignar() {
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.reasignar(id, store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Reasignarcallback);
    }

    Callback<ArrayList<Order>> Ordercallback = new Callback<ArrayList<Order>>() {
        @Override
        public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    //Toast.makeText(context, "Pedido asignado", Toast.LENGTH_SHORT).show();
                    ArrayList<Order> orders = response.body();
                    order = orders.get(0);
                    adaptador = new ReasignarAdaptador(order.getOrderItems(), context, ReasignarActivity.this);
                    recyclerView.setAdapter(adaptador);
                    /*LinearLayout items = findViewById(R.id.items);
                    items.removeAllViews();
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //Anchura del layout para dividir el espacio
                    Float width = new Float(items.getWidth());
                    Double itemWidth = width*0.75;
                    Double quantityWidth = width*0.25;*/
                    //orderID.setText("Pedido: " + order.getOrderId());
                    //customer.setText(order.getCustomer());
                    //estado.setText("Estado: " + order.getState() + ". Empleado: " + order.getEmpleado() + ". Tienda: " + order.getStore() + " . Cliente: " + order.getCustomer());
                    //total.setText("Total: " + order.getTotalasString() + " €");
                    orderID.setText(String.format("Pedido: %s", order.getOrderId()));
                    estado.setText(String.format("Estado: %s. Empleado: %s. Tienda: %s. Cliente: %s", order.getState(),order.getEmpleado(), order.getStore(), order.getCustomer()));
                    if (!order.getVoluntario().isEmpty()){
                        estado.append("   Voluntario: " + order.getVoluntario());
                    }
                    total.setText(String.format("Total: %s €", order.getTotalasString()));
                    cobrado.setChecked(order.getPagado());
                    if (sharedPrefManager.getSPEncargado()) {
                        cobrado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                order.setPagado(compoundButton.isChecked());
                            }
                        });
                    } else {
                        cobrado.setClickable(false);
                    }
                    /*for (int j=0; j<order.getOrderItems().size(); j++){
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            quantity.setTextAppearance(R.style.MyCustomEditText);
                            item.setTextAppearance(R.style.MyCustomTextView);
                        }
                        StringBuilder string = new StringBuilder();
                        OrderItem orderitem = order.getOrderItems().get(j);
                        try {
                            string.append(j+1+". Producto: " + catalog.getNodeById(orderitem.getProductID()).getTitle() + " Cantidad: " + orderitem.getQuantity() + System.getProperty("line.separator"));
                        } catch (Exception e) {
                            string.append(j+1+". Producto: " + e.getLocalizedMessage() + System.getProperty("line.separator"));
                        }
                        if (orderitem.getSelecciones() != null) {
                            string.append("Seleccion menú: ");
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
                                float price = catalog.getPriceById(orderitem.getProductID());
                                try {
                                    newQuantity = Integer.parseInt(charSequence.toString());
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.setAutoSizeTextTypeUniformWithConfiguration(20, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }
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
                        btEliminar.setBackgroundDrawable(getDrawable(R.drawable.button));
                        btEliminar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                                float sum = (float) 0;
                                try{
                                    float price = catalog.getPriceById(orderitem.getProductID());
                                    sum = price * orderitem.getQuantity();
                                } catch (Exception e){
                                    e.getLocalizedMessage();
                                }
                                if (isChecked){
                                    orderItemsDelete.add(orderitem);
                                    item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
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
                    }*/

                    btModificar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ConfirmarCambios();
                        }
                    });
                    btAsignar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reasignar();
                        }
                    });
                } else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    askPedido();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                askPedido();
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(ReasignarActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            askPedido();
        }
    };

    public void ConfirmarCambios() {
        order.removeOrderItem(orderItemsDelete);
        order.removeOrderItemsOnZero();
        body.put("order", order);
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.modificar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
    }

    /*Float sum es la cantidad a sumar o restar, signo es true para sumar y false para restar*/
    public void ActualizarTotal() {
        /*Float totalValue = Float.parseFloat(total.getText().toString().replaceAll("[^0-9\\.]", ""));
        if (signo) {
            total.setText(String.format("%s%s €", "Total: ", totalValue + sum));
        } else {
            total.setText(String.format("%s%s €", "Total: ", totalValue - sum));
        }
        order.changeQuantity(pos, newQuantity);*/
        try {
            float price = order.recalculateTotal();
            float minus = (float) 0;
            for (OrderItem orderitem : orderItemsDelete) {
                minus += catalog.getPriceById(orderitem.getProductID())*orderitem.getQuantity();
            }
            price -= minus;
            total.setText(String.format("%s%.02f €", "Total: ", price));
                /*Float oldSum = price * orderitem.getQuantity();
                Float newSum = price * newQuantity;

                modificarListener.ActualizarTotal(i, newQuantity, oldSum - newSum, false); */
            //total.setText(String.format("%s%.02f €", "Total: ", order.recalculateTotal()));
        } catch (Exception e) {
            total.setText(e.getLocalizedMessage());
        }
    }

    public void Cancelar(View v) {
        finish();
    }

    @Override
    public void EliminarOrderItem(int pos) {
        orderItemsDelete.add(order.getOrderItems().get(pos));
        ActualizarTotal();
    }

    @Override
    public void AnadirOrderItem(int pos) {
        orderItemsDelete.remove(order.getOrderItems().get(pos));
        ActualizarTotal();
    }

    @Override
    public void ModificarOrderItem(int pos, int quantity) {
        order.getOrderItems().get(pos).setQuantity(quantity);
        ActualizarTotal();
    }

    Callback<Order> Reasignarcallback = new Callback<Order>() {
        @Override
        public void onResponse(Call<Order> call, Response<Order> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Order newOrder = response.body();
                //estado.setText("Estado: " + newOrder.getState() + ". Empleado: " + newOrder.getEmpleado() + ". Tienda: " + newOrder.getStore());
                estado.setText(String.format("Estado: %s . Empleado: %s . Tienda %s . Cliente: %s .", newOrder.getState(), newOrder.getEmpleado(), newOrder.getStore(), newOrder.getCustomer()));
                if (!order.getVoluntario().isEmpty()){
                    estado.append("   Voluntario: " + order.getVoluntario());
                }
                Toast.makeText(getApplicationContext(), "Pedido asignado", Toast.LENGTH_LONG).show();
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
        public void onFailure(Call<Order> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(ReasignarActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<Order> Completarcallback = new Callback<Order>() {
        @Override
        public void onResponse(Call<Order> call, Response<Order> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Order newOrder = response.body();
                estado.setText("Estado: " + newOrder.getState() + ". Empleado: " + newOrder.getEmpleado() + ". Tienda: " + newOrder.getStore());
                Toast.makeText(getApplicationContext(), "Pedido modificado", Toast.LENGTH_LONG).show();
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
        public void onFailure(Call<Order> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(ReasignarActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
