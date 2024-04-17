package thesis.agriproducts.domain;

import android.app.ProgressDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import thesis.agriproducts.util.Utils;

public class ApiHelper {

    //region Initialize
    private static ApiHelper apiHelper;

    public static ApiHelper getApiHelper() {

        if (null == apiHelper) {
            apiHelper = new ApiHelper();
        }
        return apiHelper;
    }
    //endregion

    public static void handleError(String error, TextView mErrorView, ProgressDialog pDialog) {
        Utils.dismissProgressDialog(pDialog);
        mErrorView.setText(error);
        mErrorView.setVisibility(View.VISIBLE);
    }

    public static void handleError(String error, TextView mErrorView, ProgressBar mProgress, RecyclerView mRecyclerView) {
        Utils.showProgress(false, mProgress, mRecyclerView);
        mErrorView.setText(error);
        mErrorView.setVisibility(View.VISIBLE);
    }
}
