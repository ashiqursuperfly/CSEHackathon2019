package ashiqur.goriberfitbit.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class FileUtil {

    /**
     * TODO: 1. add apache POI lib jar files inside app->libs folder
     * Download From : https://github.com/andruhon/android5xlsx
     * In build.gradle's app module :
     * TODO: 2.Inside android{} tag:
     *  packagingOptions {
     *         exclude 'META-INF/DEPENDENCIES'
     *         exclude 'META-INF/NOTICE'
     *         exclude 'META-INF/NOTICE.txt'
     *         exclude 'META-INF/LICENSE'
     *         exclude 'META-INF/LICENSE.txt'
     *     }
     *  TODO: 3.Inside defaultconfig{} Tag
     *       multiDexEnabled true
     *  TODO: 4.Inside dependencies{} Tag :
     *     implementation fileTree(dir: 'libs', include: ['*.jar'])
     *     implementation 'com.android.support:multidex:1.0.3'
     * */

    final static String TAG = "ASHIQUR-FILEUTIL";
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String projectName;
    private String path ;

    /**@param projectName creates a directory with the project's name in the device's
     * local storage, writes files that this app needs to store, in this directory*/
    public FileUtil(String projectName) {
        this.projectName = projectName;
        this.path =Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ projectName;
    }

    //= Environment.getExternalStorageDirectory().getAbsolutePath() +"/Lady";

    public String readTxtFile(String fileName){
        String line = null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path+"/"+fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line).append(System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            bufferedReader.close();
        } catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return line;
    }

    public boolean appendToTxtFile(String data, String fileName){
        try {
            new File(path).mkdir();
            File file = new File(path+"/"+fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write((data + System.getProperty("line.separator")).getBytes());

            return true;
        } catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
        return  false;


    }

    @TargetApi(27)
    protected static void askPermissions(Activity activity) {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        activity.requestPermissions(permissions, requestCode);
    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permissionWrite = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        int permissionRead = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionRead != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public boolean delete(File file) {
        try{
            if(file.delete())
            {
                System.out.println("File deleted successfully");
                return true;
            }
            else
            {
                System.out.println("Failed to delete the file");
                return false;
            }
        }
        catch (Exception e){
            System.out.println(e);
            return false;
        }
    }

    private boolean createDirIfNotExists()
    {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), projectName);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.wtf(TAG, "Problem creating folder");
                ret = false;
            }
        }
        return ret;
    }
}
