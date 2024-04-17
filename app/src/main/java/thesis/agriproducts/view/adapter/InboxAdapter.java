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
import thesis.agriproducts.model.entities.Deal;
import thesis.agriproducts.util.Tags;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    private Context mCtx;
    private List<Deal> dealList;
    private int dealFlag;
    private static OnItemClickListener clickListener;

    public InboxAdapter(Context mCtx, List<Deal> dealList, int dealFlag) {
        this.mCtx = mCtx;
        this.dealList = dealList;
        this.dealFlag = dealFlag;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_inbox, null);
        return new InboxAdapter.InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        Deal deal = dealList.get(position);
        if(dealFlag == Tags.BUYING_FLAG) holder.txtSender.setText(deal.getProduct().getUser().getName());
        if(dealFlag == Tags.SELLING_FLAG) holder.txtSender.setText(deal.getUser().getName());
        holder.txtProduct.setText(deal.getProduct().getProductName());
        Picasso.get()
                .load(deal.getProduct().getProductUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .into(holder.imgInboxThumb);
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        InboxAdapter.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class InboxViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView txtSender, txtProduct;
        ImageView imgInboxThumb;

        private InboxViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtSender = itemView.findViewById(R.id.txtInboxSender);
            txtProduct = itemView.findViewById(R.id.txtInboxProduct);
            imgInboxThumb = itemView.findViewById(R.id.imgInboxThumb);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }

}