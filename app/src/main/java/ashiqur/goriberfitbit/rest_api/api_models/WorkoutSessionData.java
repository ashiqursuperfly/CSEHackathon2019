package ashiqur.goriberfitbit.rest_api.api_models;

import com.google.gson.annotations.SerializedName;

public class WorkoutSessionData {
    @SerializedName("distance")
    public float distance;
    @SerializedName("leap_count")
    public int leaps;
    @SerializedName("start_time")
    public String startTime;
    @SerializedName("end_time")
    public String endtime;

    public WorkoutSessionData(float distance, int leaps, String startTime, String endtime) {
        this.distance = distance;
        this.leaps = leaps;
        this.startTime = startTime;
        this.endtime = endtime;
    }

    @Override
    public String toString() {
        return "WorkoutSessionData{" +
                "distance=" + distance +
                ", leaps=" + leaps +
                ", startTime='" + startTime + '\'' +
                ", endtime='" + endtime + '\'' +
                '}';
    }
}
