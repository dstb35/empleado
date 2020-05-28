package net.benoodle.empleado;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PreferenciasFragment extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey){
        //super.onCreate(savedInstanceState);
        setPreferencesFromResource(R.xml.preferencias, rootKey);
        //addPreferencesFromResource(R.xml.preferencias);
        /*EditTextPreference url = findPreference("URL");
        url.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if ((Boolean) o) {
                    //SharedPreferences prefs = getSharedPreferences("preferencias_principal", Context.MODE_PRIVATE);
                    //BASE_URL_API = prefs.getString("URL", null);
                } else {

                }
                return false;
            }
        });*/
    }
}