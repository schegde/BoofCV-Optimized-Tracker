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


import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.io.FileInputStream;
import java.nio.ByteBuffer;

import boofcv.struct.image.InterleavedU8;

import static android.content.ContentValues.TAG;



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


    public static void convert(Frame input , InterleavedU8 output , boolean swapOrder ) {
        output.setNumBands(input.imageChannels);
        output.reshape(input.imageWidth,input.imageHeight);

        int N = output.width*output.height*output.numBands;

        ByteBuffer buffer = (ByteBuffer)input.image[0];
        if( buffer.limit() != N ) {
            throw new IllegalArgumentException("Unexpected buffer size. "+buffer.limit()+" vs "+N);
        }

        buffer.position(0);
        buffer.get(output.data,0,N);

        if( input.imageChannels == 3 && swapOrder ) {
            swapRgbBands(output.data,output.width,output.height,output.numBands);
        }
    }


    public static void swapRgbBands( byte []data, int width , int height , int numBands ) {

        int N = width*height*numBands;

        if( numBands == 3  ) {
            for (int i = 0; i < N; i+=3) {
                int k = i+2;

                byte r = data[i];
                data[i] = data[k];
                data[k] = r;
            }
        } else {
            throw new IllegalArgumentException("Support more bands");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(SrcPath);
        CanvasFrame canvasFrame = new CanvasFrame("Test");
        try
        {
            grabber.start();
            long time_vid = grabber.getLengthInTime();
            Log.d("[TIME_VID]", "Time is "+ time_vid);
            Frame frame;
            long counter = 0;
            for(long i = 0; i<grabber.getLengthInFrames(); i++)
            {
                counter++;
                frame = grabber.grabImage();
                InterleavedU8 interleaved = new InterleavedU8();
                convert(frame, interleaved, true);
            }
            Log.d("[FRAMES]", "Frames = "+ counter);
        }catch (Exception exception)
        {
            Log.e("1", "Grabber Exception");
        }

//        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//        try {
//            FileInputStream is = new FileInputStream(SrcPath);
//            mediaMetadataRetriever.setDataSource(is.getFD());
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//
//        String METADATA_KEY_DURATION = mediaMetadataRetriever
//                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        Bitmap bmpOriginal = mediaMetadataRetriever.getFrameAtTime(0);
//        int bmpVideoHeight = bmpOriginal.getHeight();
//        int bmpVideoWidth = bmpOriginal.getWidth();
//
//        Log.d("LOGTAG", "bmpVideoWidth:'" + bmpVideoWidth + "'  bmpVideoHeight:'" + bmpVideoHeight + "'");
//        //while(true);
    }

}
