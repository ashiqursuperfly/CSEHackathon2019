package ashiqur.goriberfitbit.ui;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import ashiqur.goriberfitbit.R;
import ashiqur.goriberfitbit.db.db_models.HeartRateData;
import ashiqur.goriberfitbit.db.db_models.SessionData;
import ashiqur.goriberfitbit.rest_api.ApiClient;
import ashiqur.goriberfitbit.rest_api.api_models.CustomResponse;
import ashiqur.goriberfitbit.rest_api.api_models.WorkoutSessionData;
import ashiqur.goriberfitbit.rest_api.service.ApiInterface;
import ashiqur.goriberfitbit.utils.UiUtil;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * This class extends Activity to handle a picture preview, process the preview
 * for a red values and determine a heart beat.
 * 
 * @author Justin Wetherell <phishman3579@gmail.com>
 */
public class HeartRateMonitor extends Activity {

    private static final String TAG = "HeartRateMonitor";
    private static final AtomicBoolean processing = new AtomicBoolean(false);

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static View image = null;
    private static TextView text = null;

    private static WakeLock wakeLock = null;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];

    public static enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }

    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private static double beats = 0;
    private static long startTime = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heartrate_activity);


        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        image = findViewById(R.id.image);
        text = (TextView) findViewById(R.id.text);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "myapp:DoNotDimScreen");
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();

        wakeLock.acquire();

        camera = Camera.open();

        startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();

        wakeLock.release();

        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private static PreviewCallback previewCallback = new PreviewCallback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;

            int imgAvg = ImageProcessingUtils.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }

            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
            TYPE newType = currentType;
            if (imgAvg < rollingAverage) {
                newType = TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            // Transitioned from one state to another to the same
            if (newType != currentType) {
                currentType = newType;
                if(image != null)image.postInvalidate();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 10) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
                if (dpm < 30 || dpm > 180) {
                    startTime = System.currentTimeMillis();
                    beats = 0;
                    processing.set(false);
                    return;
                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                text.setText(String.valueOf(beatsAvg));

                //TODO :

                startTime = System.currentTimeMillis();
                beats = 0;
            }
            processing.set(false);
        }
    };

    private static SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreviewSurfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea) result = size;
                }
            }
        }

        return result;
    }
    public void pushData() {

        final HeartRateData newSession = new HeartRateData(text.getText().toString(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA).format(Calendar.getInstance().getTime()));
        String json = new Gson().toJson(newSession);
        Log.wtf(TAG, "Sending :" + json);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        ApiInterface apiInterface = ApiClient.getClientWithAuth().create(ApiInterface.class);
        Call<CustomResponse> call = apiInterface.pushHeartBeatData(requestBody);

        call.enqueue(new Callback<CustomResponse>() {
            @Override
            public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                if (response.isSuccessful()) {
                    //tvDebug.setText(response.body().toString());
                    //SessionData newSessionLocal = new SessionData(newSession.distance, newSession.leaps, newSession.startTime, newSession.endtime);
                    //db.createSessionData(newSessionLocal);
                } else if (response.errorBody() != null) {
                    try {
                        JSONObject j = new JSONObject(response.errorBody().string());
                        String detail = j.get("detail").toString().trim();
                        if (detail.equalsIgnoreCase("Given token not valid for any token type")) {
                            //tvDebug.setText("Probable Token Timeout :" + response.raw().toString());
                            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

                            String json = new Gson().toJson(new CustomResponse(ApiClient.jwtRefreshToken));
                            //tvDebug.setText("Sending Json :" + json);
                            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
                            Call<CustomResponse> call2 = apiInterface.getAccessToken(requestBody);

                            call2.enqueue(new Callback<CustomResponse>() {
                                @Override
                                public void onResponse(Call<CustomResponse> call, Response<CustomResponse> response) {
                                    if (response.isSuccessful()) {
                                        ApiClient.jwtAccessToken = response.body().access;
                                    } else
                                        UiUtil.showAlertDialog(HeartRateMonitor.this, "Cannot Connect",
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
                    UiUtil.showAlertDialog(HeartRateMonitor.this, "Cannot Connect",
                            response.body().detail, "DISMISS", R.color.colorPrimary, R.color.colorAccent);
                }
            }

            @Override
            public void onFailure(Call<CustomResponse> call, Throwable t) {
                UiUtil.showErrorDialog(HeartRateMonitor.this, "Network Failure !", t.toString());
            }
        });
    }

}
