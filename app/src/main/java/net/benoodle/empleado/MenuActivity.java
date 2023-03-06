package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import net.benoodle.empleado.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MenuActivity extends AppCompatActivity {

    private ArrayList<String> selecciones = new ArrayList<>();
    private Node node;
    private int numRepeticiones;
    private String[] titulos;
    private ArrayList<String> productos;
    private List<String> extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_menu);
        String id = getIntent().getStringExtra("id");
        try {
            node = catalog.getNodeById(id);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
        if (!catalog.isStock(node.getProductID(), 1)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
            finish();
        }
        PedirSelecciones();
    }

    public void PedirSelecciones() {
        //productos[] son las opciones asociadas al menú.
        productos = node.getProductos();
        if (productos != null) {
            numRepeticiones = productos.size();
        }

        //extras son los productos asociados al menú, postres del menú be noodle
        //Array.asList devuelve un array inmodificable, hay que usar un wrapper
        //extras =  Arrays.asList(node.getExtras().split(",", -2));
        extras = node.getExtras();

        for (String producto : productos) {
            final ArrayList<Node> opciones;
            //Menú desayuno
            if (node.getProductID().compareTo(MainActivity.DESAYUNO)  == 0){
                opciones = catalog.getCafes();
            }else{
                opciones = catalog.OpcionesMenu(producto);
            }
            if (opciones.isEmpty()) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock) + producto, Toast.LENGTH_SHORT).show();
                finish();
            }
            titulos = new String[opciones.size()];
            for (int i = 0; i < opciones.size(); i++) {
                titulos[i] = opciones.get(i).getTitle();
            }
            //AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyCustomVerticalScrollBar));
            builder.setTitle(getResources().getString(R.string.choose) + " " + producto);
            builder.setCancelable(false);
            builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int position) {
                    //La posición de productos[] debería coincidir con la posición de opciones.
                    selecciones.add(opciones.get(position).getProductID());
                    dialog.dismiss();
                    if (numRepeticiones == 1) {
                        //En la última repetición se pide los extras
                        if ((extras != null) && (!extras.isEmpty())) {
                            pedirExtras();
                        } else {
                            //No hay comprobación de stock porque al menos una unidad habrá o se hubiese despublicado el producto.
                            añadirMenu();
                        }
                        dialog.dismiss();
                    } else {
                        numRepeticiones--;
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getListView().setScrollbarFadingEnabled(false);
        }
    }

    public void pedirExtras() {
        try {
            /*Quitar espacios en blanco y retornos de carro de las cadenas y poner a minúsculas
             * Mirar stock en catalog.isStock()
             */
            ArrayList<String> found = new ArrayList<>();
            for (int i = 0; i < extras.size(); i++) {
                String id = extras.get(i).replaceAll("\\s", "");
                extras.set(i, id);
                if (!catalog.isStock(id, 1)) {
                    found.add(id);
                }
            }
            extras.removeAll(found);
            if (!extras.isEmpty()) {
                final String[] extrasTitulos = new String[extras.size()];
                for (int i = 0; i < extras.size(); i++) {
                    try {
                        extrasTitulos[i] = catalog.getNodeById(extras.get(i)).getTitle();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (!MenuActivity.this.isFinishing()) {
                    //AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyCustomVerticalScrollBar));
                    builder.setTitle(getResources().getString(R.string.choose));
                    builder.setCancelable(false);
                    builder.setSingleChoiceItems(extrasTitulos, -1, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int position) {
                            dialog.dismiss();
                            selecciones.add(extras.get(position));
                            if (node.getProductID().compareTo(MainActivity.MENU_BE_NOODLE_KAKIGORI) == 0) {
                                pedirKakigori();
                            } else if ((node.getProductID().compareTo(MainActivity.MENU_BE_NOODLE) == 0) && (catalog.existsMenuKakigori()) && (catalog.getKakigoris().size() > 0)) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(new ContextThemeWrapper(MenuActivity.this, R.style.MyCustomVerticalScrollBar));
                                builder1.setTitle(getResources().getString(R.string.ask_kakigori));
                                builder1.setCancelable(false);
                                builder1.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        try {
                                            node = catalog.getNodeById(MainActivity.MENU_BE_NOODLE_KAKIGORI);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        pedirKakigori();
                                    }
                                });
                                builder1.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog1, int which) {
                                        añadirMenu();
                                        dialog1.dismiss();
                                    }
                                });
                                AlertDialog dialog1 = builder1.create();
                                dialog1.show();
                            } else {
                                añadirMenu();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    dialog.getListView().setScrollbarFadingEnabled(false);
                }
            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock_dessert), Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void pedirKakigori() {
        /*Pattern pattern = Pattern.compile("^*kakigori.*");
        final ArrayList<Node> kakigoris = new ArrayList<>();
        for (Node node : catalog.getCatalog()) {
            if (pattern.matcher(node.getTitle().toLowerCase()).matches()) {
                kakigoris.add(node);
            }
        }*/
        if (catalog.getKakigoris().size() < 1) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_stock_kakigori), Toast.LENGTH_SHORT).show();
            finish();
        }
        titulos = catalog.getKakigorisTitulos();
        /*for (int i = 0; i < kakigoris.size(); i++) {
            titulos[i] = kakigoris.get(i).getTitle();
        }*/
        //AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.MyCustomVerticalScrollBar));
        builder.setTitle(getResources().getString(R.string.choose) + " " + "kakigori");
        builder.setCancelable(false);
        builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                //La posición de productos[] debería coincidir con la posición de opciones.
                selecciones.add(catalog.getKakigoris().get(position).getProductID());
                añadirMenu();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getListView().setScrollbarFadingEnabled(false);
    }

    public void añadirMenu() {
        try {
            order.addMenuItem(node.getProductID(), selecciones, 1);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_sell), Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.menu_added), Toast.LENGTH_SHORT).show();
        //setResult(0);
        finish();
    }
}