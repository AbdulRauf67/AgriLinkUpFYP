package thesis.agriproducts.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.Nullable;

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
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.ProductAdapter;

public class MyProductsFragment extends Fragment {

    //region Attributes
    View mView;
    TextView mErrorView, mSupplierName;
    RecyclerView mRecyclerView;
    ProductAdapter mAdapter;
    ProgressBar mProgress;
    SwipeRefreshLayout mSwipeRefreshLayout;
    List<Product> productList;
    int userId, supplierId = 0;
    boolean isSupplier = false;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_my_products, container, false);
        mProgress = mView.findViewById(R.id.progMyProducts);
        mErrorView = mView.findViewById(R.id.txtMyProductsError);
        mSupplierName = mView.findViewById(R.id.txtTitleMyProducts);
        mSwipeRefreshLayout = mView.findViewById(R.id.swipViewMyProducts);
        mRecyclerView = mView.findViewById(R.id.recyclerViewMyProducts);
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
        return mView;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public void setIsSupplier(boolean isSupplier) {
        this.isSupplier = isSupplier;
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

        //Set Supplier name
        if(isSupplier) {
            mSupplierName.setText(productList.get(0).getUser().getName());
            mSupplierName.setVisibility(View.VISIBLE);
        }

        //Show Products
        for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
            Product product = iterator.next();
            if(isSupplier) {
                if(product.getUser().getUserId() != supplierId) iterator.remove();
            }
            else {
                if (product.getUser().getUserId() != userId) iterator.remove();
            }
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
}
