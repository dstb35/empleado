package net.benoodle.empleado;

import static android.view.View.GONE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static net.benoodle.empleado.MainActivity.REQUEST_CODE;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.order;
import static net.benoodle.empleado.ShopActivity.lastOrder;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartActivity extends AppCompatActivity implements CartAdapter.EliminarListener {

    private CartAdapter adaptador;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private HashMap<String, Object> body = new HashMap<>();
    private User user;
    private TextView Btotal, txtCambio;
    private Context context;
    private boolean semaforo;
    private View mProgressView;
    private SwitchCompat swcobrado;
    //private int pos;
    private Button btComprar, btReiniciar, btCalcular;
    private float cambio, ingresos;

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
        this.btComprar = findViewById(R.id.Btcomprar);
        this.btCalcular = findViewById(R.id.btCalcular);
        this.btReiniciar = findViewById(R.id.btReiniciar);
        if (sharedPrefManager.getSPEncargado()) {
            swcobrado.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    order.setPagado(isChecked);
                }
            });
        } else {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        this.semaforo = TRUE;
        this.cambio = 0;
        this.txtCambio = findViewById(R.id.txtCambio);
        if (sharedPrefManager.getSPAutoprint()) {
            if (!Printooth.INSTANCE.hasPairedPrinter()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("¿Quieres conectar una impresora?");
                builder.setCancelable(true);
                builder.setPositiveButton("Si",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    if ((ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED))  {
                                        ActivityCompat.requestPermissions(CartActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 101);
                                    }else{
                                        startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                                    }
                                }else{
                                    startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                                }
                            }
                        });
                builder.setNegativeButton("No",new DialogInterface.OnClickListener()
                            {
                                public void onClick (DialogInterface dialog,int which){
                                dialog.dismiss();
                                Toast.makeText(context, "Puedes desactivar la impresión de tickets en preferencias.", Toast.LENGTH_SHORT).show();
                            }
                            });
                            AlertDialog dialog = builder.create();
                dialog.show();
                        }
            }
            CambiarAdaptador();
            actualizarTotal();
            if (order.getOrderItems().size() == 0) {
                Toast.makeText(context, getResources().getString(R.string.cart_empty), Toast.LENGTH_LONG).show();
                findViewById(R.id.Btcomprar).setVisibility(GONE);
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(context, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
            } else {
                Toast.makeText(CartActivity.this, "Se necesita el permiso bluetooth para buscar impresoras", Toast.LENGTH_SHORT).show();
            }
        }
    }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
                Toast.makeText(context, "Impresora encontrada", Toast.LENGTH_LONG).show();
            } else if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode != Activity.RESULT_OK) {
                Toast.makeText(context, "No se encontraron impresoras", Toast.LENGTH_LONG).show();
            }
        }

        public void CambiarAdaptador () {
            if (order.getOrderItems().isEmpty()) {
                finish();
            }
            adaptador = new CartAdapter(CartActivity.this, this);
            recyclerView.setAdapter(adaptador);
        }

        /*Método asociado al botón Comprar*/
        public void Comprar (View v){
            if (semaforo) {
                btComprar.setClickable(FALSE);
                semaforo = FALSE;
                mProgressView.setVisibility(View.VISIBLE);
                order.setEmail(sharedPrefManager.getSPEmail());
                order.setState("deliver");
                order.setImpreso("1");
                Map<String, Integer> totalQuantity = order.calculateTotalsQuantity();
                body.put("totalQuantity", totalQuantity);
                body.put("order", order);
                user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName());
                body.put("user", user);
                mApiService.addOrder(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Ordercallback);
            }
        }

        /*Método asociado al botón Catalog para volver a la página de productos, acitivityMain*/
        public void Catalog (View v){
            finish();
        }

        /*Métido asocidado al botón Tengo un cupón para validar el cupón*/
        public void Docuppon (View v){
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
                            Toast.makeText(context, R.string.cuppon_product, Toast.LENGTH_LONG).show();
                        } else if (order.applyCuppon(cuppon)) {
                            actualizarTotal();
                            Toast.makeText(context, R.string.cuppon_ok, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, R.string.cuppon_already_use, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                        //Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Cuppon> call, Throwable t) {
                mProgressView.setVisibility(View.GONE);
                Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show();
            }
        };

        final Callback<ResponseBody> Ordercallback = new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String id = "";
                mProgressView.setVisibility(GONE);
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonRESULTS = new JSONObject(response.body().string());
                        id = jsonRESULTS.getJSONArray("order_id").getJSONObject(0).getString("value");
                        lastOrder = order;
                        lastOrder.setOrderId(id);
                        lastOrder.setPagado(true);
                        if (sharedPrefManager.getSPAutoprint()) {
                            MainActivity.print(lastOrder, context, sharedPrefManager.getSPCopies());
                        }
                        order = new Order(sharedPrefManager.getSPStore());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!CartActivity.this.isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle(String.format("Pedido nº: %s", id));
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                } else if (response.code() == 409) {
                    semaforo = TRUE;
                    btComprar.setClickable(TRUE);
                    //En el server cuando algún producto haya sido despublicado durante una compra
                    //se lanza el error 409 con los id's que no se pueden comprar para quitarlos del carrito
                    try {
                        JSONArray jObjError = new JSONArray(response.errorBody().string());
                        for (int i = 0; i < jObjError.length(); i++) {
                            JSONObject node = (JSONObject) jObjError.get(i);
                            catalog.actualizarStock(node.get("id").toString(), node.get("stock").toString());
                            order.removeOrderItemByStock(node.get("id").toString());
                        }
                        actualizarTotal();
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
                        Toast.makeText(context, jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                        order.setOrderId("");
                    } catch (Exception e) {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                mProgressView.setVisibility(GONE);
                semaforo = TRUE;
                btComprar.setClickable(TRUE);
            }
        };

        @Override
        public void eliminar ( int i){
            try {
                order.removeOrderItem(i);
            } catch (NoSuchElementException e) {
                Toast.makeText(context, getResources().getString(R.string.cuppon_removed), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            if (order.getOrderItems().isEmpty()) {
                finish();
            }
        }

        @Override
        public void actualizarTotal () {
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
        public void anadir (String productID,int quantity, Boolean menu,int i){
            try {
                if (menu && quantity > 0) {
                    //Ahora al añadir menus será una copia exacta, no se pedirán opciones
                    ArrayList<String> selecciones = order.getOrderItem(i).getSelecciones();
                    order.addMenuItem(productID, selecciones, quantity);
                } else if (!menu) {
                    order.addOrderItem(catalog.getNodeById(productID), quantity);
                } else if (menu && quantity < 0) {
                    this.eliminar(i);
                }
            } catch (NoSuchElementException e) {
                Toast.makeText(context, getResources().getString(R.string.cuppon_removed), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
            }
            //Podría ocurrir que con el botón menos lleguemos a no tener orderItems en el carrito
            if (order.getOrderItems().isEmpty()) {
                finish();
            }
        }

        //Modifica una selección de menú de un orderItem. Pos es la posición de la selección dentro del orderiTem
        //i es la posición del node dentro de order
        @Override
        public void modificar (Node node,int pos, int i){
            ArrayList<Node> opciones = new ArrayList<>();
            //mirar que no sea el menú be Noodle y se esté modificando el kakigori del final del menú
            if ((node.getProductID().compareTo(MainActivity.MENU_BE_NOODLE_KAKIGORI) == 0) && (pos == order.getOrderItems().size() - 1)) {
                opciones = catalog.getKakigoris();
            } else {
                opciones = catalog.OpcionesMenu(node.getType());
            }
            try {
                if (opciones.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock) + node.getType(), Toast.LENGTH_SHORT).show();
                } else {
                    String[] titulos = new String[opciones.size()];
                    for (int j = 0; j < opciones.size(); j++) {
                        titulos[j] = opciones.get(j).getTitle();
                    }
                    if (!CartActivity.this.isFinishing()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyCustomVerticalScrollBar));
                        builder.setTitle(getResources().getString(R.string.choose) + " " + node.getType());
                        builder.setCancelable(false);
                        ArrayList<Node> finalOpciones = opciones;
                        builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int position) {
                                //La posición de productos[] debería coincidir con la posición de opciones.
                                String newSele = finalOpciones.get(position).getProductID();
                                order.getOrderItem(i).getSelecciones().set(pos, newSele);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        dialog.getListView().setScrollbarFadingEnabled(false);
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.product_not_id), Toast.LENGTH_SHORT).show();
            }
        }

        public void sumar (View v){
            ingresos += Float.parseFloat(v.getTag().toString());
            try {
                cambio = ingresos - order.getTotal();
                setCambio();
            } catch (Exception e) {
                txtCambio.setText("Error ");
            }
        }

        public void setCambio () {
            if (cambio > 0) {
                txtCambio.setText(String.format("Cambio: %s", cambio));
            } else if (cambio < 0) {
                txtCambio.setText(String.format("Faltan: %s", cambio));
            } else {
                txtCambio.setText("0 €");
            }
        }

        public void calcularCambio (View v){
            AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
            builder.setTitle(String.format("Introduce ingreso"));
            builder.setCancelable(false);
            final EditText input = new EditText(CartActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setCancelable(true);
            builder.setPositiveButton("Calcular",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ingresos = Float.parseFloat(String.valueOf(input.getText()));
                            try {
                                cambio = ingresos - order.getTotal();
                                ingresos = 0;
                                setCambio();
                            } catch (Exception e) {
                                txtCambio.setText("Error ");
                            }
                            dialog.dismiss();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        public void reiniciar (View v){
            ingresos = 0;
            cambio = 0;
            txtCambio.setText("");
        }
    }

