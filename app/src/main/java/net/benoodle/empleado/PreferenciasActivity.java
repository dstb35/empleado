package net.benoodle.empleado;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import static net.benoodle.empleado.retrofit.UtilsApi.BASE_URL_API;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.mazenrashed.printooth.Printooth;

import net.benoodle.empleado.retrofit.ApiService;
import net.benoodle.empleado.retrofit.SharedPrefManager;
import net.benoodle.empleado.retrofit.UtilsApi;

public class PreferenciasActivity extends AppCompatActivity {

    protected SharedPrefManager sharedPrefManager;
    protected ApiService mApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(decorView.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preferencias);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_preferencias, new PreferenciasFragment())
                .commit();
        sharedPrefManager = new SharedPrefManager(this);
        mApiService = UtilsApi.getAPIService();

    }

    public void doLogout(View view) {
        mApiService.logoutRequest(sharedPrefManager.getSPCsrfToken(), sharedPrefManager.getSPCookie(), sharedPrefManager.getSPLogoutToken());
        sharedPrefManager.logout();
        startActivity(new Intent(this, LoginActivity.class));
    }
    public void doDone(View v) {
        finish();
    }

}
