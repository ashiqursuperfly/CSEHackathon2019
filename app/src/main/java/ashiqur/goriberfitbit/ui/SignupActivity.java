package ashiqur.goriberfitbit.ui;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.rest_api.ApiClient;
import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.service.ApiInterface;
import ashiqur.goriberfitbit.utils.UiUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ashiqur.goriberfitbit.utils.UiUtil.showErrorDialog;
import static ashiqur.goriberfitbit.utils.UiUtil.showSnackBar;

public class SignupActivity extends AppCompatActivity {

    private EditText etUserName, etPassword, etMail, etConfirmPass;
    private Button btnSignup;
    private static String TAG = "Signup-Fragment";
    private TextView tvDebug;
    private ProgressBar progressBar;

    private void initializeXmlVariables() {

        progressBar = findViewById(R.id.progress_bar);
        etUserName = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_pass);
        etConfirmPass = findViewById(R.id.et_confirm_pass);
        tvDebug = findViewById(R.id.tv_debug);
        etMail = findViewById(R.id.et_mail);
        btnSignup = findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkUserInputError()) return;
                else
                {
                    signup();
                }

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initializeXmlVariables();
        initializeJavaVariables();
    }

    private void initializeJavaVariables() {

    }

//    private void signup2()
//    {
//        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
//        try {
//            JSONObject paramObject = new JSONObject();
//            paramObject.put("email", etMail.getText().toString().trim());
//            paramObject.put("username", etUserName.getText().toString().trim());
//            paramObject.put("password", etPassword.getText().toString().trim());
//
//            Log.wtf(TAG, "Sending JSON :" + paramObject.toString());
//            Call<CustomResponse> userCall = apiInterface.signup(paramObject.toString());
//            userCall.enqueue(new Callback<CustomResponse>() {
//                @Override
//                public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
//                    if (response.isSuccessful()) {
//                        String strResponseBody = response.body().toString();
//                        Log.wtf(TAG,"Signup Response Received :" + strResponseBody);
//                        tvDebug.setText(strResponseBody);
//
//                        progressBar.setVisibility(View.INVISIBLE);
//
//                        if(response.body().detail.equals("success")){
//                            // TODO : Switch to Sensor Data Sending Activity
//                            UiUtil.showSnackBar(findViewById(android.R.id.content), "Please Check Your Inbox and Confirm Your Email\n" +
//                                            " Make Sure you check spam folder",
//                                    Snackbar.LENGTH_LONG,
//                                    "Okay", UiUtil.DO_NOTHING);
//                            Intent i = new Intent(SignupActivity.this, MainActivity.class);
//                            startActivity(i);
//                        }
//                        else if(response.body().detail.equals("failure"))
//                        {
//                            ApiClient.retrofit = null;
//                            showErrorDialog(SignupActivity.this,"Signup Failed", "Try Again Later");
//                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//
//                        else {
//
//                            ApiClient.retrofit = null;
//                            showErrorDialog(SignupActivity.this,"Signup Failed", response.body().detail);
//                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//
//                    }
//                    if(response.errorBody() != null) {
//                        try {
//                            tvDebug.setText(response.errorBody().string() );
//                            progressBar.setVisibility(View.INVISIBLE);
//
//                        } catch (IOException e) {
//                            tvDebug.setText(e.toString());
//                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<CustomResponse> call, Throwable t) {
//                    String msg = "Unsuccessful : " + t.toString();
//                    tvDebug.setText(msg);
//                    UiUtil.showErrorDialog(SignupActivity.this, "Unsuccessful Server Request", t.getMessage());
//                    progressBar.setVisibility(View.INVISIBLE);
//                }
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//    }
    private void signup() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

        String json = new Gson().toJson(new CustomResponse(etUserName.getText().toString().trim(),
                        etPassword.getText().toString().trim(),
                        etMail.getText().toString().trim()));
        tvDebug.setText(json);

        Log.wtf(TAG, "Sending Json :" + json);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Log.wtf(TAG, requestBody.toString());

        Call<CustomResponse> call = apiInterface.signup(requestBody);

        progressBar.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<CustomResponse>() {
            @Override
            public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                if (response.isSuccessful()) {
                    String strResponseBody = response.body().toString();
                    Log.wtf(TAG,"Signup Response Received :" + strResponseBody);
                    tvDebug.setText(strResponseBody);

                    progressBar.setVisibility(View.INVISIBLE);

                    if(response.body().detail.equals("success")){
                        // TODO : Switch to Sensor Data Sending Activity
                        UiUtil.showSnackBar(findViewById(android.R.id.content), "Please Check Your Inbox and Confirm Your Email\n" +
                                        " Make Sure you check spam folder",
                                Snackbar.LENGTH_LONG,
                                "Okay", UiUtil.DO_NOTHING);
                        Intent i = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(i);
                    }
                    else if(response.body().detail.equals("failure"))
                    {
                        ApiClient.retrofit = null;
                        showErrorDialog(SignupActivity.this,"Signup Failed", "Try Again Later");
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    else {

                        ApiClient.retrofit = null;
                        showErrorDialog(SignupActivity.this,"Signup Failed", response.body().detail);
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                }
                if(response.errorBody() != null) {
                    try {
                        tvDebug.setText(response.errorBody().string() );
                        progressBar.setVisibility(View.INVISIBLE);

                    } catch (IOException e) {
                        tvDebug.setText(e.toString());
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<CustomResponse> call, Throwable t) {
                String msg = "Unsuccessful : " + t.toString();
                tvDebug.setText(msg);
                UiUtil.showErrorDialog(SignupActivity.this, "Unsuccessful Server Request", t.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    private boolean checkUserInputError()
    {
        String email = etMail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPass = etConfirmPass.getText().toString().trim();
        boolean isOK = false;

        if(email.isEmpty() || password.isEmpty()  || confirmPass.isEmpty())
        {
            showSnackBar(findViewById(android.R.id.content),"One Or More Empty Fields", Snackbar.LENGTH_SHORT,"DISMISS", UiUtil.DO_NOTHING);
            return isOK;
        }
        if(!password.equals(confirmPass))
        {
            Log.wtf(TAG,"Pass :"+password+" Confirmed Pass:"+confirmPass);
            etConfirmPass.requestFocus();
            etConfirmPass.setError("Passwords Dont Match");
            return isOK;
        }
        if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+")) {
            etMail.requestFocus();
            etMail.setError("Invalid Email Address");

            return isOK;
        }

//        if(!passwordValidation(password))
//        {
//            etPassword.requestFocus();
//            etPassword.setError("Invalid Pass+\n+should be atleast of length 8 and must contain atleast one number(0-9)+\n+" +
//                    "atleast one special character+\n+" +
//                    "and letters");
//
//            return isOK;
//        }

        isOK=true;
        return isOK;
    }

}
