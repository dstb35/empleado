package net.benoodle.empleado;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.benoodle.empleado.model.Store;
import net.benoodle.empleado.retrofit.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoresActivity extends AppCompatActivity implements LocationListener {

    private Context context;
    private SharedPrefManager sharedPrefManager;
    private ApiService mApiService;
    private Button btRefresh;
    private Double latitude, longitude;
    private LocationManager locationManager;
    private String zipcode, country;
    private ArrayList<Store> stores;
    private RadioGroup radioGroup;
    private Button btChoose;
    private View mProgressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stores);
        this.context = getApplicationContext();
        sharedPrefManager = new SharedPrefManager(this);
        mApiService = UtilsApi.getAPIService(sharedPrefManager.getURL());
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!sharedPrefManager.getSPIsLoggedIn()) {
            Intent intent = new Intent(StoresActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        this.radioGroup = findViewById(R.id.stores);
        mProgressView = findViewById(R.id.login_progress);
        btRefresh = findViewById(R.id.refresh);
        btChoose = findViewById(R.id.choose);
        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cargarStores();
            }
        });
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        try {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 100, (LocationListener) this);
            cargarStores();
        } catch (Exception e){
            Toast.makeText(context, "Permiso de ubicación necesario para comprobar la disponibilidad de stands", Toast.LENGTH_LONG).show();
        }

    }

    /* Carga todas las tiendas basadas en el zip code del teléfono */
    public void cargarStores() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
        }
        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (location != null) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Address address = null;

                    if (addresses != null && addresses.size() > 0) {
                        for (int i = 0; i < addresses.size(); i++) {
                            address = addresses.get(i);
                            if (address.getPostalCode() != null) {
                                zipcode = address.getPostalCode();
                                country = address.getCountryCode();
                                mProgressView.setVisibility(View.VISIBLE);
                                mApiService.getStores(sharedPrefManager.getSPBasicAuth(), zipcode, country, sharedPrefManager.getSPCsrfToken()).enqueue(Storescallback);
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "", Toast.LENGTH_LONG).show();
                }
            }
        }else{
            Toast.makeText(context, "No se ha podido acceder a la ubicación, revisa los permisos de la aplicación y activa la ubicación.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        cargarStores();
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        cargarStores();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        cargarStores();
    }

    Callback<ArrayList<Store>> Storescallback = new Callback<ArrayList<Store>>() {
        @Override
        public void onResponse(Call<ArrayList<Store>> call, Response<ArrayList<Store>> response) {
            mProgressView.setVisibility(View.GONE);
            if (response.isSuccessful()) {
                stores = response.body();
                radioGroup.removeAllViews();
                btChoose.setOnClickListener(null);
                for (Store store : stores) {
                    RadioButton rb = new RadioButton(context);
                    rb.setText(store.getName());
                    radioGroup.addView(rb);
                }
                btChoose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int pos = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
                            if (pos >= 0 && pos < stores.size()) {
                                Store store = stores.get(pos);
                                sharedPrefManager.saveSPString(SharedPrefManager.STORE, store.getStore_id());
                                if (store.getModus().equals("0")) {
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.MODUS, false);
                                } else {
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.MODUS, true);
                                }
                                if (store.getVoluntarios().equals("0")) {
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.VOLUNTARIOS, false);
                                } else {
                                    sharedPrefManager.saveSPBoolean(SharedPrefManager.VOLUNTARIOS, true);
                                }
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                            }
                        } catch (Exception e){
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                try {
                    Toast.makeText(context, response.errorBody().string(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        public void onFailure(Call<ArrayList<Store>> call, Throwable t) {
            mProgressView.setVisibility(View.GONE);
            Toast.makeText(context, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    };
}
