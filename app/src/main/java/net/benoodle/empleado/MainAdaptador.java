package net.benoodle.empleado;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;
import java.util.ArrayList;
import static java.lang.Boolean.FALSE;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.boton;

public class MainAdaptador extends RecyclerView.Adapter<MainAdaptador.ViewHolder> {
    private Context context;
    private AsignarListener asignarListener;
    private ArrayList<Order> orders;

    public MainAdaptador(ArrayList<Order> orders, Context context, AsignarListener asignarListener) {
        this.context = context;
        this.asignarListener = asignarListener;
        this.orders = orders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);
        return new ViewHolder(v, asignarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderID, state, total, empleado, item;
        private Button boton;
        AsignarListener asignarListener;

        public ViewHolder(View itemView, AsignarListener asignarListener) {
            super(itemView);
            //this.view = itemView;
            this.orderID = itemView.findViewById(R.id.orderID);
            this.state = itemView.findViewById(R.id.state);
            this.total = itemView.findViewById(R.id.total);
            this.empleado = itemView.findViewById(R.id.empleado);
            this.asignarListener = asignarListener;
            this.boton = itemView.findViewById(R.id.boton);
            this.item = itemView.findViewById(R.id.item);
        }
    }

    public void onBindViewHolder(ViewHolder holder, final int i) {
        final Order order = orders.get(i);
        holder.orderID.setText("ID del pedido: "+order.getOrderId());
        holder.state.setText("Estado del pedido: "+order.getState());
        holder.total.setText("Importe: " + order.getTotal()+ " €");
        holder.empleado.setText("Empleado: "+order.getEmpleado());
        ArrayList<OrderItem> orderItems = order.getOrderItems();
        StringBuilder items = new StringBuilder();
            for (OrderItem orderitem : orderItems) {
                try {
                    items.append("Producto :" + catalog.getNodeById(orderitem.getId()).getTitle() + " Cantidad: " + orderitem.getQuantity());
                } catch (Exception e) {
                    items.append("Producto :"+e.getLocalizedMessage());
                }
                if (orderitem.getSelecciones() != null) {
                    items.append("-> Seleccion menú :");
                    for (String seleccion : orderitem.getSelecciones()) {
                        try {
                            items.append(catalog.getNodeById(seleccion).getTitle()+" ");
                        } catch (Exception e) {
                            items.append(e.getMessage());
                        }
                    }
                }
                items.append(System.getProperty("line.separator"));
            }
        holder.item.setText(items.toString());
        if (boton.compareTo("Asignar") == 0){
            holder.boton.setText(boton);
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Asignar(i);
                }
            });
        }else if(boton.compareTo("Completar") == 0) {
            holder.boton.setText(boton);
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Completar(i);
                }
            });
        }else if(boton.compareTo("Entregar") == 0) {
            holder.boton.setText(boton);
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Entregar(i);
                }
            });
        }else {
            holder.boton.setVisibility(View.INVISIBLE);
            holder.boton.setHeight(0);
        }
    }

    public int getItemCount() {
        return orders.size();
    }

    public interface AsignarListener {
        void Asignar(int i);
        void Completar(int i);
        void Entregar(int i);
    }
}