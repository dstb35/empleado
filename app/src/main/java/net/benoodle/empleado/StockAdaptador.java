package net.benoodle.empleado;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import androidx.recyclerview.widget.RecyclerView;

import net.benoodle.empleado.model.Catalog;
import net.benoodle.empleado.model.Node;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public class StockAdaptador extends RecyclerView.Adapter<StockAdaptador.ViewHolder> {
    private Context context;
    private StockListener stockListener;
    private Catalog stock;


    public StockAdaptador(Catalog stock, Context context, StockListener asignarListener) {
        this.context = context;
        this.stockListener = asignarListener;
        this.stock = stock;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock, parent, false);
        return new ViewHolder(v, stockListener);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Switch swstock;
        private EditText stockText;
        private StockListener stockListener;

        public ViewHolder(View itemView, StockListener stockListener) {
            super(itemView);
            this.stockListener = stockListener;
            this.swstock = itemView.findViewById(R.id.swstock);
            this.stockText = itemView.findViewById(R.id.stock);
        }
    }

    public void onBindViewHolder(ViewHolder holder, final int i) {
        final Node node = stock.getNodeByPos(i);
        holder.swstock.setText(node.getTitle());
        if (node.getStatus() == 1){
            holder.swstock.setChecked(TRUE);
        }else{
            holder.swstock.setChecked(FALSE);
        }
        holder.swstock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    stockListener.switchStock(node.getProductID(), 1);
                }else{
                    stockListener.switchStock(node.getProductID(), 0);
                }
            }
        });
        holder.stockText.setText(node.getStock());
        holder.stockText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int after) {
                stockListener.setStock(node.getProductID(), holder.stockText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public int getItemCount() {
        return stock.getSize();
    }

    public interface StockListener {
        void switchStock(String productID, Integer status);
        void setStock(String productID, String quantity);
    }
}