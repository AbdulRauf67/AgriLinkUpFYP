package thesis.agriproducts.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.annotations.NonNull;
import thesis.agriproducts.R;
import thesis.agriproducts.model.entities.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder>{

    String TAG = "Product Adapter";
    private Context mCtx;
    private List<Product> productList;
    private static OnItemClickListener clickListener;

    public ProductAdapter(Context mCtx, List<Product> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        String price = "PHP " + product.getPrice();
        holder.txtName.setText(product.getProductName());
        holder.txtLocation.setText(product.getLocation());
        holder.txtSeller.setText(product.getUser().getName());
        holder.txtPrice.setText(price);

        Picasso.get()
                .load(product.getProductUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .fit()
                .centerCrop()
                .into(holder.imgProd);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        ProductAdapter.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtName, txtSeller, txtLocation, txtPrice;
        ImageView imgProd;
        private ProductViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtName = itemView.findViewById(R.id.txtItemProductName);
            txtLocation = itemView.findViewById(R.id.txtItemProductLocation);
            txtSeller = itemView.findViewById(R.id.txtItemProductSeller);
            txtPrice = itemView.findViewById(R.id.txtItemProductPrice);
            imgProd = itemView.findViewById(R.id.imgItemProductThumb);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
