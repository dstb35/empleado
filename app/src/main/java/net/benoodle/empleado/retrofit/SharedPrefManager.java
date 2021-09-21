package net.benoodle.empleado.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.benoodle.empleado.LoginActivity;
import net.benoodle.empleado.MainActivity;
import net.benoodle.empleado.OptionsMenuActivity;
import net.benoodle.empleado.OrderDetailActivity;

public class SharedPrefManager {

    public static final String SP_HUBBING_APP = "preferencias_principal";
    public static final String SP_NAME = "spName";
    public static final String SP_EMAIL = "spEmail";
    public static final String SP_CSRF_TOKEN = "spCsrfToken";
    public static final String SP_LOGOUT_TOKEN = "spLogoutToken";
    public static final String SP_USER_ID = "spUserId";
    public static final String SP_BASIC_AUTH = "spBasicAuth";
    public static final String SP_IS_LOGGED_IN = "spIsLoggedLogin";
    public static final String COOKIE = "spCookie";
    public static final String COOKIE_EXPIRES = "spCookieExpires";
    public static final String STORE = "store";
    public static final String MODUS = "modus";
    //public static final String AUTOASSIGN = "autoassign";
    public static final String ENCARGADO = "encargado";
    public static final String COPIES = "copies";
    public static final String AUTOPRINT = "autoprint";
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    public SharedPrefManager(Context context) {
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        spEditor = sp.edit();
    }

    public void saveSPString(String keySP, String value) {
        spEditor.putString(keySP, value);
        spEditor.commit();
    }

    public void saveSPInt(String keySP, int value) {
        spEditor.putInt(keySP, value);
        spEditor.commit();
    }

    public void saveSPBoolean(String keySP, boolean value) {
        spEditor.putBoolean(keySP, value);
        spEditor.commit();
    }

    public String getSPName() {
        return sp.getString(SP_NAME, "");
    }

    public String getSPEmail() {
        return sp.getString(SP_EMAIL, "");
    }

    public String getURL() {
        return sp.getString("URL", "https://benoodle.net");
    }

    public void logout() {
        spEditor.clear();
        spEditor.apply();
    }

    public Boolean getSPIsLoggedIn() {
        return sp.getBoolean(SP_IS_LOGGED_IN, false);
    }
    public String getSPCsrfToken() {
        return sp.getString(SP_CSRF_TOKEN, "");
    }
    public String getSPCsrfLogoutToken() {
        return sp.getString(SP_LOGOUT_TOKEN, "");
    }
    public String getSPUserId() {
        return sp.getString(SP_USER_ID, "");
    }
    public String getSPBasicAuth() {
        return sp.getString(SP_BASIC_AUTH, "");
    }
    public String getSPCookie() {
        return sp.getString(COOKIE, "");
    }
    public String getSPStore() {
        return sp.getString(STORE, "");
    }
    public String getSPCookieExpires() { return sp.getString(COOKIE_EXPIRES, ""); }
    public Boolean getSPModus() { return sp.getBoolean(MODUS, false); }
    //public Boolean getSPAutoassign() { return sp.getBoolean(AUTOASSIGN, true); }
    public Boolean getSPAutoprint() { return sp.getBoolean(AUTOPRINT, true); }
    public Boolean getSPEncargado() { return sp.getBoolean(ENCARGADO, false); }
    public int getSPCopies() {return Integer.parseInt(sp.getString(COPIES, "1")); }
}
