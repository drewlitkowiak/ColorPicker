package e.drewl.colorpicker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import static e.drewl.colorpicker.MainActivity.REQUEST_WRITE_STORAGE;

/**
 * This class should handle System Preferences, permissions,
 * etc. This is to remove clutter from MainActivity.
 */
class Settings extends AppCompatActivity{

    /** Tag for the Settings Class */
    private static final String TAG = "Settings";


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if External storage is available to read.
     * @return True if readable, false otherwise.
     */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * Makes and creates a file with the given parameter.
     * @param albumName the file name, followed by the type.
     *                  For example, "myFile.txt".
     * @return The file made.
     */
    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /** By default this value is false. */
    protected static boolean canWriteToPublicStorage = false;
    /** This is the thread that the StoragePermissions runs with. Be careful with this. */
    protected static AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Attempts to recieve permission from the user to write to the external storage.
     * This uses an asynchronous call to the os, but I suspend the current thread so
     * that the main method does not continue until I get a response back.
     * @param activity The "this" passed by the MainActivity.
     * @return True if permission to write to external memory was granted. False otherwise.
     */
    protected static boolean getStoragePermissions(Activity activity) {
        /*
         * Here we check for permission to write to external storage and request it if necessary.
         * Normally you would not want to do this on ever start, but we want to be persistent
         * since it makes development a lot easier.
         *
         * Sidenote: This is no longer enabled on start. It will only be called when the
         * camera button is pressed.
         */

        canWriteToPublicStorage = (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        Log.d(TAG, "Do we have permission to write to external storage: "
                + canWriteToPublicStorage);
        if (!canWriteToPublicStorage) {
            Log.d(TAG, "getStoragePermissions: Getting storage permissions... #1");
            //Asynchronous call for permissions. I have turned it into a synchronous call.
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
            Log.d(TAG, "getStoragePermissions: Should have gotten permissions by now.#5");
        }
        return canWriteToPublicStorage;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        //This deals with the external storage.
        Log.d(TAG, "onRequestPermissionsResult: Request has finished processing#2");
        if (requestCode == REQUEST_WRITE_STORAGE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    Log.d(TAG, "onRequestPermissionsResult: " + permissions[i] + "Denied.#3");
                    canWriteToPublicStorage = false;
                }
            }
            Log.d(TAG, "onRequestPermissionsResult: Permissions Granted.#3");
            canWriteToPublicStorage = true;
            Log.d(TAG, "onRequestPermissionsResult: Setting isRunning to false#4");
            isRunning.set(false);
        }
    }
}
