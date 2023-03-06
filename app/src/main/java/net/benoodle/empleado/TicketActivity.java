package net.benoodle.empleado;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import okhttp3.ResponseBody;
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
    private String id;
    private Order order;
    private Context context;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderItemAdaptador adaptador;
    private View mProgressView;

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
        this.mProgressView = findViewById(R.id.login_progress);
        this.recyclerView = findViewById(R.id.recycler_view);
        this.layoutManager = new LinearLayoutManager(this);
        this.recyclerView.setLayoutManager(layoutManager);
        this.adaptador = new OrderItemAdaptador(new ArrayList<OrderItem>(), "imprimir");
        this.recyclerView.setAdapter(adaptador);
        this.context = getApplicationContext();
        this.orderID = findViewById(R.id.orderID);
        this.total = findViewById(R.id.total);
        this.btModificar = findViewById(R.id.btCompletarAct);
        this.btModificar.setText("Imprimir");
        this.estado = findViewById(R.id.estado);
        this.cobrado = findViewById(R.id.cobrado);
        this.cobrado.setClickable(false);
        this.btCompletar = findViewById(R.id.btEntregarAct);
        this.btCompletar.setVisibility(View.GONE);
        Printooth.INSTANCE.init(this);
        id = getIntent().getStringExtra("id");
        if (id == null) {
            pedirOrder();
        } else {
            mApiService.getOrders(id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Ordercallback);
        }


        //Version nueva de android 12
        if (!Printooth.INSTANCE.hasPairedPrinter()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
            builder.setTitle("¿Quieres conectar una impresora?");
            builder.setCancelable(true);
            builder.setPositiveButton("Si",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if ((ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)) {
                                    ActivityCompat.requestPermissions(TicketActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 101);
                                } else {
                                    startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                                }
                            } else {
                                startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                            }
                        }
                    });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    //finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //Veriosn nueva de android 12
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
            } else {
                Toast.makeText(context, "Se necesita el permiso bluetooth para buscar impresoras", Toast.LENGTH_SHORT).show();
            }
        }
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
                        mProgressView.setVisibility(View.VISIBLE);
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

    Callback<ArrayList<Order>> Ordercallback = new Callback<ArrayList<Order>>() {
        @Override
        public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    ArrayList<Order> orders = response.body();
                    order = orders.get(0);
                    //El pedido del QR o introducido a mano no pertenece a la tienda que estamos, eso supone stocks diferentes.
                    if (order.getStore().compareTo(sharedPrefManager.getSPStore()) != 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
                        builder.setMessage("Este pedido no pertenece a esta tienda. Cambia la tienda para tener el stock correcto");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    if (order.getImpreso().compareTo("1") == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
                        builder.setMessage("¡ALERTA!");
                        builder.setMessage("Este pedido ya ha sido impreso. Avisa al encargado");
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    orderID.setText(String.format("Pedido: %s", order.getOrderId()));
                    estado.setText(String.format("Estado: %s. Empleado: %s. Tienda: %s. Cliente: %s", order.getState(), order.getEmpleado(), order.getStore(), order.getCustomer()));
                    total.setText(String.format("Importe: %S €", order.getTotalasString()));
                    cobrado.setChecked(order.getPagado());
                    adaptador.updateOrderItems(order.getOrderItems());

                    btModificar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            HashMap<String, String> body = new HashMap<>();
                            body.put("id", id);
                            body.put("state", "deliver");
                            if (!order.getPagado()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(TicketActivity.this);
                                builder.setTitle("El pedido que vas a imprimir no está pagado. ¿Quieres cobrar el pedido?");
                                builder.setCancelable(false);
                                builder.setNegativeButton("No",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                builder.setPositiveButton("Sí",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (sharedPrefManager.getSPEncargado()) {
                                                    mProgressView.setVisibility(View.VISIBLE);
                                                    mApiService.cobrar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Cobrarcallback);
                                                } else {
                                                    Toast.makeText(context, "Solo los encargados pueden cobrar pedidos", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                AlertDialog dialog = builder.create();
                                dialog.show();
                            } else {
                                MainActivity.print(order, context, sharedPrefManager.getSPCopies());
                                body.put("impreso", "1");
                                mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
                                order.setImpreso("1");
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
            mProgressView.setVisibility(View.GONE);
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
            if (id == null) {
                pedirOrder();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No se encontraron impresoras", Toast.LENGTH_LONG).show();
        }
    }

    Callback<ResponseBody> Completarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Pedido impreso.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Cobrarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                if (sharedPrefManager.getSPAutoprint()) {
                    order.setPagado(true);
                    order.setImpreso("1");
                    cobrado.setChecked(true);
                    order.setImpreso("1");
                    MainActivity.print(order, context, sharedPrefManager.getSPCopies());
                }
                Toast.makeText(getApplicationContext(), "Pedido cobrado.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
