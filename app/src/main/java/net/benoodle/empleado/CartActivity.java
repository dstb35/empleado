package net.benoodle.empleado;

import static android.view.View.GONE;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.ShopActivity.order;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.ui.ScanningActivity;

import net.benoodle.empleado.model.Cuppon;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.model.User;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;


public class CartActivity extends AppCompatActivity implements CartAdapter.EliminarListener {

    private CartAdapter adaptador;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private HashMap<String, Object> body = new HashMap<>();
    private User user;
    private TextView Btotal;
    private Context context;
    private boolean semaforo;
    private View mProgressView;
    private SwitchCompat swcobrado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cart);
        this.context = getApplicationContext();
        recyclerView = findViewById(R.id.recycler_view);
        swcobrado = findViewById(R.id.swCobrado);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(CartActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        swcobrado.setChecked(sharedPrefManager.getSPEncargado());
        order.setPagado(sharedPrefManager.getSPEncargado());
        if (sharedPrefManager.getSPEncargado()) {
            swcobrado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    order.setPagado(isChecked);
                }
            });
        }else{
            swcobrado.setClickable(false);
        }
        mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        this.Btotal = findViewById(R.id.Btotal);
        mProgressView = findViewById(R.id.login_progress);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
        if (sharedPrefManager.getSPAutoprint()) {
            Printooth.INSTANCE.init(this);
            if (!Printooth.INSTANCE.hasPairedPrinter()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
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
                        Toast.makeText(context, "Puedes desactivar la impresión de tickets en preferencias.", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.semaforo = TRUE;
        CambiarAdaptador();
        ActualizarTotal();
        if (order.getOrderItems().size() == 0) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.cart_empty), Toast.LENGTH_LONG).show();
            findViewById(R.id.Btcomprar).setVisibility(GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Impresora encontrada", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "No se encontraron impresoras", Toast.LENGTH_LONG).show();
        }
    }

    public void CambiarAdaptador() {
        if (order.getOrderItems().isEmpty()) {
            finish();
        }
        adaptador = new CartAdapter(CartActivity.this, this);
        recyclerView.setAdapter(adaptador);
    }

    /*Método asociado al botón Comprar*/
    public void Comprar(View v) {
        if (semaforo) {
            semaforo = FALSE;
            mProgressView.setVisibility(View.VISIBLE);
            order.setEmail(sharedPrefManager.getSPEmail());
            Map<String, Integer> totalQuantity = order.calculateTotalsQuantity();
            body.put("totalQuantity", totalQuantity);
            body.put("order", order);
            user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName());
            body.put("user", user);
            mApiService.addOrder(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Ordercallback);
        }
    }

    /*Método asociado al botón Catalog para volver a la página de productos, acitivityMain*/
    public void Catalog(View v) {
        finish();
    }

    /*Métido asocidado al botón Tengo un cupón para validar el cupón*/
    public void Docuppon(View v) {
        Cuppon cuppon = new Cuppon();
        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
        final EditText input = new EditText(CartActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setTitle(R.string.cuppon);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.accept,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cuppon.setCuppon(input.getText().toString());
                        cuppon.setOp("validate");
                        mProgressView.setVisibility(View.VISIBLE);
                        mApiService.addCuppon(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), cuppon).enqueue(Cupponcallback);
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    Callback<Cuppon> Cupponcallback = new Callback<Cuppon>() {
        @Override
        public void onResponse(Call<Cuppon> call, Response<Cuppon> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    Cuppon cuppon = response.body();
                    if ((cuppon.getType().compareTo("product") == 0) && (!order.orderContainsProduct(cuppon.getProduct().toString()))) {
                        Toast.makeText(getApplicationContext(), R.string.cuppon_product, Toast.LENGTH_LONG).show();
                    } else if (order.applyCuppon(cuppon)) {
                        ActualizarTotal();
                        Toast.makeText(getApplicationContext(), R.string.cuppon_ok, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, R.string.cuppon_already_use, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<Cuppon> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(getApplicationContext(), t.toString(), Toast.LENGTH_LONG).show();
        }
    };

    Callback<ResponseBody> Ordercallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            String id = "";
            mProgressView.setVisibility(View.GONE);
            semaforo = TRUE;
            if (response.isSuccessful()) {
                try {
                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                    id = jsonRESULTS.getJSONArray("order_id").getJSONObject(0).getString("value");
                    order.setOrderId(id);
                    if (sharedPrefManager.getSPAutoprint()){
                        MainActivity.print(order, context, sharedPrefManager.getSPCopies());
                    }
                    order = new Order(sharedPrefManager.getSPStore());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                if (sharedPrefManager.getSPModus()) {
                    builder.setTitle(String.format("%s%s", getResources().getString(R.string.ordermoduson), id));
                } else {
                    builder.setTitle(String.format("%s%s", getResources().getString(R.string.ordermodusoff), id));
                }
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.gotIt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (response.code() == 409) {
                //En el server cuando algún producto haya sido despublicado durante una compra
                //se lanza el error 409 con los id's que no se pueden comprar para quitarlos del carrito
                try {
                    JSONArray jObjError = new JSONArray(response.errorBody().string());
                    for (int i = 0; i < jObjError.length(); i++) {
                        JSONObject node = (JSONObject) jObjError.get(i);
                        catalog.actualizarStock(node.get("id").toString(), node.get("stock").toString());
                        order.removeOrderItemByStock(node.get("id").toString());
                    }
                    ActualizarTotal();
                    adaptador.notifyDataSetChanged();
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    TextView textView = new TextView(context);
                    textView.setText(getResources().getString(R.string.removed));
                    builder.setCustomTitle(textView);
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    order.setOrderId("");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            mProgressView.setVisibility(View.GONE);
            semaforo = TRUE;
        }
    };

    @Override
    public void Eliminar(int i) {
        try {
            order.removeOrderItem(i);
        } catch (NoSuchElementException e) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.cuppon_removed), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

        if (order.getOrderItems().isEmpty()) {
            finish();
        }
    }

    @Override
    public void ActualizarTotal() {
        try {
            Btotal.setText("");
            Btotal.setText(String.format("%s%s €", getResources().getString(R.string.total), String.format("%.2f", order.getTotal())));
            //CambiarAdaptador();
        } catch (Exception e) {
            Btotal.setText(e.getLocalizedMessage());
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void Anadir(String productID, int quantity, Boolean menu, int i) {
        try {
            if (menu && quantity > 0) {
                Intent intent = new Intent(this, MenuActivity.class);
                intent.putExtra("id", productID);
                this.startActivity(intent);
            } else if (!menu) {
                order.addOrderItem(productID, quantity);
            } else if (menu && quantity < 0) {
                this.Eliminar(i);
            }
        } catch (NoSuchElementException e) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.cuppon_removed), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
        }
        //Podría ocurrir que con el botón menos lleguemos a no tener orderItems en el carrito
        if (order.getOrderItems().isEmpty()) {
            finish();
        }
    }
}
