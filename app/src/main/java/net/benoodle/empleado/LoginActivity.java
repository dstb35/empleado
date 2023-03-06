package net.benoodle.empleado;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import net.benoodle.empleado.model.LoginData;
import net.benoodle.empleado.retrofit.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends OptionsMenuActivity {

    private AppCompatAutoCompleteTextView mUsernameView;
    private AppCompatEditText mPasswordView;
    private TextView tURL;
    private View mProgressView;
    private String URL;
    private Button mEmailSignInButton;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private Toolbar toolbar;
    private String email, password;
    //private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        sharedPrefManager = new SharedPrefManager(this);
        this.tURL = findViewById(R.id.tURL);
        this.mUsernameView = findViewById(R.id.username);
        this.mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_NEXT && v.getId() == mUsernameView.getId()) {
                    mPasswordView.requestFocus();
                }
                return true;
            }
        });
        this.mPasswordView = findViewById(R.id.password);
        this.mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == EditorInfo.IME_ACTION_DONE && v.getId() == mPasswordView.getId()) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(getApplication().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    takeCredentials();
                }
                return true;
            }
        });
        this.mProgressView = findViewById(R.id.login_progress);
        //this.checkBox = findViewById(R.id.checkBox);
        this.mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        this.mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCredentials();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        PreferenceManager.setDefaultValues(this, R.xml.preferencias, false);
        URL = sharedPrefManager.getURL();
        mApiService = UtilsApi.getAPIService(URL);
        //tURL.setText("URL: "+URL);
        //mUsernameView.setText(sharedPrefManager.getSPEmail());
    }

    public void takeCredentials() {
        email = mUsernameView.getText().toString().trim();
        password = mPasswordView.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Los campos email y password no pueden estar vacíos", Toast.LENGTH_SHORT).show();
        } else if (URL.isEmpty()) {
            Toast.makeText(getApplicationContext(), "La URL no puede estar vacía", Toast.LENGTH_SHORT).show();
        } else {
            try {
                mProgressView.setVisibility(View.VISIBLE);
                attemptLogin();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void attemptLogin() {
        mApiService.loginRequest(new LoginData(email, password, Locale.getDefault().getLanguage()))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        mProgressView.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonRESULTS = new JSONObject(response.body().string());
                                if (!jsonRESULTS.getString("csrf_token").isEmpty()) {
                                    boolean exit = false;
                                    String[] Cookies = response.headers().get("Set-Cookie").split(";", 4);
                                    String csrf_token = jsonRESULTS.getString("csrf_token");
                                    String logout_token = jsonRESULTS.getString("logout_token");
                                    String user_id = jsonRESULTS.getJSONObject("current_user").getString("uid");
                                    String name = jsonRESULTS.getJSONObject("current_user").getString("name");
                                    if (jsonRESULTS.getJSONObject("current_user").has("roles")) {
                                        JSONArray roles = jsonRESULTS.getJSONObject("current_user").getJSONArray("roles");
                                        if (roles.toString().contains("encargado")) {
                                            sharedPrefManager.saveSPBoolean(SharedPrefManager.ENCARGADO, true);
                                        } else if (roles.toString().contains("empleado")) {
                                            sharedPrefManager.saveSPBoolean(SharedPrefManager.ENCARGADO, false);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "No tienes permiso para utilizar esta app", Toast.LENGTH_SHORT).show();
                                            sharedPrefManager.logout();
                                            exit = true;
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "No tienes permiso para utilizar esta app", Toast.LENGTH_SHORT).show();
                                        sharedPrefManager.logout();
                                        exit = true;
                                    }
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_NAME, name);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_EMAIL, email);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_CSRF_TOKEN, csrf_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_LOGOUT_TOKEN, logout_token);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_USER_ID, user_id);
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_IS_LOGGED_IN, true);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE, Cookies[0]);
                                    sharedPrefManager.saveSPString(SharedPrefManager.COOKIE_EXPIRES, Cookies[1]);
                                    //sharedPrefManager.saveSPBoolean(SharedPrefManager.AUTOASSIGN, checkBox.isChecked());
                                    String basic_auth = name + ":" + password;
                                    byte[] bytes_basic_auth = basic_auth.getBytes();
                                    String encoded_basic_auth = android.util.Base64.encodeToString(bytes_basic_auth, android.util.Base64.DEFAULT);
                                    sharedPrefManager.saveSPString(SharedPrefManager.SP_BASIC_AUTH, "Basic " + encoded_basic_auth.trim());
                                    if (exit) {
                                        finish();
                                    } else {
                                        Intent intent = new Intent(LoginActivity.this, StoresActivity.class);
                                        startActivity(intent);
                                    }
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
                            try {
                                /*JSONObject jsonRESULTS = new JSONObject(response.errorBody().string());
                                String error_message = jsonRESULTS.getString("message");
                                Toast.makeText(LoginActivity.this, error_message, Toast.LENGTH_SHORT).show();*/
                                /*TODO revisar otras apps*/
                                Toast.makeText(LoginActivity.this, response.errorBody().string(), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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