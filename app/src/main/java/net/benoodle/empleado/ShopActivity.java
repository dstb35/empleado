package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.order;
//import static net.benoodle.eorder.TypesActivity.order;
//import static net.benoodle.eorder.TypesActivity.tipos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.model.Tipo;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.UtilsApi;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity implements ShopAdaptador.ComprarListener {

    public static final int REQUEST_CODE = 1;
    public static String MENU = "menu";
    public static Order lastOrder;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ShopAdaptador adaptador;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    //private String type;
    private ApiService mApiService;
    private LinearLayout typesLayout, resumenLayout;
    private TextView total;
    private ArrayList<String> typesAvaliable;
    //private CountDownTimer countDownTimer;
    private ArrayList<Tipo> tipos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        this.context = getApplicationContext();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(ShopActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        sharedPrefManager = new SharedPrefManager(this);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        try {
            mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        //this.type = getIntent().getStringExtra("type");
        this.resumenLayout = findViewById(R.id.resumen);
        this.total = findViewById(R.id.total);
        if (order == null) {
            order = new Order(sharedPrefManager.getSPStore());
        }
        typesLayout = findViewById(R.id.main_types_layout);
        if (sharedPrefManager.getSPVoluntarios()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
            final EditText input = new EditText(ShopActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            builder.setView(input);
            builder.setMessage("Introduce  el nombre del voluntario.");
            builder.setCancelable(true);
            builder.setPositiveButton("Aceptar",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String nombre = input.getText().toString();
                            if (nombre.isEmpty()) {
                                Toast.makeText(getApplicationContext(), "El nombre de voluntario no puede estar vacío", Toast.LENGTH_SHORT).show();
                            } else {
                                order.setVoluntario(nombre);
                            }
                        }
                    });
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        /*Cargar todos los tipos de productos por el stock*/
        mApiService.getAllNodes(sharedPrefManager.getSPStore(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
        /*typesLayout.removeAllViews();
        typesLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                typesLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                cargarTypes();
            }
        });
        adaptador = new ShopAdaptador(catalog.TypeCatalog(this.type), ShopActivity.this, ShopActivity.this);
        recyclerView.setAdapter(adaptador);*/
        mApiService.getTypes(sharedPrefManager.getSPBasicAuth(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPCsrfToken()).enqueue(Typescallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarResumen();
    }

    /*public void ShowCart(View v) {
        Intent intent = new Intent(this, CartActivity.class);
        this.startActivity(intent);
    }*/

    public void ShowCart(View v) {
        //Si el resultado es 1 es compra exitosa, recargar el catálogo. Así no machaca el stock del carrito en compras a medias.
        startActivityForResult(new Intent(this, CartActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            order = new Order(sharedPrefManager.getSPStore());
            if (sharedPrefManager.getSPVoluntarios()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
                final EditText input = new EditText(ShopActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setMessage("Introduce  el nombre del voluntario.");
                builder.setCancelable(true);
                builder.setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nombre = input.getText().toString();
                                if (!nombre.isEmpty()) {
                                    order.setVoluntario(nombre);
                                }
                            }
                        });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            /*Cargar todos los tipos de productos por el stock*/
            mApiService.getAllNodes(sharedPrefManager.getSPStore(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
        }
    }

    public void cargarTypes() {
        float height = (float) findViewById(R.id.main_types_scroll).getHeight();
        double imagesHeight = height * 0.60;
        double textHeight = height * 0.25;
        typesAvaliable = catalog.getTypes();
        for (String name : typesAvaliable) {
            for (Tipo tipo : tipos) {
                if (tipo.getId().compareTo(name) == 0) {
                    LinearLayout titlesLayout = new LinearLayout(context);
                    titlesLayout.setOrientation(LinearLayout.VERTICAL);
                    titlesLayout.setMinimumHeight((int) height);
                    titlesLayout.setGravity(Gravity.CENTER);
                    titlesLayout.setPadding(30, 0, 30, 0);
                    ImageView image = new ImageView(context);
                    image.setMaxHeight((int) imagesHeight);
                    image.setId(typesAvaliable.indexOf(name));
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tipo = typesAvaliable.get(v.getId());
                            adaptador = new ShopAdaptador(catalog.TypeCatalog(tipo), context, ShopActivity.this);
                            recyclerView.setAdapter(adaptador);
                        }
                    });
                    try {
                        Picasso.with(context).load(tipo.getUrl()).resize(0, (int) imagesHeight).into(image);
                    } catch (Exception e) {
                        Picasso.with(context).load(tipo.getUrl()).into(image);
                    }

                    titlesLayout.addView(image);
                    TextView text = new TextView(context);
                    text.setText(tipo.getName());
                    text.setHeight((int) textHeight);
                    text.setMinHeight((int) textHeight);
                    text.setGravity(Gravity.CENTER);
                    titlesLayout.addView(text);
                    typesLayout.addView(titlesLayout);
                }
            }
        }
    }

    /*
    Añade al carrito el producto y la cantidad  pasadas.
    Si es un menú pide al usuario las opciones en MenuActivity
    Las opciones vienen de node.productos[] del server.
     */
    @Override
    public void Anadir(Node node, int quantity) {
        if (node.getType().equals(MENU)) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("id", node.getProductID());
            //Con el result llamaremos a adaptador.notifyDataSetChange para que cambie el stock o no
            startActivityForResult(intent, 1);
        } else if (!node.getType().equals(MENU)) {
            try {
                order.addOrderItem(node.getProductID(), quantity);
                Toast.makeText(context, getResources().getString(R.string.product_added), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
            }
        }
        adaptador.notifyDataSetChanged();
        actualizarResumen();
    }

    public void actualizarResumen() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 0, 0, 0);
        resumenLayout.removeAllViews();
        for (OrderItem orderItem : order.getOrderItems()) {
            TextView text = new TextView(context);
            try {
                final Node node = catalog.getNodeById(orderItem.getProductID());
                text.setText(node.getTitle() + " " + orderItem.getQuantity());
            } catch (Exception e) {
                text.setText(e.getMessage());
            }
            text.setLayoutParams(lp);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                text.setAutoSizeTextTypeUniformWithConfiguration(2, 100, 2, TypedValue.COMPLEX_UNIT_DIP);
                TextViewCompat.setAutoSizeTextTypeWithDefaults(text, TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
            }
            resumenLayout.addView(text);
        }
        try {
            total.setText(String.format("%s %s €", getResources().getString(R.string.total), String.format(Locale.getDefault(), "%.2f", order.getTotal())));
        } catch (Exception e) {
            total.setText(e.getLocalizedMessage());
        }
    }

    public void doReprint(View view) {
        if (lastOrder != null) {
            MainActivity.print(lastOrder, context, sharedPrefManager.getSPCopies());
        }else{
            Toast.makeText(context,getResources().getString(R.string.last_order_empty), Toast.LENGTH_SHORT).show();
        }
    }

    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                typesLayout.removeAllViews();
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                typesAvaliable = catalog.getTypes();
                if (catalog.sincronizarStock(order)) {
                    Toast.makeText(context, getResources().getString(R.string.removed_sync), Toast.LENGTH_SHORT).show();
                }
                cargarTypes();
                adaptador = new ShopAdaptador(catalog.TypeCatalog("menu"), ShopActivity.this, ShopActivity.this);
                recyclerView.setAdapter(adaptador);
            } else {
                try {
                    Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Node>> call, Throwable t) {
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<ArrayList<Tipo>> Typescallback = new Callback<ArrayList<Tipo>>() {
        @Override
        public void onResponse(Call<ArrayList<Tipo>> call, Response<ArrayList<Tipo>> response) {
            if (response.isSuccessful()) {
                mApiService.getAllNodes(sharedPrefManager.getSPStore(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
                tipos.clear();
                tipos = response.body();
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Tipo>> call, Throwable t) {
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };


}