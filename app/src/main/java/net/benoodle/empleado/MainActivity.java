package net.benoodle.empleado;

import androidx.annotation.NonNull;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.model.User;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.PrintingImagesHelper;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.data.printer.Printer;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.bluetooth.BluetoothProfile.GATT_SERVER;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.view.View.GONE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends OptionsMenuActivity implements MainAdaptador.AsignarListener {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    public static final String MENU = "menu";
    public static ArrayList<Order> orders = new ArrayList<>();
    public User user;
    public static Catalog catalog;
    private HashMap<String, String> body = new HashMap<>();
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private MainAdaptador adaptador;
    public static String boton = "Asignar";
    private TextView totalPedidos, store;
    private String store_id;
    private SearchView searchView;
    private Context context;
    private int itemSelected, position;
    private Printing printing = null;
    PrintingCallback printingCallback = null;
    private boolean modus, autoassign;
    private View mProgressView;
    public static final int REQUEST_CODE = 1;
    private SwitchCompat swPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        user = new User(sharedPrefManager.getSPEmail(), sharedPrefManager.getSPName(), sharedPrefManager.getSPUserId());
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        //recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.searchView);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        totalPedidos = findViewById(R.id.totalpedidos);
        store = findViewById(R.id.store);
        mProgressView = findViewById(R.id.login_progress);
        this.context = this;
        Printooth.INSTANCE.init(context);
        this.autoassign = sharedPrefManager.getSPAutoassign();
        this.swPause = findViewById(R.id.swPause);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        store_id = sharedPrefManager.getSPStore();
        store.setText("Tienda : " + store_id);
        mApiService.getStock(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
        this.itemSelected = 0;
        swPause.setChecked(sharedPrefManager.getSPAutoassign());
        modus = sharedPrefManager.getSPModus();
        swPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sharedPrefManager.saveSPBoolean(SharedPrefManager.AUTOASSIGN, compoundButton.isChecked());
                autoassign = compoundButton.isChecked();
            }
        });
        //invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.itemSelected = item.getItemId();
        invalidateOptionsMenu();
        searchView.setQuery("", false);
        searchView.clearFocus();
        switch (item.getItemId()) {
            case R.id.getOrdersNoPaid:
                if (Printooth.INSTANCE.hasPairedPrinter()) {
                    printing = Printooth.INSTANCE.printer();
                    initListeners();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                boton = "Cobrar";
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.getSinCobrar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.getOrders:
                boton = "Asignar";
                mProgressView.setVisibility(View.VISIBLE);
                if (autoassign) {
                    body.put("id", "0");
                    body.put("store_id", store_id);
                    mApiService.asignar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Autoaignarcallback);
                } else {
                    mApiService.getSinAsignar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                }
                return true;
            case R.id.MyOrders:
                boton = "Completar";
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.getAsignados(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.EntregarOrders:
                boton = "Entregar";
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.getEntregar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.CompleteOrders:
                boton = "Completados";
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.getComplete(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                return true;
            case R.id.Preferencias:
                //boton = "";
                lanzarPreferencias();
                return true;
            case R.id.Stock:
                //boton = "";
                lanzarStock();
                return true;
            case R.id.Reasignar:
                //boton = "";
                lanzarReasignar();
                return true;
            case R.id.Borrar:
                //boton = "";
                if (sharedPrefManager.getSPEncargado()){
                    Borrar();
                }else{
                    Toast.makeText(context, "Solo los encargados pueden borrar pedidos", Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.Tracing:
                //boton = "";
                lanzarTracing();
                return true;
            case R.id.Tickets:
                //boton = "";
                lanzarTicket();
                return true;
            case R.id.Store:
                //boton = "";
                lanzarStore();
                return true;
            case R.id.Cuppon:
                //boton = "";
                lanzarCupones();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initListeners() {
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
    }

    public void cambiarAdaptador() {
        if (boton.compareTo("") == 0) {
            totalPedidos.setText("Pedidos : " + String.valueOf(orders.size()));
        } else {
            totalPedidos.setText("Pedidos para " + boton.toLowerCase() + " :" + String.valueOf(orders.size()));
        }
        adaptador = new MainAdaptador(orders, MainActivity.this, MainActivity.this);
        recyclerView.setAdapter(adaptador);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adaptador.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void lanzarPreferencias() {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        this.startActivity(intent);
    }

    public void lanzarStock() {
        Intent intent = new Intent(this, StockActivity.class);
        this.startActivity(intent);
    }

    public void lanzarReasignar() {
        Intent intent = new Intent(this, ReasignarActivity.class);
        this.startActivity(intent);
    }

    public void lanzarTracing() {
        Intent intent = new Intent(this, TracingActivity.class);
        this.startActivity(intent);
    }

    public void lanzarTicket() {
        Intent intent = new Intent(this, TicketActivity.class);
        this.startActivity(intent);
    }

    public void lanzarStore() {
        if (sharedPrefManager.getSPEncargado()){
            Intent intent = new Intent(this, StoreActivity.class);
            this.startActivity(intent);
        }else{
            Toast.makeText(context, "Solo los encargados pueden modificar tiendas", Toast.LENGTH_SHORT).show();
        }
    }

    public void lanzarCupones() {
        if (sharedPrefManager.getSPEncargado()){
            Intent intent = new Intent(this, CupponActivity.class);
            this.startActivity(intent);
        }else{
            Toast.makeText(context, "Solo los encargados pueden crear cupones", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Método que asignar una order a un empleado y lanza la activity con el order.
    Si el resultado es satisfactorio actualizamos el order en la variable orders para
    evitar recargar.
    */
    @Override
    public void Asignar(int i) {
        if ((modus) && (!orders.get(i).getPagado())) {
            Toast.makeText(context, "No se puede asignar un pedido no cobrado en modo cobrar primero", Toast.LENGTH_SHORT).show();
        } else {
            body.put("id", orders.get(i).getOrderId());
            position = i;
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.asignar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Asignarcallback);
        }
    }

    /*Método para cobrar un pedido*/
    @Override
    public void Cobrar(int i) {
        if (sharedPrefManager.getSPEncargado()){
            body.put("id", orders.get(i).getOrderId());
            position = i;
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.cobrar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Cobrarcallback);
        }else{
            Toast.makeText(context, "Solo los encargados pueden cobrar pedidos", Toast.LENGTH_SHORT).show();
        }
    }

    /*Método para marcar como entregado un pedido.*/
    @Override
    public void Entregar(int i) {
        if ((modus) && (!orders.get(i).getPagado())) {
            Toast.makeText(context, "No se puede entregar un pedido no cobrado en modo cobrar primero", Toast.LENGTH_SHORT).show();
        } else {
            body.put("id", orders.get(i).getOrderId());
            position = i;
            body.put("state", "completed");
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Entregarcallback);
        }
    }

    /*Método para borrar pedidos*/
    public void Borrar(){
        body.clear();
        askPedido();
    }

    public void askPedido(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMarginEnd(8);
        lp.setMargins(0, 24, 0, 0);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setMessage("Introduce  el número de pedido que quieras modificar.");
        builder.setCancelable(true);
        builder.setPositiveButton("Aceptar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = input.getText().toString();
                        if (!id.isEmpty()) {
                            body.put(id, id);
                        }
                        if (body.isEmpty()){
                            Toast.makeText(context, "La lista está vacía, no se borraron pedidos", Toast.LENGTH_SHORT).show();
                        }else{
                            mApiService.borrar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body)
                                    .enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.isSuccessful()) {
                                                for (String id : body.values() ) {
                                                    for (Order order : orders){
                                                        if (order.getOrderId().compareTo(id) == 0){
                                                            orders.remove(order);
                                                            break;
                                                        }
                                                    }
                                                }
                                                cambiarAdaptador();
                                                //JSONObject jsonRESULTS = new JSONObject(response.body().toString());
                                                //String id = jsonRESULTS.getJSONArray("order_id").getJSONObject(0).getString("value");
                                                try {
                                                    JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                                    Toast.makeText(getApplicationContext(), jsonRESULTS.get("message").toString(), Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                                }
                                                body.clear();
                                            } else {
                                                try {
                                                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                                                    Toast.makeText(getApplicationContext(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                                                    orders.clear();
                                                    cambiarAdaptador();
                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            Toast.makeText(getApplicationContext(), "Error: "+t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
        builder.setNeutralButton("Añadir otro",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String id = input.getText().toString();
                        if (!id.isEmpty()) {
                            body.put(id, id);
                        }
                        askPedido();
                    }
                });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void Completar(int i) {
        if ((modus) && (!orders.get(i).getPagado())) {
            Toast.makeText(context, "No se puede completar un pedido no cobrado en modo cobrar primero", Toast.LENGTH_SHORT).show();
        } else {
            position = i;
            body.put("state", "deliver");
            body.put("id", orders.get(i).getOrderId());
            mProgressView.setVisibility(View.VISIBLE);
            mApiService.completar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Completarcallback);
        }
    }

    @Override
    public void Ver(int i) {
        if ((modus) && (!orders.get(i).getPagado())) {
            Toast.makeText(context, "No se puede ver un pedido no cobrado en modo cobrar primero", Toast.LENGTH_SHORT).show();
        } else {
            position = i;
            lanzarOrderDetailActivity();
        }
    }

    public void lanzarOrderDetailActivity() {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("i", position);
        startActivityForResult(intent, REQUEST_CODE);
//        this.startActivity(intent);
    }

    public void print(Order order) {
        if (printing != null) {
            StringBuilder string = new StringBuilder();
            string.append(System.getProperty("line.separator"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            string.append(dateFormat.format(Calendar.getInstance().getTime()));
            string.append(System.getProperty("line.separator"));
            string.append("Pedido Nº: " + order.getOrderId());
            string.append(System.getProperty("line.separator"));
            StringBuilder products = new StringBuilder();
            for (OrderItem orderItem : order.getOrderItems()) {
                try {
                    Node node = catalog.getNodeById(orderItem.getProductID());
                    products.append(node.getTitle() + ": " + orderItem.getQuantity());
                } catch (Exception e) {
                    products.append("Producto no encontrado con ID" + orderItem.getProductID() + ": " + orderItem.getQuantity());
                }
                products.append(System.getProperty("line.separator"));
            }
            ArrayList<Printable> al = new ArrayList<>();
            Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
            al.add(new ImagePrintable.Builder(image).build());
            al.add((new TextPrintable.Builder())
                    .setText(string.toString())
                    .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_60())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER())
                    .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_NORMAL())
                    .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_ON())
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setNewLinesAfter(1)
                    .build());
            al.add((new TextPrintable.Builder())
                    .setText(products.toString())
                    .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_LEFT())
                    .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_NORMAL())
                    .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_OFF())
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setNewLinesAfter(1)
                    .build());
            al.add((new TextPrintable.Builder())
                    .setText("Total: " + order.getTotal() + " €")
                    .setLineSpacing(DefaultPrinter.Companion.getLINE_SPACING_30())
                    .setAlignment(DefaultPrinter.Companion.getALIGNMENT_RIGHT())
                    .setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_NORMAL())
                    .setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_OFF())
                    .setCharacterCode(DefaultPrinter.Companion.getCHARCODE_PC1252())
                    .setNewLinesAfter(5)
                    .build());
            printing.print(al);
        } else {
            Toast.makeText(getApplicationContext(), "No hay impresora", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (itemSelected == 0) {
            this.itemSelected = menu.getItem(1).getItemId();
        }
        SpannableString s = new SpannableString(menu.findItem(itemSelected).toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            s.setSpan(new ForegroundColorSpan(getColor(R.color.menu_item_selected)), 0, s.length(), 0);
        }
        menu.findItem(itemSelected).setTitle(s);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Impresora encontrada", Toast.LENGTH_LONG).show();
            initListeners();
        } else if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER) {
            Toast.makeText(getApplicationContext(), "No se encontraron impresoras", Toast.LENGTH_LONG).show();
        } else if (requestCode == MainActivity.REQUEST_CODE && resultCode == 0) {
            autoassign = false;
        } else if (requestCode == MainActivity.REQUEST_CODE && resultCode == 1) {
            autoassign = sharedPrefManager.getSPAutoassign();
        }
    }

    Callback<ArrayList<Order>> Orderscallback = new Callback<ArrayList<Order>>() {
        @Override
        public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    orders = response.body();
                } else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), response.errorBody().string(), Toast.LENGTH_LONG).show();
                    orders.clear();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            cambiarAdaptador();
        }

        @Override
        public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<Order> Asignarcallback = new Callback<Order>() {
        @Override
        public void onResponse(Call<Order> call, Response<Order> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Pedido asignado.", Toast.LENGTH_LONG).show();
                lanzarOrderDetailActivity();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    adaptador.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<Order> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Cobrarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                int copies = sharedPrefManager.getSPCopies();
                for (int i=0; i<copies; i++){
                    print(orders.get(position));
                }
                orders.remove(position); //Lo elimino porque orders son los pedidos sin cobrar.
                Toast.makeText(getApplicationContext(), "Pedido cobrado.", Toast.LENGTH_LONG).show();
                adaptador.notifyDataSetChanged();
                cambiarAdaptador();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    adaptador.notifyDataSetChanged();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                catalog = new Catalog(response.body());
                /*Con el catálogo cargado se pasa a mostrar los pedidos sin asignar*/
                if ((autoassign) && (boton.compareTo("Asignar") == 0)) {
                    body.put("id", "0");
                    body.put("store_id", store_id);
                    mProgressView.setVisibility(View.VISIBLE);
                    mApiService.asignar(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), body).enqueue(Autoaignarcallback);
                } else {
                    cambiarAdaptador();
                    //boton = "Asignar";
                    //mApiService.getSinAsignar(store_id, sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                }
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
        public void onFailure(Call<ArrayList<Node>> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Entregarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Toast.makeText(getApplicationContext(), "Pedido entregado", Toast.LENGTH_LONG).show();
                orders.remove(position);
                cambiarAdaptador();
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
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ResponseBody> Completarcallback = new Callback<ResponseBody>() {
        @Override
        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Toast.makeText(context, "Pedido completado", Toast.LENGTH_LONG).show();
                orders.remove(position);
                cambiarAdaptador();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ResponseBody> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<Order> Autoaignarcallback = new Callback<Order>() {
        @Override
        public void onResponse(Call<Order> call, Response<Order> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                Order order = response.body();
                orders.add(order);
                position = orders.size() - 1;
                Toast.makeText(getApplicationContext(), "Pedido asignado.", Toast.LENGTH_LONG).show();
                lanzarOrderDetailActivity();
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    orders.clear();
                    cambiarAdaptador();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<Order> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            orders.clear();
            cambiarAdaptador();
            Toast.makeText(MainActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
