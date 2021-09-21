package net.benoodle.empleado;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;

import java.util.ArrayList;
import java.util.Objects;

import static net.benoodle.empleado.MainActivity.boton;
import static net.benoodle.empleado.MainActivity.catalog;

public class OrderItemAdaptador extends RecyclerView.Adapter<OrderItemAdaptador.ViewHolder> {
    private ArrayList<OrderItem> orderItems;
    private boolean impresion;

    public OrderItemAdaptador(ArrayList<OrderItem> orderItems, String modus) {
        this.orderItems = orderItems;
        if (modus.compareTo("imprimir") == 0){
            this.impresion = true;
        }else{
            this.impresion = false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderitem, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView product;
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            this.product = itemView.findViewById(R.id.product);
            this.checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, final int i) {
        final OrderItem orderitem = orderItems.get(i);
        String string = "";
        try {
            string = string.concat(i+1 + ". " + catalog.getNodeById(orderitem.getProductID()).getTitle()+".\t");
        } catch (Exception e) {
            string = string.concat("Producto :" + e.getLocalizedMessage()+".\t");
        }
        string = string.concat(" Unidades: " + orderitem.getQuantity() + System.getProperty("line.separator"));

        if (orderitem.getSelecciones() != null) {
            string = string.concat("Seleccion menú :");
            ArrayList<String> selecciones = orderitem.getSelecciones();
            for (String seleccion : selecciones) {
                try {
                    string = string.concat(catalog.getNodeById(seleccion).getTitle() + ". ");
                } catch (Exception e) {
                    string = string.concat(e.getLocalizedMessage() + " ");
                }
            }
            string = string.concat(Objects.requireNonNull(System.getProperty("line.separator")));
        }
        string = string.concat(Objects.requireNonNull(System.getProperty("line.separator")));
        holder.product.setText(string);
        if (impresion){
            holder.checkBox.setVisibility(View.GONE);
        }else {
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                    if (isChecked) {
                        holder.product.setPaintFlags(holder.product.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        holder.product.setPaintFlags(holder.product.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            });
        }
    }

    public int getItemCount() {
        return orderItems.size();
    }

    public void updateOrderItems(ArrayList<OrderItem> items) {
        orderItems.clear();
        orderItems.addAll(items);
        this.notifyDataSetChanged();
    }
}