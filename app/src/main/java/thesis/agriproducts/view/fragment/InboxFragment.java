package thesis.agriproducts.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.domain.Services;
import thesis.agriproducts.model.CenterRepository;
import thesis.agriproducts.model.entities.Deal;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;
import thesis.agriproducts.view.adapter.InboxAdapter;

public class InboxFragment extends Fragment {

    //region Attributes
    int userId;
    int CURRENT_FLAG = 0;

    View mView;
    RecyclerView mRecyclerView;
    InboxAdapter mAdapter;
    ProgressBar mProgress;
    SwipeRefreshLayout mSwipeRefreshLayout;
    TextView mErrorView;
    List<Deal> dealList;
    //endregion

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_inbox, container, false);
        mProgress = mView.findViewById(R.id.progInbox);
        mErrorView = mView.findViewById(R.id.txtInboxError);
        mSwipeRefreshLayout = mView.findViewById(R.id.swipeViewInbox);
        mRecyclerView = mView.findViewById(R.id.recyclerViewInbox);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { fetchDeals(CURRENT_FLAG); }
        });
        userId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        Button mSellingBtn = mView.findViewById(R.id.btnSelling);
        Button mBuyingBtn= mView.findViewById(R.id.btnBuying);
        mSellingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDeals(Tags.SELLING_FLAG);
            }
        });
        mBuyingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDeals(Tags.BUYING_FLAG);
            }
        });
        return mView;
    }

    private void clearViews() {
        mRecyclerView.setAdapter(null);
        mErrorView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void fetchDeals(final int dealFlag) {
        clearViews();
        Utils.showProgress(true, mProgress, mRecyclerView);

        CURRENT_FLAG = dealFlag;
        Services api = Api.getInstance().getServices();
        Call<Result> call;
        call = (dealFlag == Tags.BUYING_FLAG) ? api.getBuying(userId) : api.getSelling(userId);
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.showProgress(false, mProgress, mRecyclerView);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    dealList = response.body().getDeals();
                    CenterRepository.getCenterRepository().setListOfDeals(dealList);
                    showDeals();
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

    private void showDeals() {
        mAdapter = new InboxAdapter(getActivity(), dealList, CURRENT_FLAG);
        mAdapter.setOnItemClickListener(new InboxAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Utils.setPosition(position);
                Utils.switchContent(getActivity(), R.id.fragContainer, Tags.MESSAGES_FRAGMENT);
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

}
