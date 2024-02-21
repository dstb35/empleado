package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Cuppon;
import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.User;
import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.benoodle.empleado.MainActivity.catalog;

public class CupponActivity extends AppCompatActivity {

    private ApiService mApiService;
    private SharedPrefManager sharedPrefManager;
    private Catalog stock;
    private HashMap<String, Object> body = new HashMap<>();
    private Context context;
    private View mProgressView;
    private EditText txtpercentage, txtcuppon, txtuser;
    private Spinner spinner;
    private CheckBox chmultiuser;
    private String user = "", type = "";
    private ArrayList<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getApplicationContext();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_cuppon);
        this.txtpercentage = findViewById(R.id.Txtpercentage);
        this.spinner = findViewById(R.id.spinner);
        for (Node node : catalog.getCatalog()) {
            items.add(node.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinner.setAdapter(adapter);
        this.txtcuppon = findViewById(R.id.cuppon);
        Random r = new Random();
        int random = r.nextInt(10000) + 1;
        txtcuppon.setText(String.valueOf(random));
        this.txtuser = findViewById(R.id.user);
        sharedPrefManager = new SharedPrefManager(this);
        mProgressView = findViewById(R.id.login_progress);
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
        this.chmultiuser = findViewById(R.id.chmultiuser);
        chmultiuser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    //Guardar el user por si se desmarca volver a ponerlo
                    user = txtuser.getText().toString();
                    txtuser.setEnabled(false);
                    txtuser.setText("");
                } else {
                    txtuser.setEnabled(true);
                    txtuser.setText(user);
                }
            }
        });
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.percentage:
                if (checked) {
                    spinner.setVisibility(View.GONE);
                    txtpercentage.setVisibility(View.VISIBLE);
                    type = "percentage";
                }
                break;
            case R.id.product:
                if (checked) {
                    spinner.setVisibility(View.VISIBLE);
                    txtpercentage.setVisibility(View.GONE);
                    type = "product";
                }
                break;
        }
    }

    public void doDone(View v) {
        if (chmultiuser.isChecked()) {
            user = "0";
        } else {
            user = txtuser.getText().toString();
        }
        String txt = txtcuppon.getText().toString();
        try {
            if (user.isEmpty() && (!chmultiuser.isChecked())) {
                Toast.makeText(context, "El nombre de usuario no puede estar vacío o selecciona cualquier usuario", Toast.LENGTH_SHORT).show();
            } else if (txt.isEmpty()) {
                Toast.makeText(context, "El código de cupón no puede estar vacío", Toast.LENGTH_SHORT).show();
            } else if (!sharedPrefManager.getSPEncargado()) {
                Toast.makeText(context, "Solo los encargados pueden modificar el stock.", Toast.LENGTH_SHORT).show();
            } else if (type.isEmpty()) {
                Toast.makeText(context, "Elige un tipo", Toast.LENGTH_SHORT).show();
            } else {
                Cuppon cuppon = new Cuppon(txt, user, type);
                cuppon.setOp("add");
                /*body.put("user", user);
                body.put("cuppon", cuppon);
                body.put("op", "add");*/
                if (type.compareTo("percentage") == 0) {
                    int percentage = Integer.parseInt(txtpercentage.getText().toString());
                    if ((percentage > 100) || (percentage < 1)) {
                        Toast.makeText(context, "El porcentaje de descuento ha de estar comprendido entre 1 y 100", Toast.LENGTH_SHORT).show();
                    } else {
                        cuppon.setPercentage(percentage);
                        //body.put("percentage", percentage);
                        //body.put("type", type);
                    }
                } else if (type.compareTo("product") == 0) {
                    cuppon.setProduct(Integer.valueOf(catalog.getIdByPosition(spinner.getSelectedItemPosition())));
                    /*body.put("product", catalog.getIdByPosition(spinner.getSelectedItemPosition()));
                    body.put("type", type);*/
                }
                mProgressView.setVisibility(View.VISIBLE);
                mApiService.addCuppon(sharedPrefManager.getSPBasicAuth(), sharedPrefManager.getSPCsrfToken(), cuppon).enqueue(Cupponcallback);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(context, "El porcentaje ha de ser numérico.", Toast.LENGTH_SHORT).show();
        }
    }

    public void doCancel(View v) {
        finish();
    }

    Callback<Cuppon> Cupponcallback = new Callback<Cuppon>() {
        @Override
        public void onResponse(Call<Cuppon> call, Response<Cuppon> response) {
            mProgressView.setVisibility(View.GONE);
            try {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Cupón creado correctamente", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    JSONObject jObjError = new JSONObject(response.errorBody().string());
                    Toast.makeText(context, jObjError.getString("message"), Toast.LENGTH_LONG).show();
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
}
