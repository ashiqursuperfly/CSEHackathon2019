package ashiqur.goriberfitbit.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.rest_api.ApiClient;
import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.api_models.Leaderboard;
import ashiqur.goriberfitbit.rest_api.service.ApiInterface;
import ashiqur.goriberfitbit.utils.UiUtil;
import hivatec.ir.suradapter.SURAdapter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ashiqur.goriberfitbit.utils.UiUtil.showErrorDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment {


    private TextView tv1, tv2, tv3, tv4, tv5;
    private Context context;
    private String TAG = "Leaderboard";
    private ArrayList<TwoItemDataModel> itemList;
    private SURAdapter recyclerViewAdapter;
    RecyclerView recyclerView;
    private View view;


    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        Activity a;
        context = c;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;

        tv1 = view.findViewById(R.id.tv1);

        tv2 = view.findViewById(R.id.tv2);
        tv3 = view.findViewById(R.id.tv3);
        tv4 = view.findViewById(R.id.tv4);
        tv5 = view.findViewById(R.id.tv5);
        setLeaderBoardData();
    }

    private void setLeaderBoardData() {
        setupLeaderboard();


    }


    private void setupLeaderboard()  {

        ApiInterface apiInterface = ApiClient.getClientWithAuth().create(ApiInterface.class);

        Call<Leaderboard> userCall = apiInterface.getLeaderboard();
        userCall.enqueue(new Callback<Leaderboard>() {
            @Override
            public void onResponse(Call<Leaderboard> call, Response<Leaderboard> response) {
                if (response.isSuccessful()) {
                    itemList = new ArrayList<>();
                    String strResponseBody = response.body().toString();

                    tv1.setText(new TwoItemDataModel(response.body()._1.username,response.body()._1.score).toString());
                    tv2.setText(new TwoItemDataModel(response.body()._2.username,response.body()._2.score).toString());
                    tv3.setText(new TwoItemDataModel(response.body()._3.username,response.body()._3.score).toString());
                    tv4.setText(new TwoItemDataModel(response.body()._4.username,response.body()._4.score).toString());
                    tv5.setText(new TwoItemDataModel(response.body()._5.username,response.body()._5.score).toString());
                    Log.wtf(TAG,"Signup Response Received :" + strResponseBody);
                }
                if(response.errorBody() != null) {
                    try {
                        JSONObject j = new JSONObject(response.errorBody().string());
                        String detail = j.get("detail").toString().trim();
                        if(detail.equalsIgnoreCase("Given token not valid for any token type"))
                        {
                            Log.wtf(TAG,"Probable Token Timeout :" + response.raw().toString());
                            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

                            String json = new Gson().toJson(new CustomResponse(ApiClient.jwtRefreshToken));
                            Log.wtf(TAG,"Sending Json :" + json);
                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                            Call<CustomResponse> call2 = apiInterface.getAccessToken(requestBody);

                            call2.enqueue(new Callback<CustomResponse>() {
                                @Override
                                public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                                    if(response.isSuccessful()){
                                        ApiClient.jwtAccessToken = response.body().access;
                                    }
                                    else UiUtil.showAlertDialog((Activity)context, "Cannot Connect",
                                            "Access Token not found !", "DISMISS", R.color.colorPrimary,R.color.colorAccent);
                                }
                                @Override
                                public void onFailure(Call<CustomResponse> call, Throwable t) {
                                }
                            });
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(Call<Leaderboard> call, Throwable t) {
                String msg = "Unsuccessful : " + t.toString();
                showErrorDialog((Activity)context, "Unsuccessful Server Request", t.getMessage());

            }
        });

    }

}
