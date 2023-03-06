package net.benoodle.empleado;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.order;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.gms.common.util.NumberUtils;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopActivity extends AppCompatActivity {

    public static final int COMPRA_OK = 1;
    public static final int QR_OK = 2;
    public static String MENU = "menu";
    public static Order lastOrder;
    private SharedPrefManager sharedPrefManager;
    private Context context;
    private ApiService mApiService;
    private LinearLayout resumenLayout, productsLayout;
    private TextView total;
    private ArrayList<String> typesAvaliable;
    private Pattern pattern = Pattern.compile("^*kakigori.*");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        this.context = getApplicationContext();
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

        this.resumenLayout = findViewById(R.id.resumen);
        this.total = findViewById(R.id.total);
        this.productsLayout = findViewById(R.id.products);
        if (order == null) {
            order = new Order(sharedPrefManager.getSPStore());
        }

        if (sharedPrefManager.getSPVoluntarios()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
            final EditText input = new EditText(ShopActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT);
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
        mApiService.getAllNodes(sharedPrefManager.getSPStore(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarResumen();
    }

    public void ShowCart(View v) {
        //Si el resultado es 1 es compra exitosa, recargar el catálogo. Así no machaca el stock del carrito en compras a medias.
        startActivityForResult(new Intent(this, CartActivity.class), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(resultCode, resultCode, data);
        if (requestCode == COMPRA_OK && resultCode == RESULT_OK) {
            if (sharedPrefManager.getSPVoluntarios()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
                final EditText input = new EditText(ShopActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT);
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
            /*Cargar todos los tipos de productos por el stock*/ //Requestcode 49374 resultcode -1
            mApiService.getAllNodes(sharedPrefManager.getSPStore(), Locale.getDefault().getLanguage(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Nodecallback);
        } else if (requestCode == QR_OK) {
            if (resultCode == -1) {
                String id = data.getStringExtra("SCAN_RESULT");
                if (id != null) {
                    if (TextUtils.isDigitsOnly(id)) {
                        Intent intent = new Intent(context, TicketActivity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }else{
                        Toast.makeText(context, "Formato incorrecto.", Toast.LENGTH_SHORT).show();
                        pedirCodigoManual();
                    }

                } else {
                    Toast.makeText(context, "Código erróneo", Toast.LENGTH_SHORT).show();
                    pedirCodigoManual();
                }
            } else {
                Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show();
                pedirCodigoManual();
            }
        }
    }

    /*
    Añade al carrito el producto y la cantidad  pasadas.
    Si es un menú pide al usuario las opciones en MenuActivity
    Las opciones vienen de node.productos[] del server.
     */
    //@Override
    public void anadir(Node node, int quantity) {
        if (node.getType().equals(MENU)) {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("id", node.getProductID());
            //Con el result llamaremos a adaptador.notifyDataSetChange para que cambie el stock o no
            startActivityForResult(intent, 1);
        } else if (pattern.matcher(node.getTitle().toLowerCase()).matches()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyCustomVerticalScrollBar));
            builder.setTitle(getResources().getString(R.string.choose) + " " + "kakigori");
            builder.setCancelable(false);
            builder.setSingleChoiceItems(catalog.getKakigorisTitulos(), -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                    try {
                        order.addOrderItem(catalog.getKakigoris().get(position), quantity);
                        Toast.makeText(context, getResources().getString(R.string.product_added), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        actualizarResumen();
                    } catch (Exception e) {
                        Toast.makeText(context, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setScrollbarFadingEnabled(false);
        } else {
            try {
                order.addOrderItem(node, quantity);
                Toast.makeText(context, getResources().getString(R.string.product_added), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
            }
        }
        actualizarResumen();
    }

    public void actualizarResumen() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                MATCH_PARENT,
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
        } else {
            Toast.makeText(context, getResources().getString(R.string.last_order_empty), Toast.LENGTH_SHORT).show();
        }
    }

    public void doQr(View view) {
        //barcodeLauncher.launch(new ScanOptions());
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setRequestCode(2);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.initiateScan();
    }

    public void pedirCodigoManual(){
        AlertDialog.Builder builder = new AlertDialog.Builder(ShopActivity.this);
        builder.setMessage("¿Quieres introducir el pedido manualmente?");
        builder.setCancelable(true);
        builder.setPositiveButton("Sí",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, TicketActivity.class);
                        startActivity(intent);
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

    /*private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() == null) {
                    Toast.makeText(context, "Cancelado", Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(context, MenuActivity.class);
                    intent.putExtra("id", result.getContents());
                }
            });
*/
    Callback<ArrayList<Node>> Nodecallback = new Callback<ArrayList<Node>>() {
        @Override
        public void onResponse(Call<ArrayList<Node>> call, Response<ArrayList<Node>> response) {
            if (response.isSuccessful()) {
                productsLayout.removeAllViews();
                catalog = new Catalog(response.body());
                catalog.CrearTypes();
                if (catalog.sincronizarStock(order)) {
                    Toast.makeText(context, getResources().getString(R.string.removed_sync), Toast.LENGTH_SHORT).show();
                }

                typesAvaliable = catalog.getTypes();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;
                float scaleFactor = metrics.density;
                float widthDp = width / scaleFactor;

                //Diseño pequeño o grande
                float parentWidth;
                int numitems;
                if (widthDp < 600) {
                    parentWidth = productsLayout.getWidth() / 3;
                    numitems = 3;
                } else if (widthDp < 1200) {
                    parentWidth = width / 6;
                    numitems = 4;
                } else {
                    parentWidth = width / 8;
                    numitems = 6;
                }
                LinearLayout.LayoutParams categoriesParams = new LinearLayout.LayoutParams(
                        MATCH_PARENT,
                        WRAP_CONTENT
                );
                LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                        (int) parentWidth,
                        WRAP_CONTENT
                );
                titleParams.setMargins(8, 16, 0, 8);
                for (String name : typesAvaliable) {
                    TextView type = new TextView(context);
                    type.setText(String.format("%s%s", name.substring(0, 1).toUpperCase(), name.substring(1)));
                    type.setPadding(8, 16, 0, 16);
                    productsLayout.addView(type);
                    LinearLayout oneLineLayout = new LinearLayout(context);
                    oneLineLayout.setGravity(Gravity.CENTER);
                    oneLineLayout.setLayoutParams(categoriesParams);
                    productsLayout.addView(oneLineLayout);
                    int i = 0;
                    for (Node node : catalog.TypeCatalog(name)) {
                        if (i % numitems == 0) {
                            oneLineLayout = new LinearLayout(context);
                            oneLineLayout.setLayoutParams(categoriesParams);
                            productsLayout.setGravity(Gravity.CENTER);
                            productsLayout.addView(oneLineLayout);
                        }
                        LinearLayout titleLayout = new LinearLayout(context);
                        titleLayout.setOrientation(LinearLayout.VERTICAL);
                        titleLayout.setLayoutParams(titleParams);
                        titleLayout.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.shape, getTheme()));
                        titleLayout.setGravity(Gravity.CENTER);
                        titleLayout.setElevation(16);
                        ImageView image = new ImageView(context);
                        image.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                        try {
                            Picasso.with(context).load(node.getUrl()).resize(0, 120).into(image);
                        } catch (Exception e) {
                            Picasso.with(context).load(node.getUrl()).into(image);
                        }

                        titleLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                anadir(node, 1);
                            }
                        });
                        titleLayout.addView(image);
                        TextView text = new TextView(context);
                        text.setTextSize(10);
                        text.setGravity(Gravity.CENTER);
                        text.setPadding(8, 0, 8, 8);
                        text.setText(String.format("%s %s €", node.getTitle(), node.getPrice()));
                        titleLayout.addView(text);
                        oneLineLayout.addView(titleLayout);
                        i++;
                    }
                }
            } else {
                try {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
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
}