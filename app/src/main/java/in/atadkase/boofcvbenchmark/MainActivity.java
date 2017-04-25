package in.atadkase.boofcvbenchmark;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.ActivityCompat;

import com.getkeepsafe.relinker.ReLinker;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.FileInputStream;

import static android.content.ContentValues.TAG;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;


public class MainActivity extends Activity {

    String SrcPath="/storage/emulated/0/imag/file3.mp4";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (permission!= PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "verifyStoragePermissions: ");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        try {
            FileInputStream is = new FileInputStream(SrcPath);
            mediaMetadataRetriever.setDataSource(is.getFD());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        String METADATA_KEY_DURATION = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Bitmap bmpOriginal = mediaMetadataRetriever.getFrameAtTime(0);
        int bmpVideoHeight = bmpOriginal.getHeight();
        int bmpVideoWidth = bmpOriginal.getWidth();

        Log.d("LOGTAG", "bmpVideoWidth:'" + bmpVideoWidth + "'  bmpVideoHeight:'" + bmpVideoHeight + "'");
        //while(true);
    }

}
