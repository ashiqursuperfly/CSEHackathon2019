package ashiqur.goriberfitbit.rest_api.api_models;
import com.google.gson.annotations.SerializedName;

public class CustomResponse {
    @SerializedName("username")
    public String username;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("refresh")
    public String refresh;

    @SerializedName("access")
    public String access;

    @SerializedName("detail")
    public String detail;

    public CustomResponse(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    public CustomResponse(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CustomResponse(String refresh) {
        this.refresh = refresh;
    }

    @Override
    public String toString() {
        return "CustomResponse{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", refresh='" + refresh + '\'' +
                ", access='" + access + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
