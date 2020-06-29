package net.benoodle.empleado;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import net.benoodle.empleado.model.LoginData;
import net.benoodle.empleado.retrofit.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends OptionsMenuActivity{

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView, mStore;
    private TextView tURL;
    private View mProgressView;
    private String URL;
    private Button mEmailSignInButton;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private Toolbar toolbar;
    private String email, password, store_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        sharedPrefManager = new SharedPrefManager(this);
        /*if (sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }*/
        tURL = findViewById(R.id.tURL);
        mUsernameView = findViewById(R.id.username);
        mPasswordView = findViewById(R.id.password);
        mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mStore = findViewById(R.id.store);
    }

    protected void onStart(){
        super.onStart();
        PreferenceManager.setDefaultValues(this, R.xml.preferencias, false);
        URL = sharedPrefManager.getURL();
        mStore.setText(sharedPrefManager.getSPStore());
        tURL.setText("URL: "+URL);
        mUsernameView.setText(sharedPrefManager.getSPEmail());
        if (!URL.isEmpty()) {
            try {
                mApiService = UtilsApi.getAPIService(URL);
                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        email = mUsernameView.getText().toString().trim();
                        password = mPasswordView.getText().toString().trim();
                        store_id = mStore.getText().toString().trim();
                        if (email.isEmpty() || password.isEmpty()){
                            Toast.makeText(getApplicationContext(), "Los campos email y password no pueden estar vacíos", Toast.LENGTH_SHORT).show();
                        }else if(store_id.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "El campo tienda no puede estar vacío", Toast.LENGTH_SHORT).show();
                        }else{
                            attemptLogin();
                        }
                    }
                });
                mProgressView = findViewById(R.id.login_progress);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void attemptLogin() {
        mApiService.loginRequest(new LoginData(email, password))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            mProgressView.setVisibility(View.GONE);
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (!jsonRESULTS.getString("csrf_token").isEmpty()) {
                                    String[] Cookies = response.headers().get("Set-Cookie").split(";", 4);
                                    String csrf_token = jsonRESULTS.getString("csrf_token");
                                    String logout_token = jsonRESULTS.getString("logout_token");
                                    String user_id = jsonRESULTS.getJSONObject("current_user").getString("uid");
                                    String name = jsonRESULTS.getJSONObject("current_user").getString("name");
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_NAME, name);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_EMAIL, email);
                                    sharedPrefManager.saveSPString(SharedPrefManager.STORE, store_id);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_CSRF_TOKEN, csrf_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_LOGOUT_TOKEN, logout_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_USER_ID, user_id);
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_IS_LOGGED_IN, true);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE, Cookies[0]);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE_EXPIRES, Cookies[1]);
                                    //String basic_auth = username + ":" + password;
                                    String basic_auth = name + ":" + password;
                                    byte[] bytes_basic_auth = basic_auth.getBytes();
                                    String encoded_basic_auth = android.util.Base64.encodeToString(bytes_basic_auth, android.util.Base64.DEFAULT);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_BASIC_AUTH, "Basic " + encoded_basic_auth.trim());

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String error_message = jsonRESULTS.getString("error_msg");
                                    Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mProgressView.setVisibility(View.GONE);
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.errorBody().string());
                                String error_message = jsonRESULTS.getString("message");
                                Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("debug", "onFailure: ERROR > " + t.toString());
                        mProgressView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.getOrders:
                return true;
            case R.id.MyOrders:
                return true;
            case R.id.CompleteOrders:
                return true;
            case R.id.Preferencias:
                LanzarPreferencias();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void LanzarPreferencias() {
        Intent intent = new Intent(this, PreferenciasActivity.class);
        this.startActivity(intent);
    }
}