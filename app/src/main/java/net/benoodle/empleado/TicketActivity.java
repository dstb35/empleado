package net.benoodle.empleado;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.PairedPrinter;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.data.printer.Printer;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Bluetooth;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.empleado.MainActivity.catalog;

public class TicketActivity extends AppCompatActivity {
    private TextView orderID, total, estado;
    private Button btModificar, btCompletar;
    private SwitchCompat cobrado;
    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private String id; //store_id;
    private Order order;
    private Context context;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    //private HashMap<String, Object> body = new HashMap<>();
    //private ArrayList<OrderItem>  orderItemsDelete = new ArrayList<>();
    //private Printing printing = null;
    //PrintingCallback printingCallback = null;
    private OrderItemAdaptador adaptador;
    private int numCopias;

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
        this.recyclerView = findViewById(R.id.recycler_view);
        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setLayoutManager(layoutManager);
        this.adaptador = new OrderItemAdaptador(new ArrayList<OrderItem>(), "imprimir");
        this.recyclerView.setAdapter(adaptador);
        this.context = getApplicationContext();
        //this.store_id = sharedPrefManager.getSPStore();
        this.orderID = findViewById(R.id.orderID);
        //this.customer = findViewById(R.id.customer);
        this.total = findViewById(R.id.total);
        this.btModificar = findViewById(R.id.btCompletarAct);
        this.btModificar.setText("Imprimir");
        this.estado = findViewById(R.id.estado);
        this.cobrado = findViewById(R.id.cobrado);
        this.cobrado.setClickable(false);
        this.btCompletar = findViewById(R.id.btEntregarAct);
        this.btCompletar.setVisibility(View.GONE);
        this.numCopias = sharedPrefManager.getSPCopies();
        Printooth.INSTANCE.init(this);
        pedirOrder();
        if (!Printooth.INSTANCE.hasPairedPrinter()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
            builder.setTitle("¿Quieres conectar una impresora?");
            builder.setCancelable(true);
            builder.setPositiveButton("Si",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                        }
                    });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        /*if (Printooth.INSTANCE.hasPairedPrinter()) {
            printer = Printooth.INSTANCE.getPairedPrinter();
            this.initListeners();
            pedirOrder();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
            builder.setTitle("¿Quieres conectar una impresora?");
            builder.setCancelable(true);
            builder.setPositiveButton("Si",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                        }
                    });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }*/

    }

    private void pedirOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
        final EditText input = new EditText(TicketActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setTitle("Introduce  el número de pedido que quieres imprimir : ");
        builder.setCancelable(true);
        builder.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        id = input.getText().toString();
                        mApiService.getOrders(id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Ordercallback);
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

    /*private void initListeners () {
        if (printing != null && printingCallback == null) {
            this.printingCallback = new PrintingCallback() {
                public void connectingWithPrinter() {
                    Toast.makeText(getApplicationContext(), "Conectando a la impresora", Toast.LENGTH_SHORT).show();
                }

                public void printingOrderSentSuccessfully() {
                    Toast.makeText(getApplicationContext(), "Conexión realizada", Toast.LENGTH_SHORT).show();
                }

                public void connectionFailed(@NonNull String error) {
                    Toast.makeText(getApplicationContext(), "Conexión fallida :" + error, Toast.LENGTH_SHORT).show();
                }

                public void onError(@NonNull String error) {
                    Toast.makeText(getApplicationContext(), "Error :" + error, Toast.LENGTH_SHORT).show();
                }

                public void onMessage(@NonNull String message) {
                    Toast.makeText(getApplicationContext(), "Mensaje :" + message, Toast.LENGTH_SHORT).show();
                }
            };
            Printooth.INSTANCE.printer().setPrintingCallback(printingCallback);
        }
    }*/

    Callback<ArrayList<Order>> Ordercallback = new Callback<ArrayList<Order>>() {
        @Override
        public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
            try {
                if (response.isSuccessful()) {
                    ArrayList<Order> orders = response.body();
                    order = orders.get(0);
                    orderID.setText(String.format("Pedido: %s", order.getOrderId()));
                    estado.setText(String.format("Estado: %s. Empleado: %s. Tienda: %s. Cliente: %s", order.getState(), order.getEmpleado(), order.getStore(), order.getCustomer()));
                    total.setText(String.format("Importe: %S €", order.getTotalasString()));
                    cobrado.setChecked(order.getPagado());
                    adaptador.updateOrderItems(order.getOrderItems());
                    /*LinearLayout items = findViewById(R.id.items);
                    items.removeAllViews();
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    //Anchura del layout para dividir el espacio
                    float width = (float)items.getWidth();
                    Double itemWidth = width*0.75;
                    Double quantityWidth = width*0.25;
                    orderID.setText("Número de pedido: "+order.getOrderId());
                    estado.setText("Estado: "+order.getState()+". Empleado: "+order.getEmpleado()+". Tienda: "+order.getStore()+" . Cliente: "+order.getCustomer());
                    //customer.setText(order.getCustomer());
                    total.setText("Total: "+order.getTotalasString()+" €");
                    cobrado.setChecked(order.getPagado());
                    for (int j=0; j<order.getOrderItems().size(); j++){
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        TextView item = new TextView(context);
                        item.setWidth(itemWidth.intValue());
                        TextView quantity = new TextView(context);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            item.setTextAppearance(R.style.MyCustomTextView);
                            quantity.setTextAppearance(R.style.MyCustomTextView);
                        }
                        quantity.setMaxWidth(quantityWidth.intValue());
                        quantity.setMinWidth(80);
                        StringBuilder string = new StringBuilder();
                        OrderItem orderitem = order.getOrderItems().get(j);
                        try {
                            string.append(j+1+". Producto: " + catalog.getNodeById(orderitem.getProductID()).getTitle() + System.getProperty("line.separator"));
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
                        item.setText(string.toString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.setAutoSizeTextTypeUniformWithConfiguration(20, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }
                        itemLayout.addView(item);
                        itemLayout.addView(quantity);
                        items.addView(itemLayout);
                    }*/
                    btModificar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!order.getPagado()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
                                builder.setTitle("El pedido que vas a imprimir no está pagado. Primero cobra y modifica el pedido");
                                builder.setCancelable(false);
                                builder.setPositiveButton("Aceptar",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                MainActivity.print(order, context, sharedPrefManager.getSPCopies());
                                finish();
                            }
                        }
                    });
                } else {
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
        public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
            Toast.makeText(TicketActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void Cancelar(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Impresora encontrada", Toast.LENGTH_LONG).show();
            pedirOrder();
        } else {
            Toast.makeText(getApplicationContext(), "No se encontraron impresoras", Toast.LENGTH_LONG).show();
        }
    }
}
