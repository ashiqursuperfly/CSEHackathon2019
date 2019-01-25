package ashiqur.goriberfitbit.rest_api.service;

import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.api_models.Leaderboard;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @GET("leaderboard/")
    Call<Leaderboard> getLeaderboard();

    @POST("api/token/")
    Call<CustomResponse> login(@Body RequestBody requestBody);

    @POST("api/token/refresh/")
    Call<CustomResponse> getAccessToken(@Body RequestBody requestBody);

    @POST("signup/")
    Call<CustomResponse> signup(@Body RequestBody requestBody);


    @POST("data/session/")
    Call<CustomResponse> pushMotionData(@Body RequestBody requestBody);

    @POST("data/heart/")
    Call<CustomResponse> pushHeartBeatData(@Body RequestBody requestBody);

}
