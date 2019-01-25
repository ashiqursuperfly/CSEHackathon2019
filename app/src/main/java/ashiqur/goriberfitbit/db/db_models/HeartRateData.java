package ashiqur.goriberfitbit.db.db_models;

import com.google.gson.annotations.SerializedName;

public class HeartRateData {
    @SerializedName("bpm")
    public String bpm;

    @SerializedName("timestamp")
    public String timestamp;

    public HeartRateData(String bpm, String timestamp) {
        this.bpm = bpm;
        this.timestamp = timestamp;
    }

    public String getBpm() {
        return bpm;
    }

    public void setBpm(String bpm) {
        this.bpm = bpm;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
