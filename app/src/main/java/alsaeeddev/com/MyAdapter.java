package alsaeeddev.com;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private final List<ItemModel> itemList;

   private final Context context;
  private final QuantityChangeListener listener;

    public MyAdapter(Context context, List<ItemModel> data, QuantityChangeListener listener) {
        this.itemList = data;
       this.listener = listener;
       this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bindData(position);

      //  holder.itemView.setOnClickListener(v -> Toast.makeText(context, itemList.get(position).getItemQuantity(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void addItem(String item) {
      /*  mData.add(item);
        notifyItemInserted(mData.size() - 1);*/

     /*   mData.add(0, item); // Insert item at index 0
        notifyItemInserted(0);*/
    }

    public  class MyViewHolder extends RecyclerView.ViewHolder {
        TextView itemName, itemPrice;
        EditText editText;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.tvItemName);
            itemPrice = itemView.findViewById(R.id.tvItemPrice);
            editText = itemView.findViewById(R.id.etQuantity);


        }

        public void bindData(int position) {
            ItemModel item = itemList.get(position);
            itemName.setText(item.getItemName());
           itemPrice.setText(String.valueOf(item.getItemPrice()));
           editText.setText(String.valueOf(item.getItemQuantity()));

           editText.addTextChangedListener(new TextWatcher() {
               @Override
               public void beforeTextChanged(CharSequence s, int start, int count, int after) {

               }

               @Override
               public void onTextChanged(CharSequence s, int start, int before, int count) {
                   if(!TextUtils.isEmpty(s.toString())){
                       int quantity = Integer.parseInt(s.toString());
                       if(quantity != 0) {
                           item.setItemQuantity(quantity);
                           //   if(listener != null && position != RecyclerView.NO_POSITION) {
                           listener.onQuantityChanged(position, quantity);
                       }
                     //  }
                   }
               }

               @Override
               public void afterTextChanged(Editable s) {

               }
           });





        }

   /*     public interface QuantityChangeListener {
            void onQuantityChanged(int position, int quantity);
        }

        public void setListener(QuantityChangeListener listener){
            listener = listener;
        }*/


    }




}

