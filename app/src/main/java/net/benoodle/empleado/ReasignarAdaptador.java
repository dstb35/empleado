package net.benoodle.empleado;

import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.OrderItem;

import java.util.ArrayList;

import static net.benoodle.empleado.MainActivity.catalog;

public class ReasignarAdaptador extends RecyclerView.Adapter<ReasignarAdaptador.ViewHolder> {
    private ArrayList<OrderItem> orderItems;
    private Context context;
    private ModificarListener modificarListener;

    public ReasignarAdaptador(ArrayList<OrderItem> orderItems, Context context, ModificarListener modificarListener) {
        this.orderItems = orderItems;
        this.modificarListener = modificarListener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderitem, parent, false);
        return new ViewHolder(v, modificarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView product;
        private EditText quantity;
        private CheckBox checkBox;
        ModificarListener modificarListener;

        public ViewHolder(View itemView, ModificarListener modificarListener) {
            super(itemView);
            this.modificarListener = modificarListener;
            this.product = itemView.findViewById(R.id.product);
            this.quantity = itemView.findViewById(R.id.quantity);
            this.quantity.setVisibility(View.VISIBLE);
            this.checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public void onBindViewHolder(ViewHolder holder, final int i) {
        final OrderItem orderitem = orderItems.get(i);
        String string = "";
        try {
            string = string.concat(i + 1 + ". Producto :" + catalog.getNodeById(orderitem.getProductID()).getTitle());
        } catch (Exception e) {
            string = string.concat("Producto :" + e.getLocalizedMessage());
        }

        if (orderitem.getSelecciones() != null) {
            string = string.concat(". Seleccion menú :");
            for (String seleccion : orderitem.getSelecciones()) {
                try {
                    string = string.concat(catalog.getNodeById(seleccion).getTitle() + ". ");
                } catch (Exception e) {
                    string = string.concat(e.getLocalizedMessage() + " ");
                }
            }
            string = string.concat(System.getProperty("line.separator"));
        }
        string = string.concat(System.getProperty("line.separator"));
        holder.product.setText(string);
        holder.quantity.setText(String.valueOf(orderitem.getQuantity()));
        holder.quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                int newQuantity;
                //float price = catalog.getPriceById(orderitem.getProductID());
                try {
                    newQuantity = Integer.parseInt(charSequence.toString());
                } catch (NumberFormatException e) {
                    newQuantity = 0;
                }
                /*Float oldSum = price * orderitem.getQuantity();
                Float newSum = price * newQuantity;

                modificarListener.ActualizarTotal(i, newQuantity, oldSum - newSum, false); */
                orderitem.setQuantity(newQuantity);
                modificarListener.ModificarOrderItem(i, newQuantity);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton group, boolean isChecked) {
                if (isChecked) {
                    holder.product.setPaintFlags(holder.product.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.quantity.setVisibility(View.INVISIBLE);
                    modificarListener.EliminarOrderItem(i);
                } else {
                    holder.product.setPaintFlags(holder.product.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.quantity.setVisibility(View.VISIBLE);
                    modificarListener.AnadirOrderItem(i);
                }
            }
        });
        holder.checkBox.setText("Eliminar");
    }

    public int getItemCount() {
        return orderItems.size();
    }

    public interface ModificarListener {
        void EliminarOrderItem (int pos);
        void AnadirOrderItem (int pos);
        void ModificarOrderItem(int pos, int quantity);
    }
}