package thesis.agriproducts.view.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Deal;
import thesis.agriproducts.model.entities.Message;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.MessagesAdapter;

public class MessagesFragment extends Fragment {

    //region Attributes
    String TAG = "Messages Fragment";
    View mView, mViewProduct;
    TextView mErrorView, mMessage;
    TextView mProductName, mProductSeller, mProductLocation, mProductPrice;
    ImageView mProductImage;
    ProgressBar mProgress;
    ProgressDialog pDialog;

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    MessagesAdapter mMessagesAdapter;

    int position, userId;
    String content;
    Message message;
    Deal deal;
    List<Message> messageList;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        layoutManager = new LinearLayoutManager(getActivity());

        mView = inflater.inflate(R.layout.fragment_chat_room, container, false);

        mViewProduct = mView.findViewById(R.id.itemProduct);
        mProductName = mViewProduct.findViewById(R.id.txtItemProductName);
        mProductSeller = mViewProduct.findViewById(R.id.txtItemProductSeller);
        mProductLocation = mViewProduct.findViewById(R.id.txtItemProductLocation);
        mProductPrice = mViewProduct.findViewById(R.id.txtItemProductPrice);
        mProductImage = mViewProduct.findViewById(R.id.imgItemProductThumb);

        mProgress = mView.findViewById(R.id.progMessages);
        mErrorView = mView.findViewById(R.id.txtMessagesError);
        mMessage = mView.findViewById(R.id.txtMessage);
        mRecyclerView = mView.findViewById(R.id.recyclerViewMessages);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(layoutManager);
        Button mSend = mView.findViewById(R.id.btnSend);
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        deal = CenterRepository.getCenterRepository().getListOfDeals().get(position);
        showProduct();
        fetchMessages();
        return mView;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void clearViews() {
        mRecyclerView.setAdapter(null);
        mErrorView.setVisibility(View.GONE);
    }

    private void showProduct() {
        String price = "PHP " + deal.getProduct().getPrice();
        mProductName.setText(deal.getProduct().getProductName());
        mProductLocation.setText(deal.getProduct().getLocation());
        mProductPrice.setText(price);
        mProductSeller.setText(deal.getProduct().getUser().getName());
        Picasso.get()
                .load(deal.getProduct().getProductUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .fit()
                .centerCrop()
                .into(mProductImage);
    }

    private void fetchMessages() {
        clearViews();
        Utils.showProgress(true, mProgress, mRecyclerView);
        Api.getInstance().getServices().getMessages(deal.getDealId()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    messageList = response.body().getMessages();
                    showMessages();
                    scrollToBottom();
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, mProgress, mRecyclerView);
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                ApiHelper.handleError(t.getMessage(), mErrorView, mProgress, mRecyclerView);
            }
        });
    }

    private void sendMessage() {
        String time = android.text.format.DateFormat.format("kk:mm MM/dd/yyyy", new java.util.Date()).toString();
        mErrorView.setVisibility(View.GONE);
        pDialog = Utils.showProgressDialog(getActivity(), "Sending message...");
        content = mMessage.getText().toString();
        message = new Message(deal.getDealId(), userId, content, time);
        Api.getInstance().getServices().setMessage(deal.getDealId(), userId, content).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (!response.isSuccessful())
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    messageList.add(message);
                    mMessagesAdapter.notifyDataSetChanged();
                    scrollToBottom();
                } catch (Exception ex) {
                    Utils.dismissProgressDialog(pDialog);
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Utils.dismissProgressDialog(pDialog);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMessages() {
        mMessagesAdapter = new MessagesAdapter(getActivity(), messageList, userId);
        mRecyclerView.setAdapter(mMessagesAdapter);
    }

    private void scrollToBottom() {
        if (mMessagesAdapter.getItemCount() > 1)
            mRecyclerView.getLayoutManager().smoothScrollToPosition(mRecyclerView, null, mMessagesAdapter.getItemCount() - 1);
        mMessagesAdapter.notifyDataSetChanged();
    }
}

