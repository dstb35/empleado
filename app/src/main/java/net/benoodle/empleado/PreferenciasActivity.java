package net.benoodle.empleado;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
//import static net.benoodle.empleado.retrofit.UtilsApi.BASE_URL_API;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

public class PreferenciasActivity extends AppCompatActivity  {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.activity_preferencias, new PreferenciasFragment())
                .commit();
    }

    public void doDone(View v){
        finish();
    }

}
