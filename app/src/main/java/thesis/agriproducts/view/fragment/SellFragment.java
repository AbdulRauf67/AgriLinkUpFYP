package thesis.agriproducts.view.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.jetbrains.annotations.Nullable;

import java.io.File;

import id.zelory.compressor.Compressor;
import io.reactivex.annotations.NonNull;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import thesis.agriproducts.R;
import thesis.agriproducts.domain.Api;
import thesis.agriproducts.domain.ApiHelper;
import thesis.agriproducts.model.entities.Product;
import thesis.agriproducts.model.entities.Result;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;

public class SellFragment extends Fragment {

    //region Attributes
    View mView;
    EditText mProductName, mLocation, mPrice, mQuantity, mUnit, mDescription, mStatus;
    ImageView mProductImage;
    TextView mErrorView;
    ProgressDialog pDialog;

    int PICK_IMAGE_REQUEST = 1;
    int PLACE_PICKER_REQUEST = 2;

    Uri fileUri;
    RequestBody sellerIdBody, productNameBody, descriptionBody, quantityBody, unitBody, priceBody, locationBody, latBody, lngBody, statusBody, fileBody;
    LatLng location;
    Product product;
    int sellerId;
    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_product_sell, container, false);

        // Initialize Views
        mProductName = mView.findViewById(R.id.txtSellProductName);
        mLocation = mView.findViewById(R.id.txtSellLocation);
        mPrice = mView.findViewById(R.id.txtSellPrice);
        mQuantity = mView.findViewById(R.id.txtSellQuantity);
        mUnit = mView.findViewById(R.id.txtSellUnit);
        mDescription = mView.findViewById(R.id.txtSellDescription);
        mStatus = mView.findViewById(R.id.txtSellStatus);
        mProductImage = mView.findViewById(R.id.imgSellProduct);
        mErrorView = mView.findViewById(R.id.txtSellError);
        Button mSellProduct = mView.findViewById(R.id.btnSell);

        mLocation.setInputType(InputType.TYPE_NULL);
        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDestination();
            }
        });
        mLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) { if(hasFocus) openDestination(); }
        });
        mProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        mSellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticate();
            }
        });

        return mView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                try {
                    fileUri = data.getData();
                    mProductImage.setImageBitmap(new Compressor(getActivity())
                            .compressToBitmap(new File(Utils.getRealPathFromURI(getActivity(), fileUri))));
                    mProductImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, getActivity());
                location = place.getLatLng();
                mLocation.setText(place.getAddress().toString());
            }
        }

    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
    }

    public void openDestination() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            Intent intent = builder.build(getActivity());
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        }
        catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        }
    }

    private void authenticate() {
        mErrorView.setVisibility(View.GONE);
        sellerId = SharedPrefManager.getInstance().getUser(getActivity()).getUserId();
        product = new Product(
                sellerId,
                mProductName.getText().toString(),
                mDescription.getText().toString(),
                Double.valueOf(mQuantity.getText().toString()),
                mUnit.getText().toString(),
                Double.valueOf(mPrice.getText().toString()),
                mLocation.getText().toString(),
                location.latitude,
                location.longitude,
                mStatus.getText().toString()

        );

        if (Utils.isEmptyFields(product.getProductName(), product.getDescription(), mQuantity.getText().toString(), mUnit.getText().toString(), mPrice.getText().toString(), mLocation.getText().toString())) {
            mErrorView.setText(R.string.error_sell_product);
            mErrorView.setVisibility(View.VISIBLE);
        } else if (fileUri == null) {
            mErrorView.setText(R.string.error_product_image);
            mErrorView.setVisibility(View.VISIBLE);
        } else {
            Utils.hideKeyboard(getActivity());
            fetchProduct();
        }
    }

    private void fetchProduct() {
        pDialog = Utils.showProgressDialog(getActivity(), "Posting your product...");
        parseRequestBody();
        Api.getInstance().getServices().setProduct(sellerIdBody, productNameBody, descriptionBody, quantityBody, unitBody, priceBody, locationBody, latBody, lngBody, statusBody, fileBody).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    Utils.switchContent(getActivity(), R.id.fragContainer, Tags.MY_PRODUCTS_FRAGMENT);
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

    private void parseRequestBody() {
        File filePath = new File(Utils.getRealPathFromURI(getActivity(), fileUri));
        try {
            sellerIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(sellerId));
            productNameBody = RequestBody.create(MediaType.parse("text/plain"), product.getProductName());
            descriptionBody = RequestBody.create(MediaType.parse("text/plain"), product.getDescription());
            quantityBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getQuantity()));
            unitBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getUnit()));
            priceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getPrice()));
            locationBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getLocation()));
            latBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getLat()));
            lngBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(product.getLng()));
            statusBody = RequestBody.create(MediaType.parse("text/plain"), product.getStatus());
            fileBody = RequestBody.create(MediaType.parse(getActivity().getContentResolver().getType(fileUri)), new Compressor(getActivity()).compressToFile(filePath));
        } catch (Exception ex) {

        }
    }

}
