package ashiqur.goriberfitbit.ui;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.db.DBHelper;
import ashiqur.goriberfitbit.db.db_models.SessionData;
import ashiqur.goriberfitbit.rest_api.ApiClient;
import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.api_models.WorkoutSessionData;
import ashiqur.goriberfitbit.rest_api.service.ApiInterface;
import ashiqur.goriberfitbit.utils.FileUtil;
import ashiqur.goriberfitbit.utils.UiUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
class DataPoint {
    public static double avgZ;
    public static double avgY;
    public static double avgX;
    double x;
    double y;
    double z;

    public DataPoint(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void calibrate(List<DataPoint> data) {
        for (DataPoint d :
                data) {
            avgX += d.x;
            avgY += d.y;
            avgZ += d.z;
        }
        avgX = avgX / data.size();
        avgY = avgY / data.size();
        avgZ = avgZ / data.size();

    }
}

public class MotionDetectionFragment extends Fragment {

    // xml
    private TextView tvAcc, tvDebug, tvDistanceWalked;
    private Switch switchCalcDistance;
    // java
    DBHelper db;
    private Context context;
    private ArrayList<DataPoint> list;
    private boolean calibrationDone;
    private int SAMPLE_SIZE = 25;
    private static String TAG = "MotionDetection-Fragment";
    private static String fileName = "GoriberFitbit";
    private ArrayList<Location> points;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    private boolean mLocationPermissionsGranted;
    private FileUtil fileUtil;
    private static double G = 9.80;
    private double LEAP_THRESHOLD = 3.0;
    private int counter = 0;

    private float distance;
    private int leapscount;
    private String startTime;


    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        Activity a;
        context = c;
    }

    public MotionDetectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_motion_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeXmlVariables(view);
        initializeJavaVariables();
        updateMotionData();

        getLastLocationNewMethod();
        updateLeapCount();

    }

    private void updateLeapCount() {
        if (!(UiUtil.setSensor(Sensor.TYPE_LINEAR_ACCELERATION, (Activity) context, new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                // tvAcc.setText(Arrays.toString(event.values));
                if (!calibrationDone) {
                    list.add(new DataPoint(event.values[0], event.values[1], event.values[2]));
                    Log.wtf(TAG, list.size() + "");
                }
                if (list.size() < SAMPLE_SIZE) return;
                else if (!calibrationDone) {
                    DataPoint.calibrate(list);
                    calibrationDone = true;
                    UiUtil.showToast(context, "Calibration Successful", Toast.LENGTH_SHORT);
                }
                double x = event.values[0] - DataPoint.avgX;
                double y = event.values[1] - DataPoint.avgY;
                double z = event.values[2] - DataPoint.avgZ;

                String str = x + " " + y + " " + z;
                tvAcc.setText(str);

                counter--;
                if (counter > 0) return;
                if (y >= LEAP_THRESHOLD) {
                    counter = 3;
                    leapscount++;
                }

                // fileUtil.appendToTxtFile("(" +x+","+y+","+z+")" + "\n" , fileName);

//        double normalize_z_value = x / Math.sqrt(x*x + y*y + z*z);
//        double normalize_x_value = y / Math.sqrt(x*x + y*y + z*z);
//        double normalize_y_value = z / Math.sqrt(x*x + y*y + z*z);
//
//        float angleZ = (float) Math.acos(normalize_z_value);
//        float angleY = (float) Math.acos(normalize_y_value);
//        float angleX = (float) Math.acos(normalize_x_value);

//        String s = angleX *(180/Math.PI) + " " + angleY*(180/Math.PI) + " " + angleZ*(180/Math.PI);

                //     tvAcc.setText(s);

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }))) {
            UiUtil.showErrorDialog((Activity) context, "SENSOR NOT FOUND !", "Accelerometer Sensor Not Available in the Device");
        }

    }

    private void initializeJavaVariables() {
        list = new ArrayList<>();
        fileUtil = new FileUtil(fileName);
        points = new ArrayList<>();
        db = new DBHelper(context);
        db.createSessionData(new SessionData(23, 123, "dsa", "dsa"));
        db.createSessionData(new SessionData(2323, 321123, "dasdsa", "ADsdsa"));

        Log.wtf(TAG, db.getAllSessions().toString());
    }

    private void initializeXmlVariables(View view) {
        tvAcc = view.findViewById(R.id.tv_accelerometer);
        tvDebug = view.findViewById(R.id.tv_debug);
        tvDistanceWalked = view.findViewById(R.id.tv_distance);
        switchCalcDistance = view.findViewById(R.id.switch_calc_distance);
    }

    private void updateMotionData() {
        switchCalcDistance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.wtf("Switch State=", "" + isChecked);
                if (!isChecked) {
                    //TODO : Push to server
                    Log.wtf(TAG, "Pushing to Server");
                    tvDebug.setText("Pushing to Server");
                    UiUtil.showAlertDialog((Activity) context, "END OF SESSION", "You've Walked " + distance + " Metres " + "leaped " + leapscount + "times",
                            "Okay", R.color.colorAccent, R.color.colorPrimaryDark);
                    pushData();
                    leapscount = 0;
                    distance = 0;
                } else
                    startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).format(Calendar.getInstance().getTime());
            }
        });

    }


    private void pushData() {

        final WorkoutSessionData newSession = new WorkoutSessionData(distance, leapscount, startTime,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).format(Calendar.getInstance().getTime()));
        String json = new Gson().toJson(newSession);
        tvDebug.setText(json);
        Log.wtf(TAG, "Sending :" + json);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        ApiInterface apiInterface = ApiClient.getClientWithAuth().create(ApiInterface.class);
        Call<CustomResponse> call = apiInterface.pushMotionData(requestBody);

        call.enqueue(new Callback<CustomResponse>() {
            @Override
            public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                if (response.isSuccessful()) {
                    tvDebug.setText(response.body().toString());
                    SessionData newSessionLocal = new SessionData(newSession.distance, newSession.leaps, newSession.startTime, newSession.endtime);
                    db.createSessionData(newSessionLocal);
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject j = new JSONObject(response.errorBody().string());
                        String detail = j.get("detail").toString().trim();
                        if (detail.equalsIgnoreCase("Given token not valid for any token type")) {
                            tvDebug.setText("Probable Token Timeout :" + response.raw().toString());
                            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

                            String json = new Gson().toJson(new CustomResponse(ApiClient.jwtRefreshToken));
                            tvDebug.setText("Sending Json :" + json);
                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                            Call<CustomResponse> call2 = apiInterface.getAccessToken(requestBody);

                            call2.enqueue(new Callback<CustomResponse>() {
                                @Override
                                public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                                    if (response.isSuccessful()) {
                                        ApiClient.jwtAccessToken = response.body().access;
                                    } else
                                        UiUtil.showAlertDialog((Activity) context, "Cannot Connect",
                                                "Access Token not found !", "DISMISS", R.color.colorPrimary, R.color.colorAccent);
                                }

                                @Override
                                public void onFailure(Call<CustomResponse> call, Throwable t) {
                                }
                            });
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    UiUtil.showAlertDialog((Activity) context, "Cannot Connect",
                            response.body().detail, "DISMISS", R.color.colorPrimary, R.color.colorAccent);
                }
            }

            @Override
            public void onFailure(Call<CustomResponse> call, Throwable t) {
                UiUtil.showErrorDialog((Activity) context, "Network Failure !", t.toString());
            }
        });
    }

//    private parseStringIntoDate(String date)
//    {
//
//    }

    private void getLastLocationNewMethod() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationPermission();
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            points.add(location);
                            tvDebug.setText(location.toString());
                            if (points.size() >= 2) {
                                Location l1 = points.get(points.size() - 2);
                                Location l2 = points.get(points.size() - 1);
                                String dStr = "Distance :" + l1.distanceTo(l2) + "";
                                Log.wtf(TAG, l1.getLatitude() + " " + l1.getLongitude() + " "
                                        + l1.getLatitude() + " " + l1.getLongitude());
                                dStr += "Distance 2 :" + meterDistanceBetweenPoints(l1.getLatitude(),
                                        l1.getLongitude(), l2.getLatitude(), l2.getLongitude());
                                distance += (points.get(points.size() - 2).distanceTo(points.get(points.size() - 1)));
                                tvAcc.setText(dStr);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void getLocationPermission() {
        Log.wtf(TAG, "getLocationPermission: getting location permissions");

        if (ContextCompat.checkSelfPermission(context,
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(context,
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                Log.wtf(TAG, "Permission : permission already granted before");
                // switchFragment(R.id.navdrawer_browserides);
            } else {
                ActivityCompat.requestPermissions((Activity) context,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions((Activity) context,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private double meterDistanceBetweenPoints(double lat_a, double lng_a, double lat_b, double lng_b) {
        float pk = (float) (180.f / Math.PI);

        double a1 = lat_a / pk;
        double a2 = lng_a / pk;
        double b1 = lat_b / pk;
        double b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }
}
