package in.atadkase.boofcvbenchmark;

import android.Manifest;
import android.app.Activity;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.ActivityCompat;
import android.graphics.Color;
import static android.content.ContentValues.TAG;


import java.util.List;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.FileInputStream;

import boofcv.struct.image.GrayU8;
import georegression.struct.shapes.Quadrilateral_F64;
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.core.image.ConvertImage;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.struct.image.Planar;







public class MainActivity extends Activity {

    String SrcPath="/storage/1D08-311A/video.mp4";

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
        } catch (Exception e) {
            e.printStackTrace();
        }


        String playback_duration_ms = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);


        System.out.print("The frame rate is::");
        System.out.println(playback_duration_ms);

       long frameperiod_us = (1000000/30);
//
       long playback_duration_us = frameperiod_us*20; //Process 20 frames!!
               //(Long.parseLong(playback_duration_ms))*1000 ;

//
        GrayU8 gray = new GrayU8(1,1);

        Quadrilateral_F64 location = new Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0);
        TrackerObjectQuad<GrayU8> tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);

        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        List<Quadrilateral_F64> history = new ArrayList<>();

        long totalVideo=0;
        long totalRGB_GRAY = 0;
        long totalTracker = 0;

        int totalFaults = 0;
        int totalFrames = 0;

        //java.util.List<Quadrilateral_F64> locations = new ArrayList<>();
        //locations.add( new Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0) );


        //Read the first frame and initialize the tracker
            Bitmap bmpOriginal = mediaMetadataRetriever.getFrameAtTime(0);
            int bmpVideoHeight = bmpOriginal.getHeight();
            int bmpVideoWidth = bmpOriginal.getWidth();
            Planar<GrayU8> rgb = new Planar(GrayU8.class, bmpVideoWidth, bmpVideoHeight, 3);
            int colour;

            for (int j = 0; j < bmpVideoHeight; j++){
                for (int i = 0; i < bmpVideoWidth; i++) {

                    colour = bmpOriginal.getPixel(i, j);

                    int red = Color.red(colour);
                    int blue = Color.blue(colour);
                    int green = Color.green(colour);


                    rgb.getBand(0).set(i, j, red);
                    rgb.getBand(1).set(i, j, green);
                    rgb.getBand(2).set(i, j, blue);


                }
            }

            gray.reshape(rgb.width,rgb.height);
            ConvertImage.average(rgb,gray);

            tracker.initialize(gray, location);

            //Read the remaining frames till the end!

        for(long time_count=frameperiod_us;time_count<playback_duration_us;time_count+=frameperiod_us){

            long time0 = System.nanoTime(); //Start timer!

            //Read a frame!!

            bmpOriginal = mediaMetadataRetriever.getFrameAtTime(time_count);
            for (int j = 0; j < bmpVideoHeight; j++){
                for (int i = 0; i < bmpVideoWidth; i++) {

                    colour = bmpOriginal.getPixel(i, j);

                    int red = Color.red(colour);
                    int blue = Color.blue(colour);
                    int green = Color.green(colour);


                    rgb.getBand(0).set(i, j, red);
                    rgb.getBand(1).set(i, j, green);
                    rgb.getBand(2).set(i, j, blue);


                }
            }

            long time1 = System.nanoTime();
            ConvertImage.average(rgb,gray);
            long time2 = System.nanoTime();

            boolean visible = tracker.process(gray, location);
            long time3 = System.nanoTime();
            history.add( location.copy() );

            totalVideo += time1-time0;
            totalRGB_GRAY += time2-time1;
            totalTracker += time3-time2;

            totalFrames++;
            if( !visible )
                totalFaults++;
        }

        mediaMetadataRetriever.release();
        //**************************************************************************
        //**************************************************************************
        //**************************************************************************
        //**************************************************************************
        //Done with processing, now write the summary file!.

        System.out.println("Finished the processing!!!!!");

        double fps_Video = totalFrames/(totalVideo*1e-9);
        double fps_RGB_GRAY = totalFrames/(totalRGB_GRAY*1e-9);
        double fps_Tracker = totalFrames/(totalTracker*1e-9);
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
        System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                fps_Video,fps_RGB_GRAY,fps_Tracker,totalFaults);

        BufferedWriter out = null;
        try
        {
            FileWriter fstream = new FileWriter("summary.txt", true);   // append to file
            out = new BufferedWriter(fstream);
            String summaryString = timeStamp+ " Video: "+ numberFormat.format(fps_Video)
                    +" RGB_GRAY: "+numberFormat.format(fps_RGB_GRAY)+ " Tracker: "
                    + numberFormat.format(fps_Tracker)+ " Faults: "+
                    totalFaults+"\n";
            out.write(summaryString);
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if(out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {/*ignore*/}
            }

        }


       //**************************************************************************
        //**************************************************************************
        //**************************************************************************
        //**************************************************************************
        //Save history to a file!!!
        try
        {
            FileWriter fstream = new FileWriter("history."+timeStamp+".txt", true);   // append to file
            out = new BufferedWriter(fstream);
            for( Quadrilateral_F64 history_loc : history ) {
                out.write("a:"+history_loc.a.x+" "+history_loc.a.y+"\n"+
                        "b:"+history_loc.b.x+" "+history_loc.b.y+"\n"+
                        "c:"+history_loc.c.x+" "+history_loc.c.y+"\n "+
                        "d:"+history_loc.d.x+" "+history_loc.d.y+"\n");
            }
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            if(out != null) {
                try {
                    out.close();
                }
                catch (Exception ex) {/*ignore*/}
            }

        }


        Log.d("LOGTAG", "bmpVideoWidth:'" + bmpVideoWidth + "'  bmpVideoHeight:'" + bmpVideoHeight + "'");

    }

}
