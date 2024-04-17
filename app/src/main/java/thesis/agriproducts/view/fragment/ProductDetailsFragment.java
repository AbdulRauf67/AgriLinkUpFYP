package thesis.agriproducts.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

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
import thesis.agriproducts.model.entities.User;
import thesis.agriproducts.util.SharedPrefManager;
import thesis.agriproducts.util.Tags;
import thesis.agriproducts.util.Utils;


public class ProductDetailsFragment extends Fragment {

    //region Attributes
    String TAG = "Product Details Fragment";

    View mView, mFormView, mFormEdit, mFormButtons;
    TextView mProductName, mPrice, mSeller, mQuantity, mStatus, mLocation, mDescription, mErrorView;
    EditText mEditProductName, mEditLocation, mEditPrice, mEditQuantity, mEditUnit, mEditDescription, mEditStatus;
    ImageView mImage;
    Button mMakeOffer, mDelete, mEdit, mSave, mOrder;
    ProgressDialog pDialog;

    User user;
    Product product, newProduct;

    int position = 0;
    int PLACE_PICKER_REQUEST = 1;
    LatLng location;
    RequestBody productIdBody, productNameBody, descriptionBody, quantityBody, unitBody, priceBody, locationBody, latBody, lngBody, statusBody;
    //endregion


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_product_details, container, false);
        mFormView = mView.findViewById(R.id.formViewProduct);
        mFormEdit = mView.findViewById(R.id.formEditProduct);
        mFormButtons = mView.findViewById(R.id.formProductButtons);
        mErrorView = mView.findViewById(R.id.txtEditError);

        mProductName = mView.findViewById(R.id.txtProdName);
        mPrice = mView.findViewById(R.id.txtProdPrice);
        mSeller = mView.findViewById(R.id.txtProdSeller);
        mQuantity = mView.findViewById(R.id.txtProdQuantity);
        mStatus = mView.findViewById(R.id.txtProdStatus);
        mLocation = mView.findViewById(R.id.txtProdLocation);
        mDescription = mView.findViewById(R.id.txtProdDescription);
        mImage = mView.findViewById(R.id.imgProduct);

        mEditProductName = mView.findViewById(R.id.txtEditProductName);
        mEditLocation = mView.findViewById(R.id.txtEditLocation);
        mEditPrice = mView.findViewById(R.id.txtEditPrice);
        mEditQuantity = mView.findViewById(R.id.txtEditQuantity);
        mEditStatus = mView.findViewById(R.id.txtEditStatus);
        mEditUnit = mView.findViewById(R.id.txtEditUnit);
        mEditDescription = mView.findViewById(R.id.txtEditDescription);

        mMakeOffer = mView.findViewById(R.id.btnMakeOffer);
        mDelete = mView.findViewById(R.id.btnDeleteProduct);
        mEdit = mView.findViewById(R.id.btnEditProduct);
        mSave = mView.findViewById(R.id.btnSaveProduct);
        mOrder = mView.findViewById(R.id.btnOrder);

        mMakeOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageAlert();
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteProduct();
            }
        });
        mEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProduct();
            }
        });
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
        mOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setPosition(position);
                Utils.switchContent(getActivity(), R.id.fragContainer, Tags.PAYMENT_FRAGMENT);
            }
        });
        mSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setSupplierId(product.getUser().getUserId());
                Utils.setIsSupplier(true);
                Utils.switchContent(getActivity(), R.id.fragContainer, Tags.MY_PRODUCTS_FRAGMENT);
            }
        });

        Log.d(TAG, "onCreateView: position " + position);
        product = CenterRepository.getCenterRepository().getListOfProducts().get(position);
        showButton();
        showProduct();
        return mView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                Place place = PlacePicker.getPlace(data, getActivity());
                location = place.getLatLng();
                mEditLocation.setText(place.getAddress().toString());
            }
        }

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

    public void setPosition(int position) {
        this.position = position;
    }

    private void showButton() {
        user = SharedPrefManager.getInstance().getUser(getActivity());
        Log.d(TAG, "showButton: accountId " + user.getAccountId());
        if(user.getAccountId() == Tags.ADMIN) {
            mMakeOffer.setVisibility(View.GONE);
            mFormButtons.setVisibility(View.VISIBLE);
        } else if(product.getUser().getUserId() == user.getUserId()) {
            mMakeOffer.setVisibility(View.GONE);
            mFormButtons.setVisibility(View.VISIBLE);
            mEdit.setVisibility(View.VISIBLE);
            mOrder.setVisibility(View.GONE);
        } else {
            mMakeOffer.setVisibility(View.VISIBLE);
            mFormButtons.setVisibility(View.GONE);
        }
    }

    private void showProduct() {
        mFormView.setVisibility(View.VISIBLE);
        mFormEdit.setVisibility(View.GONE);
        if(user.getAccountId() == Tags.USER) {
            mEdit.setVisibility(View.VISIBLE);
            mSave.setVisibility(View.GONE);
        }
        String price = "PHP " + String.valueOf(product.getPrice()) + " per Unit";
        String quantity = String.valueOf(product.getQuantity() + product.getUnit());
        mProductName.setText(product.getProductName());
        mPrice.setText(price);
        mStatus.setText(product.getStatus());
        mSeller.setText(product.getUser().getName());
        mQuantity.setText(quantity);
        mLocation.setText(product.getLocation());
        mDescription.setText(product.getDescription());
        Picasso.get()
                .load(product.getProductUrl())
                .placeholder(R.drawable.ic_photo_light_blue_24dp)
                .error(R.drawable.ic_error_outline_red_24dp)
                .fit()
                .centerInside()
                .into(mImage);
    }

    private void messageAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final EditText mMessage = new EditText (getActivity());
        mMessage.setHint("message");
        mMessage.setText("I want to buy your product.");
        alert.setTitle("Enter Message");
        alert.setView(mMessage);
        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setDeal(mMessage.getText().toString());
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { dialog.dismiss();
            }
        });
        alert.show();
    }

    private void setDeal(String content) {
        pDialog = Utils.showProgressDialog(getActivity(), "Sending a deal...");
        Api.getInstance().getServices().setDeal(product.getProductId(), user.getUserId(), content).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Utils.switchContent(getActivity(), R.id.fragContainer, Tags.HOME_FRAGMENT);
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

    private void deleteProduct() {
        pDialog = Utils.showProgressDialog(getActivity(), "Deleting product...");
        Api.getInstance().getServices().deleteProduct(product.getProductId()).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull final Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    Utils.switchContent(getActivity(), R.id.fragContainer, Tags.HOME_FRAGMENT);
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

    private void editProduct() {
        mEditLocation.setInputType(InputType.TYPE_NULL);
        mEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDestination();
            }
        });
        mEditLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) { openDestination(); }
        });
        mEditProductName.setText(product.getProductName());
        mEditLocation.setText(product.getLocation());
        mEditPrice.setText(String.valueOf(product.getPrice()));
        mEditQuantity.setText(String.valueOf(product.getQuantity()));
        mEditUnit.setText(product.getUnit());
        mEditDescription.setText(product.getDescription());
        mEditStatus.setText(product.getStatus());
        location = new LatLng(product.getLat(), product.getLng());

        mFormView.setVisibility(View.GONE);
        mFormEdit.setVisibility(View.VISIBLE);
        mEdit.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
    }

    private void saveProduct() {
        if(!authenticate()) return;
        parseRequestBody();
        pDialog = Utils.showProgressDialog(getActivity(), "Updating your product...");
        parseRequestBody();
        Api.getInstance().getServices().updateProduct(productIdBody, productNameBody, descriptionBody, quantityBody, unitBody, priceBody, locationBody, latBody, lngBody, statusBody).enqueue(new Callback<Result>() {
            @Override
            public void onResponse(@Nullable Call<Result> call, @NonNull Response<Result> response) {
                try {
                    Utils.dismissProgressDialog(pDialog);
                    if (response.errorBody() != null)
                        throw new Exception(response.errorBody().string());
                    if (response.body().getError())
                        throw new Exception(response.body().getMessage());
                    Toast.makeText(getActivity(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    showProduct();
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
        newProduct = new Product(
                mEditProductName.getText().toString(),
                mEditDescription.getText().toString(),
                Double.valueOf(mEditQuantity.getText().toString()),
                mEditUnit.getText().toString(),
                Double.valueOf(mEditPrice.getText().toString()),
                mEditLocation.getText().toString(),
                location.latitude,
                location.longitude,
                mEditStatus.getText().toString()

        );
        newProduct.setProductId(product.getProductId());

        if (Utils.isEmptyFields(product.getProductName(), product.getDescription(), mEditQuantity.getText().toString(), mEditUnit.getText().toString(), mEditPrice.getText().toString(), mEditLocation.getText().toString())) {
            mErrorView.setText(R.string.error_sell_product);
            mErrorView.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private void parseRequestBody() {
        productIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getProductId()));
        productNameBody = RequestBody.create(MediaType.parse("text/plain"), newProduct.getProductName());
        descriptionBody = RequestBody.create(MediaType.parse("text/plain"), newProduct.getDescription());
        quantityBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getQuantity()));
        unitBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getUnit()));
        priceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getPrice()));
        locationBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getLocation()));
        latBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getLat()));
        lngBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getLng()));
        statusBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(newProduct.getStatus()));
    }
}
