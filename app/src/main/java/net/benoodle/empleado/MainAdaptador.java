package net.benoodle.empleado;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.Order;
import net.benoodle.empleado.model.OrderItem;

import java.util.ArrayList;

import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.MainActivity.boton;

public class MainAdaptador extends RecyclerView.Adapter<MainAdaptador.ViewHolder> implements Filterable {
    private Context context;
    private AsignarListener asignarListener;
    private ArrayList<Order> orders;
    private ArrayList<Order> ordersFull;

    public MainAdaptador(ArrayList<Order> orders, Context context, AsignarListener asignarListener) {
        this.context = context;
        this.asignarListener = asignarListener;
        this.orders = orders;
        ordersFull = new ArrayList<>(orders);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order, parent, false);
        return new ViewHolder(v, asignarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderID, customer, state, total, empleado, item, cuppons, updated;
        private SwitchCompat cobrado;
        private Button boton, btCompletar, btEntregar;
        AsignarListener asignarListener;

        public ViewHolder(View itemView, AsignarListener asignarListener) {
            super(itemView);
            this.orderID = itemView.findViewById(R.id.orderID);
            this.customer = itemView.findViewById(R.id.customer);
            this.state = itemView.findViewById(R.id.state);
            this.total = itemView.findViewById(R.id.total);
            this.empleado = itemView.findViewById(R.id.empleado);
            this.asignarListener = asignarListener;
            this.boton = itemView.findViewById(R.id.btn);
            this.item = itemView.findViewById(R.id.item);
            this.cobrado = itemView.findViewById(R.id.cobrado);
            this.btCompletar = itemView.findViewById(R.id.btCompletar);
            this.btEntregar = itemView.findViewById(R.id.btEntregar);
            this.cuppons = itemView.findViewById(R.id.cuppons);
            this.updated = itemView.findViewById(R.id.updated);
        }
    }

    public void onBindViewHolder(ViewHolder holder, final int i) {
        final Order order = orders.get(i);
        holder.orderID.setText("Nº del pedido: " + order.getOrderId());
        holder.customer.setText(order.getCustomer());
        holder.state.setText("Estado del pedido: " + order.getState());
        holder.cobrado.setChecked(order.getPagado());
        holder.cuppons.setText(order.getCupponsasString());
        holder.updated.setText("Actualizado " + order.getUpdated());
        ArrayList<OrderItem> orderItems = order.getOrderItems();
        if (boton.compareTo("Asignar") != 0) {
            try {
                holder.total.setText("Importe: " + order.getTotalasString() + " €");
            }catch (Exception e){
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            holder.empleado.setText("Empleado: " + order.getEmpleado());
            StringBuilder items = new StringBuilder();
            for (OrderItem orderitem : orderItems) {
                try {
                    items.append(catalog.getNodeById(orderitem.getProductID()).getTitle() + ". \t Unidades: " + orderitem.getQuantity());
                } catch (Exception e) {
                    items.append("Producto: " + e.getLocalizedMessage());
                }
                if (orderitem.getSelecciones() != null) {
                    items.append("   ->Seleccion menú :");
                    for (String seleccion : orderitem.getSelecciones()) {
                        try {
                            items.append(catalog.getNodeById(seleccion).getTitle() + " ");
                        } catch (Exception e) {
                            items.append(e.getMessage());
                        }
                    }
                }
                items.append(System.getProperty("line.separator"));
            }
            holder.item.setText(items.toString());
        }else{
            holder.item.setVisibility(View.GONE);
            holder.total.setVisibility(View.GONE);
            holder.empleado.setVisibility(View.INVISIBLE);
        }
        holder.boton.setText(boton);
        if (boton.compareTo("Asignar") == 0) {
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Asignar(i);
                }
            });
        } else if (boton.compareTo("Completar") == 0) {
            holder.boton.setText("Ver pedido");
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Ver(i);
                }
            });
            holder.btCompletar.setVisibility(View.VISIBLE);
            holder.btEntregar.setVisibility(View.VISIBLE);
            holder.btCompletar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { asignarListener.Completar(i);}
            });
            holder.btEntregar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Entregar(i);
                }
            });

        } else if (boton.compareTo("Cobrar") == 0) {
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Cobrar(i);
                }
            });
        } else if (boton.compareTo("Entregar") == 0) {
            holder.boton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    asignarListener.Entregar(i);
                }
            });
        } else {
            holder.empleado.setText("Empleado: " + order.getEmpleado() + " Tienda: " + order.getStore());
            holder.boton.setVisibility(View.INVISIBLE);
            holder.boton.setHeight(0);
        }
    }

    public int getItemCount() {
        return orders.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Order> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(ordersFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Order order : ordersFull) {
                    if (order.getOrderId().startsWith(filterPattern)) {
                        filteredList.add(order);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            orders.clear();
            orders.addAll((ArrayList) results.values);
            notifyDataSetChanged();
        }
    };

    public interface AsignarListener {
        void Cobrar(int i);

        void Asignar(int i);

        void Completar(int i);

        void Entregar(int i);

        void Ver(int i);
    }
}