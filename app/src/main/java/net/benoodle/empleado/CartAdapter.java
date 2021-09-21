package net.benoodle.empleado;

import static net.benoodle.empleado.MainActivity.MENU;
import static net.benoodle.empleado.MainActivity.catalog;
import static net.benoodle.empleado.ShopActivity.order;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import net.benoodle.empleado.model.Node;
import net.benoodle.empleado.model.OrderItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private Context context;
    private EliminarListener eliminarListener;

    public CartAdapter(Context context, EliminarListener eliminarListener) {
        this.context = context;
        this.eliminarListener = eliminarListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_node, parent, false);
        return new ViewHolder(v, eliminarListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView price, title, quantity, selecciones;
        public Button btEliminar, btMas, btMenos;
        EliminarListener eliminarListener;

        public ViewHolder(View itemView, EliminarListener eliminarListener) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.selecciones = itemView.findViewById(R.id.selecciones);
            this.price = itemView.findViewById(R.id.price);
            this.image = itemView.findViewById(R.id.image);
            this.quantity = itemView.findViewById(R.id.quantity);
            this.eliminarListener = eliminarListener;
            this.btEliminar = itemView.findViewById(R.id.btEliminar);
            this.btMas = itemView.findViewById(R.id.Btmas);
            this.btMenos = itemView.findViewById(R.id.Btmenos);
        }
    }

    public void onBindViewHolder(final ViewHolder holder, final int i) {
        OrderItem orderItem = order.getOrderItems().get(i);
        try {
            final Node node = catalog.getNodeById(orderItem.getProductID());
            Picasso.with(context).load(node.getUrl()).into(holder.image);
            holder.title.setText(node.getTitle());
            if (node.getType().compareTo(MENU) == 0){
                holder.selecciones.setText(order.getSeleccionesByID(orderItem));
                holder.selecciones.setVisibility(View.VISIBLE);
            }else{
                holder.selecciones.setVisibility(View.INVISIBLE);
            }
            holder.price.setText(node.getPrice() + " €");
            holder.quantity.setText(String.valueOf(orderItem.getQuantity()));
            holder.btEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Eliminamos por la posición
                    eliminarListener.Eliminar(i);
                    eliminarListener.ActualizarTotal();
                }
            });
            holder.btMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarListener.Anadir(node.getProductID(),1, node.getType().equals(MENU), i);
                    eliminarListener.ActualizarTotal();
                }
            });
            holder.btMenos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eliminarListener.Anadir(node.getProductID(),-1, node.getType().equals(MENU), i);
                    eliminarListener.ActualizarTotal();
                }
            });

        } catch (Exception e) {
           holder.title.setText(e.getMessage());
        }
    }

    public int getItemCount() {
        return order.getOrderItems().size();
    }

    public interface EliminarListener{
        void Eliminar(int i);
        void Anadir(String productID, int quantity, Boolean menu, int i);
        void ActualizarTotal();
    }
}