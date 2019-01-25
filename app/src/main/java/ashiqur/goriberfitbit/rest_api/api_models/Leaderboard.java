package ashiqur.goriberfitbit.rest_api.api_models;

import com.google.gson.annotations.SerializedName;

public class Leaderboard {
    @SerializedName("1")
    public User _1;
    @SerializedName("2")
    public User _2;
    @SerializedName("3")
    public User _3;
    @SerializedName("4")
    public User _4;
    @SerializedName("5")
    public User _5;

    @Override
    public String toString() {
        return "Leaderboard{" +
                "_1=" + _1 +
                ", _2=" + _2 +
                ", _3=" + _3 +
                ", _4=" + _4 +
                ", _5=" + _5 +
                '}';
    }
}
