package net.benoodle.empleado;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.ui.ScanningActivity;

import java.util.Objects;

public class PreferenciasFragment extends PreferenceFragmentCompat {
    private PreferenceScreen direccion;
    private Preference eliminar, asignar;
    private PreferenceCategory category;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferencias, rootKey);
        Preference pref1 = findPreference("version");
        try {
            PackageInfo pInfo = requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0);
            String version = pInfo.versionName;
            assert pref1 != null;
            pref1.setSummary(version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        category = findPreference("printer");
        direccion = getPreferenceManager().createPreferenceScreen(getContext());
        eliminar = new Preference(getContext());
        asignar = new Preference(getContext());
        asignar.setTitle("Asignar");
        asignar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED))  {
                            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, 101);
                        }else{
                            startActivityForResult(new Intent(getContext(), ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                        }
                    }else{
                        startActivityForResult(new Intent(getContext(), ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });

        if (Printooth.INSTANCE.hasPairedPrinter()) {
            initPrinter();
        }
        category.addPreference(asignar);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(getContext(), ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
            } else {
                Toast.makeText(getContext(), "Se necesita el permiso bluetooth para buscar impresoras", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(getContext(), "Impresora asignada", Toast.LENGTH_LONG).show();
            initPrinter();
        } else {
            Toast.makeText(getContext(), "No se encontraron impresoras", Toast.LENGTH_LONG).show();
        }
    }

    private void initPrinter() {
        direccion.setTitle(Printooth.INSTANCE.getPairedPrinter().getName() + " " + Printooth.INSTANCE.getPairedPrinter().getAddress());
        eliminar.setTitle("Eliminar");
        eliminar.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Printooth.INSTANCE.removeCurrentPrinter();
                category.removePreference(direccion);
                category.removePreference(eliminar);
                return true;
            }
        });
        category.addPreference(direccion);
        category.addPreference(eliminar);
    }
}