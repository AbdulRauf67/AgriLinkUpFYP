package thesis.agriproducts.view.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.ProductAdapter;

public class OrdersFragment extends Fragment {

    //region Attributes
    String TAG = "Orders Fragment";

    View mView;
    TabLayout mTabLayout;
    TextView mError;

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mRecyclerView;
    ProgressBar mProgress;
    ProductAdapter productAdapter;

    boolean buying = true;
    //endregion

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_orders, container, false);

        mTabLayout = mView.findViewById(R.id.tabOrders);
        mTabLayout.addTab(mTabLayout.newTab().setText("Buying"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Selling"));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mProgress = mView.findViewById(R.id.progressView);
        mError = mView.findViewById(R.id.txtItemError);

        mSwipeRefreshLayout = mView.findViewById(R.id.swipeView);
        mRecyclerView = mView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchOrders(buying);
            }
        });

        mTabLayout.getTabAt(0).select();
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                buying = (tab.getPosition() == 0);
                fetchOrders(buying);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fetchOrders(buying);

        return mView;

    }

    private void clearView() {
        mRecyclerView.setAdapter(null);
        mError.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchOrders(boolean buying) {
        Log.d(TAG, "fetchOrders: " + buying);
        clearView();
        Utils.showProgress(true, mProgress, mRecyclerView);
        int userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        Api.getInstance().getServices().getOrders(userId, buying).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if(!response.isSuccessful())
                        throw new Exception(response.errorBody().toString());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    showOrders(response.body());
                } catch (Exception ex) {
                    Utils.handleError(ex.getMessage(), mError, mProgress, mRecyclerView);
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOrders(final Result result) {
        List<Order> orders = result.getOrders();
        final List<Product> productList = new ArrayList<>();
        Log.d(TAG, "showOrders: " + new Gson().toJson(orders));

        CenterRepository.getCenterRepository().setListOfOrders(orders);

        for(Order order : orders) {
            productList.add(order.getProduct());
        }
        productAdapter = new ProductAdapter(getActivity(), productList);
        productAdapter.setOnItemClickListener(new ProductAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Utils.setPosition(position);
                Utils.setBuying(buying);
                Utils.switchContent(getActivity(), R.id.fragContainer, Tags.ORDER_DETAILS_FRAGMENT);
            }
        });

        mRecyclerView.setAdapter(productAdapter);
    }
}
