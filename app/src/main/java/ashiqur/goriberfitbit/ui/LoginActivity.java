package ashiqur.goriberfitbit.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.rest_api.ApiClient;
import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.service.ApiInterface;
import ashiqur.goriberfitbit.utils.FileUtil;
import ashiqur.goriberfitbit.utils.UiUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvDebug;
    private EditText etUsername, etPassword;
    private Button btnRequest, btnSignup;
    private String TAG = "LoginActivity :";
    private ProgressBar progressBar;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeXmlVariables();
        FileUtil.verifyStoragePermissions(LoginActivity.this);
        ConnectivityManager connectivityManager
                = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null)
            UiUtil.showErrorDialog(LoginActivity.this,"Network Error", " Cannot Find Connectivity Service ");
        else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (!(activeNetworkInfo != null && activeNetworkInfo.isConnected())) {
                UiUtil.showErrorDialog(LoginActivity.this, "Network Error", " Cannot Find Connectivity Service ");
            }

        }
        initializeXmlVariables();
        initializeJavaVariables();



    }

    private void initializeJavaVariables() {
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
        });
    }

    private void initializeXmlVariables() {
        tvDebug = findViewById(R.id.tv_debug);
        btnRequest = findViewById(R.id.button);
        btnRequest.setOnClickListener(this);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_pass);
        progressBar = findViewById(R.id.progress_bar);
        btnSignup = findViewById(R.id.btn_signup);

    }
    private void login() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

        String json = new Gson().toJson(new CustomResponse(etUsername.getText().toString().trim(), etPassword.getText().toString().trim()));
        tvDebug.setText(json);
        Log.wtf(TAG, "Sending Json :" +json);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        Call<CustomResponse> call = apiInterface.login(requestBody);

        progressBar.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<CustomResponse>() {
            @Override
            public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                if (response.isSuccessful()) {
                    String strResponseBody = response.body().toString();
                    Log.wtf(TAG,"Tokens Received :" + strResponseBody);
                    tvDebug.setText(strResponseBody);

                    progressBar.setVisibility(View.INVISIBLE);

                    ApiClient.setJwtAccessToken(response.body().access);
                    ApiClient.setJwtRefreshToken(response.body().refresh);

                    // TODO : Switch to Sensor Data Sending Activity
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                }
                if(response.errorBody() != null) {
                    try {
                        tvDebug.setText(response.errorBody().string() );
                        progressBar.setVisibility(View.INVISIBLE);

                        ApiClient.retrofit = null;

                    } catch (IOException e) {
                        tvDebug.setText(e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<CustomResponse> call, Throwable t) {
                String msg = "Unsuccessful : " + t.toString();
                tvDebug.setText(msg);
                ApiClient.retrofit = null;
                UiUtil.showErrorDialog(LoginActivity.this, "Unsuccessful Server Request", t.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onClick(View v) {
         login();
//        Intent i = new Intent(LoginActivity.this, MainActivity.class);
//        startActivity(i);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.wtf(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.wtf(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.wtf(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    UiUtil.showToast(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG);
                    //initialize our map
                    //initMap();
                }
            }
        }
    }
}
