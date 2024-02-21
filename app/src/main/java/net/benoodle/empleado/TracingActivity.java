package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import net.benoodle.empleado.model.Store;
import net.benoodle.empleado.model.User;
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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static net.benoodle.empleado.MainActivity.catalog;

public class TracingActivity extends AppCompatActivity {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private HashMap<String, Object> body = new HashMap<>();
    private Context context;
    private View mProgressView;
    private Button btChoose;
    private Node product;
    private LinearLayout items, itemsDraft;
    private ArrayList<Order> orders = new ArrayList<>();
    private ArrayList<Order> drafts = new ArrayList<>();
    private TextView title, total, totalDraft;
    private String[] titulos;
    private ArrayList<String> tachados = new ArrayList<>();
    //private ArrayList<String> tachadosDraft = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tracing);
        this.sharedPrefManager = new SharedPrefManager(this);
        this.mProgressView = findViewById(R.id.login_progress);
        this.items = findViewById(R.id.items);
        this.title = findViewById(R.id.product);
        this.total = findViewById(R.id.total);
        this.totalDraft = findViewById(R.id.totalDraft);
        this.itemsDraft = findViewById(R.id.itemsDraft);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        try {
            mApiService = UtilsApi.getAPIService();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        askProduct();
    }

    public void askProduct() {
        if (catalog.getCatalog().isEmpty()) {
            Toast.makeText(context, "No hay productos en el catálogo o no se ha cargado correctamente", Toast.LENGTH_SHORT).show();
            finish();
        }
        titulos = new String[catalog.getSize()];
        for (int i = 0; i < catalog.getSize(); i++) {
            titulos[i] = catalog.getNodeByPos(i).getTitle();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(TracingActivity.this);
        builder.setTitle("Elige producto.");
        builder.setCancelable(false);
        builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                product = catalog.getNodeByPos(position);
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.getTracing(product.getProductID(), sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getListView().setScrollbarFadingEnabled(false);
    }

    Callback<ArrayList<ArrayList<Order>>> Orderscallback = new Callback<ArrayList<ArrayList<Order>>>() {
        @Override
        public void onResponse(Call<ArrayList<ArrayList<Order>>> call, Response<ArrayList<ArrayList<Order>>> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                try {
                    int quantity = 0;
                    int draftQuantity = 0;
                    orders = response.body().get(0);
                    drafts = response.body().get(1);
                    items.removeAllViews();
                    itemsDraft.removeAllViews();
                    title.setText(product.getTitle());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(16, 8, 8, 0);
                    //Anchura del layout para dividir el espacio
                    Float width = new Float(items.getWidth());
                    Double itemWidth = width * 0.90;
                    //Double btWidth = width*0.25;
                    for (int j = 0; j < orders.size(); j++) {
                        Order order = orders.get(j);
                        final int pos = j;
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        itemLayout.setGravity(Gravity.CENTER);
                        itemLayout.setMinimumHeight(100);
                        TextView item = new TextView(context);
                        item.setLayoutParams(new LinearLayout.LayoutParams(
                                itemWidth.intValue(), LinearLayout.LayoutParams.MATCH_PARENT));

                        //Se usa total como número total de productos para ese pedido en el endpoint, no como el precio del pedido
                        quantity += Integer.parseInt(order.getTotalasString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.setTextAppearance(R.style.MyCustomTextView);
                            item.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                            item.setAutoSizeTextTypeUniformWithConfiguration(24, 36, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }
                        item.setText("Pedido nº " + order.getOrderId() + " asignado a " + order.getEmpleado() + " . Cantidad: " + order.getTotalasString());
                        itemLayout.addView(item);
                        CheckBox checkBox = new CheckBox(context);
                        if (tachados.contains(order.getOrderId())) {
                            checkBox.setChecked(true);
                            item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                                if (isChecked) {
                                    item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    tachados.add(orders.get(pos).getOrderId());
                                } else {
                                    item.setPaintFlags(item.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    tachados.remove(orders.get(pos).getOrderId());
                                }
                            }
                        });
                        //checkBox.setGravity(Gravity.CENTER);
                        itemLayout.addView(checkBox);
                        items.addView(itemLayout);
                    }
                    for (int i = 0; i < drafts.size(); i++) {
                        Order order = drafts.get(i);
                        final int pos = i;
                        LinearLayout itemLayout = new LinearLayout(context);
                        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                        itemLayout.setLayoutParams(lp);
                        itemLayout.setGravity(Gravity.CENTER);
                        itemLayout.setMinimumHeight(100);
                        TextView item = new TextView(context);
                        item.setLayoutParams(new LinearLayout.LayoutParams(
                                itemWidth.intValue(), LinearLayout.LayoutParams.MATCH_PARENT));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            item.setTextAppearance(R.style.MyCustomTextView);
                            item.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);
                            item.setAutoSizeTextTypeUniformWithConfiguration(24, 36, 2, TypedValue.COMPLEX_UNIT_DIP);
                        }
                        //Se usa total como número total de productos para ese pedido en el endpoint, no como el precio del pedido
                        draftQuantity += Integer.parseInt(order.getTotalasString());
                        item.setText("Pedido nº " + order.getOrderId() + ". Cantidad: " + order.getTotalasString());

                        itemLayout.addView(item);
                        CheckBox checkBox = new CheckBox(context);
                        if (tachados.contains(order.getOrderId())) {
                            checkBox.setChecked(true);
                            item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                                if (isChecked) {
                                    item.setPaintFlags(item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    tachados.add(drafts.get(pos).getOrderId());
                                } else {
                                    item.setPaintFlags(item.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    tachados.remove(drafts.get(pos).getOrderId());
                                }
                            }
                        });
                        //checkBox.setGravity(Gravity.CENTER);
                        itemLayout.addView(checkBox);
                        itemsDraft.addView(itemLayout);
                    }
                    total.setText("Total: " + quantity + " en pedidos asignados.");
                    totalDraft.setText("Total: " + draftQuantity + " en pedidos sin asignar.");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                try {

                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(getApplicationContext(), jObjError.get("message").toString(), Toast.LENGTH_LONG).show();
                    orders.clear();
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

        }

        @Override
        public void onFailure(Call<ArrayList<ArrayList<Order>>> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    public void doRefresh(View v) {
        mProgressView.setVisibility(View.VISIBLE);
        mApiService.getTracing(product.getProductID(), sharedPrefManager.getSPStore(), sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken()).enqueue(Orderscallback);
    }

    public void doCambiar(View v) {
        tachados.clear();
        askProduct();
    }

    public void doFinish(View v) {
        finish();
    }
}
