package ashiqur.goriberfitbit.rest_api;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static String baseUrl = "https://csehackathon.tahmeedtarek.com/";//"http://192.168.43.158:8000/"; //
    public static Retrofit retrofit = null;
    public static String jwtAccessToken = null;
    public static String jwtRefreshToken = null;

    private static OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request newRequest  = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + jwtAccessToken)
                    .build();
            return chain.proceed(newRequest);
        }
    }).build();

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    public static Retrofit getClientWithAuth()
    {
        // Adds Authorization header
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }

    public static void setJwtAccessToken(String jwtAccessToken) {
        ApiClient.jwtAccessToken = jwtAccessToken;
    }

    public static void setJwtRefreshToken(String jwtRefreshToken) {
        ApiClient.jwtRefreshToken = jwtRefreshToken;
    }
}
