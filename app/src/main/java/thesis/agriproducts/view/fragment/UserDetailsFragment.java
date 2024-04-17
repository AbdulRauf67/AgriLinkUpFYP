package thesis.agriproducts.view.fragment;

import android.app.ProgressDialog;
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
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.model.entities.User;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class UserDetailsFragment extends Fragment {

    //region Attributes
    String TAG = "User Details Fragment";
    int position;
    User user;

    View mView;
    TextView mName, mEmail, mNumber, mAddress, mActivate, mErrorView;
    ImageView mImage;
    ProgressDialog pDialog;
    Button mActivateBtn;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_user_details, container, false);
        mErrorView = mView.findViewById(R.id.txtUserError);
        mName = mView.findViewById(R.id.txtUserDetailName);
        mEmail = mView.findViewById(R.id.txtUserDetailEmail);
        mNumber = mView.findViewById(R.id.txtUserDetailNumber);
        mAddress = mView.findViewById(R.id.txtUserDetailAddress);
        mActivate = mView.findViewById(R.id.txtUserDetailActivate);
        mImage = mView.findViewById(R.id.imgUser);

        mActivateBtn = mView.findViewById(R.id.btnActivateUser);
        Button mDelete = mView.findViewById(R.id.btnDeleteUser);
        mActivateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activateUser();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        user = CenterRepository.getCenterRepository().getListOfUsers().get(position);
        showUser();
        return mView;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private void showUser() {
        mName.setText(user.getName());
        mEmail.setText(String.valueOf(user.getEmail()));
        mNumber.setText(String.valueOf(user.getNumber()));
        mAddress.setText(user.getAddress());
        mActivate.setText(user.getActivated() ? "Activated" : "Inactive");
        Picasso.get()
                .load(user.getUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .fit().centerInside()
                .into(mImage);
        mActivateBtn.setEnabled(!user.getActivated());
    }

    private void activateUser() {
        pDialog = Utils.showProgressDialog(getActivity(), "Activating User...");
        Api.getInstance().getServices().activateUser(user.getUserId()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    mActivate.setText("Activated");
                    mActivateBtn.setEnabled(false);
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, pDialog);
                }
            }
            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Utils.dismissProgressDialog(pDialog);
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteUser() {
        pDialog = Utils.showProgressDialog(getActivity(), "Deleting product...");
        Api.getInstance().getServices().deleteUser(user.getUserId()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Utils.switchContent(getActivity(), R.id.adminContainer, Tags.USERS_FRAGMENT);
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                } catch (Exception ex) {
                    ApiHelper.handleError(ex.getMessage(), mErrorView, pDialog);
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
