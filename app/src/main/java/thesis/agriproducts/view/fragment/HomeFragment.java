package thesis.agriproducts.view.fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
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

public class HomeFragment extends Fragment {

    //region Attributes
    String TAG = "Home Fragment";

    View mView;
    Spinner mFilter;
    EditText mSearchProduct;
    TextView mErrorView;
    RecyclerView mRecyclerView;
    ProductAdapter mAdapter;
    ProgressBar mProgress;
    SwipeRefreshLayout mSwipeRefreshLayout;

    List<Product> productList;
    String[] items = new String[]{"Filter Products", "Price: Low to High", "Price: High to Low", "Nearest"};
    Location currentLocation;

    FusedLocationProviderClient fusedLocationClient;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_products, container, false);
        mProgress = mView.findViewById(R.id.progProducts);
        mErrorView = mView.findViewById(R.id.txtHomeError);
        mSearchProduct = mView.findViewById(R.id.txtSearchProducts);
        mFilter = mView.findViewById(R.id.txtFilter);
        mSwipeRefreshLayout = mView.findViewById(R.id.swipeView);
        mRecyclerView = mView.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSearchProduct.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == KeyEvent.ACTION_DOWN && keyCode ==KeyEvent.KEYCODE_ENTER)) {
                    searchProducts(mSearchProduct.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 1) sortPrice(true);
                else if(position == 2) sortPrice(false);
                else if(position == 3) sortNearest();
                else{
                    Toast.makeText(getActivity(), "Select Filter", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchProducts();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, items);
        mFilter.setAdapter(adapter);
        fetchProducts();


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        return mView;
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
                    if(response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if(response.body().getError())
                        throw new Exception(response.body().getMessage());
                    productList = response.body().getProducts();
                    CenterRepository.getCenterRepository().setListOfProducts(productList);
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
        for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
            Product product = iterator.next();
            if (product.getUser().getUserId() == userId) iterator.remove();
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

    private void searchProducts(String productName){
        Utils.hideKeyboard(getActivity());
        for (Iterator<Product> iterator = productList.iterator(); iterator.hasNext();) {
            Product product = iterator.next();
            if (!product.getProductName().contains(productName)) iterator.remove();
        }
        CenterRepository.getCenterRepository().setListOfProducts(productList);
        mAdapter.notifyDataSetChanged();
    }

    private void sortPrice(final boolean isCheapest) {
        Log.d(TAG, "sortCheapest: Sort Cheapest");
        if(productList == null) return;
        Collections.sort(productList, new Comparator<Product>(){
            @Override
            public int compare(Product o1, Product o2) {
                if(isCheapest) return Double.valueOf(o1.getPrice()).compareTo(o2.getPrice());
                else return Double.valueOf(o2.getPrice()).compareTo(o1.getPrice());
            }
        });
        mAdapter.notifyDataSetChanged();
    }

    private void sortNearest() {
        Log.d(TAG, "sortCheapest: Sort Nearest");
        if(productList == null) return;
        if (ContextCompat.checkSelfPermission( getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentLocation = location;
                            Log.d(TAG, "onSuccess: " + location.getLatitude());
                            Collections.sort(productList, new Comparator<Product>(){
                                @Override
                                public int compare(Product o1, Product o2) {
                                    Location loc1 = new Location("Loc1");
                                    loc1.setLatitude(o1.getLat());
                                    loc1.setLongitude(o1.getLng());

                                    Location loc2 = new Location("Loc2");
                                    loc2.setLatitude(o1.getLat());
                                    loc2.setLongitude(o1.getLng());

                                    double dist1 = loc1.distanceTo(currentLocation);
                                    double dist2 = loc2.distanceTo(currentLocation);
                                    Log.d(TAG, "compare: 1 " + dist1);
                                    Log.d(TAG, "compare: 2 " + dist2);
                                    return Double.valueOf(dist2).compareTo(dist1);
                                }
                            });
                            mAdapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(getActivity(), "Invalid location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
//        if(productList == null) return;
//        Collections.sort(productList, new Comparator<Product>(){
//            @Override
//            public int compare(Product o1, Product o2) {
//                Location loc1 = new Location("Loc1");
//                loc1.setLatitude(o1.getLat());
//                loc1.setLongitude(o1.getLng());
//
//                Location loc2 = new Location("Loc2");
//                loc2.setLatitude(o1.getLat());
//                loc2.setLongitude(o1.getLng());
//
//                double dist1 = loc1.distanceTo(currentLocation);
//                double dist2 = loc2.distanceTo(currentLocation);
//                return Double.valueOf(dist2).compareTo(dist1);
//            }
//        });
//        mAdapter.notifyDataSetChanged();
    }

}
