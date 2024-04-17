package thesis.agriproducts.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.reactivex.annotations.NonNull;
import thesis.agriproducts.R;
import thesis.agriproducts.model.entities.Message;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder> {

    private int userId;
    private Context mCtx;
    private List<Message> messageList;
    private int SELF = 786;

    public MessagesAdapter(Context mCtx, List<Message> messageList, int userId) {
        this.userId = userId;
        this.mCtx = mCtx;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == SELF) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_receiver, parent, false);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sender, parent, false);
        }

        return new MessagesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.txtViewMessage.setText(message.getContent());
        holder.txtViewTime.setText(message.getTime());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getUserId() == userId) {
            return SELF;
        }
        return position;
    }

    class MessagesViewHolder extends RecyclerView.ViewHolder{

        TextView txtViewMessage, txtViewTime;

        private MessagesViewHolder(View itemView) {
            super(itemView);
            txtViewMessage = itemView.findViewById(R.id.txtViewMessage);
            txtViewTime = itemView.findViewById(R.id.txtViewTime);
        }
    }
}
