package ashiqur.goriberfitbit.db.db_models;
public class SessionData {
    public double distance;
    public int leaps;
    public String startTime;
    public String endtime;

    public SessionData() {
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public SessionData(double distance, int leaps, String startTime, String endtime) {

        this.distance = distance;
        this.leaps = leaps;
        this.startTime = startTime;
        this.endtime = endtime;
    }

    public void setLeaps(int leaps) {
        this.leaps = leaps;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndtime(String endtime) {
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
