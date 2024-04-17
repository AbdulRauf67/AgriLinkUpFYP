package thesis.agriproducts.view.fragment;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Order;
import thesis.agriproducts.model.entities.Product;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.model.entities.User;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class OrderDetailsFragment extends Fragment {


    //region Attributes
    String TAG = "OrderDetailsFragment";
    int position;
    Order order;

    View mView, mViewProduct, mViewUser, mViewButtons;
    ImageView mImage, mImageUser;
    TextView mProdName, mSupplierName, mLocation, mPrice, mQuantity, mChange;
    TextView mName, mEmail, mAddress;
    TextView mUser, mStatus;
    Button mProcessing, mDelivered, mCancel;
    ProgressDialog pDialog;

    boolean buying = true;
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_order_details, container, false);
        mViewButtons = mView.findViewById(R.id.viewOrderButtons);

        mViewProduct = mView.findViewById(R.id.itemOrderProduct);
        mImage = mViewProduct.findViewById(R.id.imgItemProductThumb);
        mProdName = mViewProduct.findViewById(R.id.txtItemProductName);
        mSupplierName = mViewProduct.findViewById(R.id.txtItemProductSeller);
        mLocation = mViewProduct.findViewById(R.id.txtItemProductLocation);
        mPrice = mViewProduct.findViewById(R.id.txtItemProductPrice);

        mViewUser = mView.findViewById(R.id.itemOrderUser);
        mImageUser = mViewUser.findViewById(R.id.imgItemUserThumb);
        mName = mViewUser.findViewById(R.id.txtItemUserName);
        mEmail = mViewUser.findViewById(R.id.txtItemUserEmail);
        mAddress = mViewUser.findViewById(R.id.txtItemUserAddress);

        mStatus = mView.findViewById(R.id.txtOrderStatus);
        mUser = mView.findViewById(R.id.txtOrderUser);
        mQuantity = mView.findViewById(R.id.txtOrderQuantity);
        mChange = mView.findViewById(R.id.txtOrderChange);
        mProcessing = mView.findViewById(R.id.btnOrderProcess);
        mDelivered = mView.findViewById(R.id.btnOrderDelivered);
        mCancel = mView.findViewById(R.id.btnOrderCancel);

        mProcessing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrderStatus(Tags.PROCESSING, true);
            }
        });
        mDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOrderStatus(Tags.DELIVERED, false);
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteOrder();
            }
        });

        authenticate();
        showOrderDetails();
        return mView;
    }

    public void setBuying(boolean buying) {
        this.buying = buying;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void authenticate() {
        if(buying) {
            mViewButtons.setVisibility(View.GONE);
            mCancel.setVisibility(View.VISIBLE);
            mUser.setText(R.string.buyer);
        } else {
            mViewButtons.setVisibility(View.VISIBLE);
            mCancel.setVisibility(View.GONE);
            mUser.setText(R.string.supplier);
        }
    }

    private void showOrderDetails() {
        order = CenterRepository.getCenterRepository().getListOfOrders().get(position);
        mStatus.setText(order.getStatus());
        Product product = order.getProduct();
        mProdName.setText(product.getProductName());
        mSupplierName.setText(product.getUser().getName());
        mLocation.setText(product.getUser().getAddress());
        mPrice.setText(String.valueOf(product.getPrice()));
        Picasso.get()
                .load(product.getProductUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .into(mImage);

        User user;
        String account, name, email, address, url;
        if(buying) {
            account = "Seller";
            name = "Name: " + order.getProduct().getUser().getName();
            email = "Email: " + order.getProduct().getUser().getEmail();
            address = "Address: " + order.getProduct().getUser().getAddress();
            url = order.getProduct().getUser().getUrl();

        } else {
            account = "Buyer";
            name = "Name: " + order.getUser().getName();
            email = "Email: " + order.getUser().getEmail();
            address = "Address: " + order.getUser().getAddress();
            url = "Address: " + order.getProduct().getUser().getUrl();
        }

        mUser.setText(account);
        mName.setText(name);
        mEmail.setText(email);
        mAddress.setText(address);
        Picasso.get()
                .load(order.getProduct().getUser().getUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .into(mImageUser);

        mQuantity.setText(String.valueOf(order.getQuantity()));
        if (order.getCash() > 0) {
            mChange.setVisibility(View.VISIBLE);
            mChange.setText(String.valueOf(order.getCash()));
        } else {
            mChange.setVisibility(View.GONE);
        }


        if(order.getStatus().equals(Tags.DELIVERED)) {
            mStatus.setTextColor(Color.parseColor("#00b226"));
            mProcessing.setVisibility(View.GONE);
            mDelivered.setVisibility(View.GONE);
            mCancel.setVisibility(View.VISIBLE);
            mCancel.setText("DELETE");
        } else {
            mStatus.setTextColor(Color.parseColor("#b20c0f"));
        }

    }

    private void setOrderStatus(String status, boolean active) {
        pDialog = Utils.showProgressDialog(getActivity(), "Updating...");
        int orderId = order.getId();
        Api.getInstance().getServices().updateOrderStatus(orderId, status, active).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if(!response.isSuccessful())
                        throw new Exception(response.errorBody().toString());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    Utils.switchContent(getActivity(), R.id.fragContainer, Tags.ORDERS_FRAGMENT);
                } catch (Exception ex) {
                    Utils.dismissProgressDialog(pDialog);
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Utils.dismissProgressDialog(pDialog);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteOrder() {
        int orderId = order.getId();
        pDialog = Utils.showProgressDialog(getActivity(), "Cancelling order...");
        Api.getInstance().getServices().deleteOrder(orderId).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    Utils.switchContent(getActivity(), R.id.fragContainer, Tags.ORDERS_FRAGMENT);
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
}
