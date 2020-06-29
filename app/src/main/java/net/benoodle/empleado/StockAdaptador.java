package net.benoodle.empleado;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class StockAdaptador extends RecyclerView.Adapter<StockAdaptador.ViewHolder> {
    private Context context;
    private Catalog stock;
    private Node node;

    public StockAdaptador(Catalog stock, Context context) {
        this.context = context;
        this.stock = stock;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Switch swstock;
        private TextView stockText;
        private Button cambiar;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swstock = itemView.findViewById(R.id.swstock);
            this.stockText = itemView.findViewById(R.id.stock);
            this.cambiar = itemView.findViewById(R.id.cambiar);
        }
    }

    public void onBindViewHolder(ViewHolder holder, int i) {
        node = stock.getNodeByPos(i);
        holder.swstock.setText(node.getTitle());
        holder.swstock.setChecked(node.getStatus());
        holder.swstock.setOnClickListener(new CompoundButton.OnClickListener() {
            public void onClick(View view){
                stock.switchStock(node.getProductID(), holder.swstock.isChecked());
            }
        });
        holder.stockText.setText(node.getStock());
        holder.cambiar.setOnClickListener(new CompoundButton.OnClickListener() {
            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final EditText input = new EditText(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setTitle("Introduce el stock nuevo: ");
                builder.setCancelable(true);
                builder.setPositiveButton("Cambiar",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stock.setStock(node.getProductID(),  input.getText().toString());
                                holder.stockText.setText(input.getText().toString());
                            }
                        });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(context, "Cancelado", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        //No funciona bien, se activa con el scroll
        /*holder.stockText.addTextChangedListener(null);
        holder.stockText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                stock.setStock(node.getProductID(), holder.stockText.getText().toString());
            }
        });*/
    }

    public int getItemCount() {
        return stock.getSize();
    }
}