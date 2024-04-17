package thesis.agriproducts.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Iterator;
import java.util.List;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Product;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.model.entities.User;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.ProductAdapter;
import thesis.agriproducts.view.adapter.UserAdapter;

public class AdminFragment extends Fragment {

    //region Attributes
    String TAG = "Admin Fragment";
    String CURRENT_TAG = "";
    View mView;
    TextView mTitle, mErrorView;
    RecyclerView mRecyclerView;
    ProgressBar mProgress;
    SwipeRefreshLayout mSwipeRefreshLayout;

    ProductAdapter mProductAdapter;
    UserAdapter mUserAdapter;
    List<Product> productList;
    List<User> userList;
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_admin, container, false);
        mTitle = mView.findViewById(R.id.txtTitleAdmin);
        mProgress = mView.findViewById(R.id.progAdmin);
        mErrorView = mView.findViewById(R.id.txtAdminError);
        mSwipeRefreshLayout = mView.findViewById(R.id.swipeViewAdmin);
        mRecyclerView = mView.findViewById(R.id.recyclerViewAdmin);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                show();
            }
        });
        show();
        return mView;
    }

    public void setTag(String CURRENT_TAG) {
        this.CURRENT_TAG = CURRENT_TAG;
    }

    public void show() {
        switch (CURRENT_TAG) {
            case Tags.USERS_FRAGMENT:
                mTitle.setText(Tags.USERS_FRAGMENT);
                fetchUsers();
                break;
            case Tags.PRODUCTS_FRAGMENT:
                mTitle.setText(Tags.PRODUCTS_FRAGMENT);
                fetchProducts();
                break;
        }
    }

    private void fetchProducts() {
        clearViews();
        Utils.showProgress(true, mProgress, mRecyclerView);
        Api.getInstance().getServices().getProducts().enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
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

    private void fetchUsers() {
        clearViews();
        Utils.showProgress(true, mProgress, mRecyclerView);
        Api.getInstance().getServices().getUsers().enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    userList = response.body().getUsers();
                    showUsers();
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

    //region Methods
    private void showProducts() {
        CenterRepository.getCenterRepository().setListOfProducts(productList);
        mProductAdapter = new ProductAdapter(getActivity(), productList);
        mProductAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Utils.setPosition(position);
                Utils.switchContent(getActivity(), R.id.adminContainer, Tags.PRODUCT_DETAILS_FRAGMENT);
            }
        });
        mRecyclerView.setAdapter(mProductAdapter);
    }

    private void showUsers() {
        for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
            User user = iterator.next();
            if (user.getAccountId() == Tags.ADMIN) iterator.remove();
        }
        CenterRepository.getCenterRepository().setListOfUsers(userList);
        mUserAdapter = new UserAdapter(getActivity(), userList);
        mUserAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Utils.setPosition(position);
                Utils.switchContent(getActivity(), R.id.adminContainer, Tags.USER_DETAILS_FRAGMENT);
            }
        });
        mRecyclerView.setAdapter(mUserAdapter);
    }

    private void clearViews(){
        mRecyclerView.setAdapter(null);
        mErrorView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }
    //endregion
}
