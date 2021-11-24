package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.MENU;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.OrderItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private EliminarListener eliminarListener;
    private LinearLayout.LayoutParams lp;

    public CartAdapter(Context context, EliminarListener eliminarListener) {
        this.context = context;
        this.eliminarListener = eliminarListener;
        this.lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.lp.setMargins(8, 8, 8, 8);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_node, parent, false);
        return new ViewHolder(v, eliminarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView price, title, quantity;
        public Button btEliminar, btMas, btMenos, btModificar;
        public LinearLayout seleLayout;
        EliminarListener eliminarListener;
        public ViewHolder(View itemView, EliminarListener eliminarListener) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            //this.selecciones = itemView.findViewById(R.id.selecciones);
            this.price = itemView.findViewById(R.id.price);
            this.image = itemView.findViewById(R.id.image);
            this.quantity = itemView.findViewById(R.id.quantity);
            this.eliminarListener = eliminarListener;
            this.btEliminar = itemView.findViewById(R.id.btEliminar);
            this.btMas = itemView.findViewById(R.id.Btmas);
            this.btMenos = itemView.findViewById(R.id.Btmenos);
            this.btModificar = itemView.findViewById(R.id.btModificar);
            this.seleLayout = itemView.findViewById(R.id.seleccionesLayout);
        }
    }

    public void onBindViewHolder(ViewHolder holder, int i) {
        OrderItem orderItem = order.getOrderItems().get(holder.getBindingAdapterPosition());
        try {
            final Node node = catalog.getNodeById(orderItem.getProductID());
            Picasso.with(context).load(node.getUrl()).into(holder.image);
            holder.title.setText(node.getTitle());
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                holder.seleLayout.setOrientation(LinearLayout.VERTICAL);

            }
            if (node.getType().compareTo(MENU) == 0) {
                holder.seleLayout.removeAllViews();
                holder.seleLayout.setVisibility(View.VISIBLE);
                for (int j = 0; j < orderItem.getSelecciones().size(); j++) {
                    final int pos = j;
                    Button seleButton = new Button(context);
                    String id = orderItem.getSelecciones().get(pos);
                    Node nodeSele = catalog.getNodeById(id);
                    seleButton.setText(nodeSele.getTitle());
                    seleButton.setTextSize(12);
                    seleButton.setLayoutParams(lp);
                    seleButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //eliminarListener.modificar(nodeSele, pos, holder.getBindingAdapterPosition());
                            ArrayList<Node> opciones = new ArrayList<>();
                            //mirar que no sea el menú be Noodle y se esté modificando el kakigori del final del menú
                            if ((node.getProductID().compareTo(MainActivity.MENU_BE_NOODLE_KAKIGORI) == 0) && (pos == orderItem.getSelecciones().size() - 1)) {
                                opciones = catalog.getKakigoris();
                            } else if (nodeSele.getType().compareTo("postres") == 0) {
                                //Si es un postre la selecciones vienen determinadas por el campo extras.
                                List<String> extras = node.getExtras();
                                for (int i = 0; i < extras.size(); i++) {
                                    String id = extras.get(i).replaceAll("\\s", "");
                                    try {
                                        opciones.add(catalog.getNodeById(id));
                                    } catch (Exception e) {

                                    }
                                }
                            } else {
                                opciones = catalog.OpcionesMenu(nodeSele.getType());
                            }
                            try {
                                if (opciones.isEmpty()) {
                                    Toast.makeText(context, context.getResources().getString(R.string.no_stock) + nodeSele.getType(), Toast.LENGTH_SHORT).show();
                                } else {
                                    String[] titulos = new String[opciones.size()];
                                    for (int j = 0; j < opciones.size(); j++) {
                                        titulos[j] = opciones.get(j).getTitle();
                                    }
                                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.MyCustomVerticalScrollBar));
                                    builder.setTitle(context.getResources().getString(R.string.choose) + " " + nodeSele.getType());
                                    builder.setCancelable(false);
                                    ArrayList<Node> finalOpciones = opciones;
                                    builder.setSingleChoiceItems(titulos, -1, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int position) {
                                            //La posición de productos[] debería coincidir con la posición de opciones.
                                            String newSele = finalOpciones.get(position).getProductID();
                                            try {
                                                seleButton.setText(catalog.getNodeById(newSele).getTitle());
                                            } catch (Exception e) {
                                                seleButton.setText(String.format("%s newSele", context.getString(R.string.product_not_id)));
                                            }
                                            orderItem.getSelecciones().set(pos, newSele);
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            Toast.makeText(context, context.getResources().getString(R.string.menu_canceled), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    dialog.getListView().setScrollbarFadingEnabled(false);

                                }
                            } catch (Exception e) {
                                Toast.makeText(context, context.getResources().getString(R.string.product_not_id), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    holder.seleLayout.addView(seleButton);
                }
                //holder.selecciones.setText(order.getSeleccionesByID(orderItem));
                //holder.selecciones.setVisibility(View.VISIBLE);
            } else {
                holder.seleLayout.setVisibility(View.INVISIBLE);
            }
            holder.price.setText(String.format("%s €", node.getPrice()));
            holder.quantity.setText(String.valueOf(orderItem.getQuantity()));
            holder.btEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Eliminamos por la posición
                    eliminarListener.eliminar(holder.getBindingAdapterPosition());
                    eliminarListener.actualizarTotal();
                }
            });
            holder.btMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarListener.anadir(node.getProductID(), 1, node.getType().equals(MENU), holder.getBindingAdapterPosition());
                    eliminarListener.actualizarTotal();
                }
            });
            holder.btMenos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarListener.anadir(node.getProductID(), -1, node.getType().equals(MENU), holder.getBindingAdapterPosition());
                    eliminarListener.actualizarTotal();
                }
            });
            /*if (node.getType().compareTo(MENU) == 0){
                holder.btModificar.setVisibility(View.VISIBLE);
                holder.btModificar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        eliminarListener.modificar(node.getProductID(), holder.getBindingAdapterPosition());
                    }
                });
            }*/

        } catch (Exception e) {
            holder.title.setText(e.getMessage());
        }
    }

    public int getItemCount() {
        return order.getOrderItems().size();
    }

    public interface EliminarListener {
        void eliminar(int i);

        void anadir(String productID, int quantity, Boolean menu, int i);

        void actualizarTotal();

        void modificar(Node nodeSele, int pos, int i);
    }
}