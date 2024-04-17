package thesis.agriproducts.view.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import java.util.Iterator;
import java.util.List;

import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Product;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.ProductAdapter;

// Utils.setIsSupplier(false);
// Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.MY_PRODUCTS_FRAGMENT);
// Utils.switchContent(HomeActivity.this, R.id.fragContainer, Tags.ACCOUNT_FRAGMENT);
public class ProfileFragment extends Fragment {

    //region Attributes
    View mView, mViewMyProducts, mViewAccount;
    TabLayout mTabLayout;
    int userId;
    TextView mErrorView;

    TextView mSupplierName;
    RecyclerView mRecyclerView;
    ProductAdapter mAdapter;
    ProgressBar mProgress;
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<Product> productList;


    View mFormView, mFormEdit;
    TextView mName, mEmail, mNumber, mAddress;
    EditText mEditName, mEditEmail, mEditNumber, mEditAddress;
    Button mEdit, mSave;
    ProgressDialog pDialog;
    RequestBody userIdBody, nameBody, emailBody, numberBody, addressBody;
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_profile, container, false);
        mViewMyProducts = mView.findViewById(R.id.viewMyProducts);
        mViewAccount = mView.findViewById(R.id.viewAccount);

        mTabLayout = mView.findViewById(R.id.tabUser);
        mTabLayout.addTab(mTabLayout.newTab().setText("My Products"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Account"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        mTabLayout.getTabAt(0).select();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 0) {
                    viewMyProducts();
                    mViewMyProducts.setVisibility(View.VISIBLE);
                    mViewAccount.setVisibility(View.GONE);
                }
                else if(tab.getPosition() == 1) {
                    viewAccount();
                    mViewMyProducts.setVisibility(View.GONE);
                    mViewAccount.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        viewMyProducts();
        mViewMyProducts.setVisibility(View.VISIBLE);
        mViewAccount.setVisibility(View.GONE);
        return mView;
    }

    //region My Products
    private void viewMyProducts() {
        mProgress = mViewMyProducts.findViewById(R.id.progMyProducts);
        mErrorView = mViewMyProducts.findViewById(R.id.txtMyProductsError);
        mSupplierName = mViewMyProducts.findViewById(R.id.txtTitleMyProducts);
        mSwipeRefreshLayout = mViewMyProducts.findViewById(R.id.swipViewMyProducts);
        mRecyclerView = mViewMyProducts.findViewById(R.id.recyclerViewMyProducts);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showProducts();
            }
        });
        userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        fetchProducts();
    }

    private void clearViews() {
        mRecyclerView.setAdapter(null);
        mErrorView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchProducts() {
        clearViews();
        Utils.showProgress(true, mProgress, mRecyclerView);
        Api.getInstance().getServices().getProducts().enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    productList = response.body().getProducts();
                    showProducts();
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

    private void showProducts() {
        int userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();


        //Show Products
        for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
            Product product = iterator.next();
            if (product.getUser().getUserId() != userId) iterator.remove();
        }

        CenterRepository.getCenterRepository().setListOfProducts(productList);
        if(productList.size() < 1) {
            mErrorView.setText("No product(s) found");
            mErrorView.setVisibility(View.VISIBLE);
        }
        mAdapter = new ProductAdapter(getActivity(), productList);
        mAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Utils.setPosition(position);
                Utils.switchContent(getActivity(), R.id.fragContainer, Tags.PRODUCT_DETAILS_FRAGMENT);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }
    //endregion

    //region Account
    private void viewAccount() {
        mFormView = mViewAccount.findViewById(R.id.formViewAccount);
        mFormEdit = mViewAccount.findViewById(R.id.formEditAccount);
        mErrorView = mViewAccount.findViewById(R.id.txtEditUserError);

        mName = mViewAccount.findViewById(R.id.txtUserName);
        mEmail = mViewAccount.findViewById(R.id.txtUserEmail);
        mNumber = mViewAccount.findViewById(R.id.txtUserNumber);
        mAddress = mViewAccount.findViewById(R.id.txtUserAddress);

        mEditName = mViewAccount.findViewById(R.id.txtEditUserName);
        mEditEmail = mViewAccount.findViewById(R.id.txtEditUserEmail);
        mEditNumber = mViewAccount.findViewById(R.id.txtEditUserNumber);
        mEditAddress = mViewAccount.findViewById(R.id.txtEditUserAddress);

        mEdit = mViewAccount.findViewById(R.id.btnEditUser);
        mSave = mViewAccount.findViewById(R.id.btnSaveUser);
        Button mChangePassword = mViewAccount.findViewById(R.id.btnChangePassowrd);
        Button mLogout = mViewAccount.findViewById(R.id.btnLogout);


        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditUser();

            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUser();

            }
        });
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePassword();

            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });
        showUser();
    }

    private void showUser() {
        mFormView.setVisibility(View.VISIBLE);
        mFormEdit.setVisibility(View.GONE);
        mEdit.setVisibility(View.VISIBLE);
        mSave.setVisibility(View.GONE);
        userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        mName.setText(SharedPrefManager.getInstance().getUser(getActivity()).getName());
        mEmail.setText(SharedPrefManager.getInstance().getUser(getActivity()).getEmail());
        mNumber.setText(SharedPrefManager.getInstance().getUser(getActivity()).getNumber());
        mAddress.setText(SharedPrefManager.getInstance().getUser(getActivity()).getAddress());
    }

    private void showChangePassword() {
        //Initialize
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final LinearLayout layout = new LinearLayout(getActivity());
        final EditText mOldPassword = new EditText (getActivity());
        final EditText mNewPassword = new EditText (getActivity());

        //Layout
        mOldPassword.setHint("Current Password");
        mNewPassword.setHint("New Password");
        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(mOldPassword);
        layout.addView(mNewPassword);
        alert.setTitle("Change Password");
        alert.setView(layout);

        //Handler
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String oldPassword = mOldPassword.getText().toString();
                String newPassword = mNewPassword.getText().toString();
                changePassword(oldPassword, newPassword);
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { dialog.dismiss();
            }
        });
        alert.show();
    }

    private void changePassword(String oldP, String newP) {
        if (Utils.isEmptyFields(oldP, newP)) {
            Toast.makeText(getActivity(), "Invalid Fields", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(oldP)) {
            Toast.makeText(getActivity(), "Invalid Old Password", Toast.LENGTH_LONG).show();
        } else if (!Utils.isPasswordValid(newP)) {
            Toast.makeText(getActivity(), "Invalid New Password", Toast.LENGTH_LONG).show();
        }

        Api.getInstance().getServices().changePassword(userId, oldP, newP).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    logOut();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@Nullable Call<Result> call, @NonNull Throwable t) {
                Toast.makeText(getActivity(), "Api Failure: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEditUser() {
        mEditName.setText(mName.getText());
        mEditEmail.setText(mEmail.getText());
        mEditNumber.setText(mNumber.getText());
        mEditAddress.setText(mAddress.getText());
        mFormView.setVisibility(View.GONE);
        mFormEdit.setVisibility(View.VISIBLE);
        mEdit.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
    }

    private void saveUser() {
        if(!authenticate()) return;
        parseRequestBody();
        pDialog = Utils.showProgressDialog(getActivity(), "Updating your account...");
        Api.getInstance().getServices().updateUser(userIdBody, nameBody, emailBody, numberBody, addressBody).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    showUser();
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

    private boolean authenticate() {
        mErrorView.setVisibility(View.GONE);
        Utils.hideKeyboard(getActivity());
        if (Utils.isEmptyFields(mEditName.getText().toString(),
                mEditEmail.getText().toString(),
                mEditNumber.getText().toString(),
                mEditAddress.getText().toString())) {
            mErrorView.setText(R.string.error_sign_up);
            mErrorView.setVisibility(View.VISIBLE);
            return false;
        } else if (!Utils.isEmailValid(mEditEmail.getText().toString())) {
            mEditEmail.setError(getString(R.string.error_invalid_email));
            mEditEmail.requestFocus();
            return false;
        }
        return true;
    }

    private void parseRequestBody() {
        userIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        nameBody = RequestBody.create(MediaType.parse("text/plain"), mEditName.getText().toString());
        emailBody = RequestBody.create(MediaType.parse("text/plain"), mEditEmail.getText().toString());
        numberBody = RequestBody.create(MediaType.parse("text/plain"), mEditNumber.getText().toString());
        addressBody = RequestBody.create(MediaType.parse("text/plain"), mEditAddress.getText().toString());
    }

    private void logOut() {
        SharedPrefManager.getInstance().logout(getActivity());
        getActivity().finish();
    }
    //endregion

}
