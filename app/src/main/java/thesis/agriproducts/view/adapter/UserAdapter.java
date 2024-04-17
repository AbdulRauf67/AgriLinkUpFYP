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
import thesis.agriproducts.model.entities.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{

    private Context mCtx;
    private List<User> userList;
    private static UserAdapter.OnItemClickListener clickListener;

    public UserAdapter(Context mCtx, List<User> userList) {
        this.mCtx = mCtx;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_user, null);
        return new UserAdapter.UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());

        Picasso.get()
                .load(user.getUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .fit()
                .centerCrop()
                .into(holder.imgUser);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setOnItemClickListener(UserAdapter.OnItemClickListener clickListener) {
        UserAdapter.clickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtName, txtEmail;
        ImageView imgUser;
        private UserViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            txtName = itemView.findViewById(R.id.txtItemUserName);
            txtEmail = itemView.findViewById(R.id.txtItemUserEmail);
            imgUser = itemView.findViewById(R.id.imgItemUserThumb);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
