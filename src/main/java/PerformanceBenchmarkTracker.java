
import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.core.image.ConvertImage;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.struct.shapes.Quadrilateral_F64;
import boofcv.struct.image.ImageBase;

import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;

import boofcv.misc.BoofMiscOps;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Example of how to benchmark large components easily.
 *
 * @author Peter Abeles
 */
public class PerformanceBenchmarkTracker {

    public static void main(String[] args) {
        MediaManager media = DefaultMediaManager.INSTANCE;
        System.out.println("Circulant Tracker Benchmark Application");
        String fileName = "data/example/wildcat_robot.mjpeg";
        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        // specify the target's initial location and initialize with the first frame

        java.util.List<Quadrilateral_F64> locations = new ArrayList<>();

        // TODO add two  or others which are larger, smaller, and in slightly different locations
        locations.add( new Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0) );

        for( Quadrilateral_F64 location : locations ) {

            List<Quadrilateral_F64> history = new ArrayList<>();
            long totalVideo = 0;
            long totalRGB_GRAY = 0;
            long totalTracker = 0;

            int totalFaults = 0;
            int totalFrames = 0;

            GrayU8 gray = new GrayU8(1,1);


            TrackerObjectQuad<GrayU8> tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);
            SimpleImageSequence<Planar<GrayU8>> video = media.openVideo(fileName, ImageType.pl(3,GrayU8.class));
            Planar<GrayU8> rgb = video.next();
            gray.reshape(rgb.width,rgb.height);
            ConvertImage.average(rgb,gray);
            tracker.initialize(gray, location);

            while( true ) {
                long time0 = System.nanoTime();
                if( video.hasNext() ) {
                    rgb = video.next();
                } else {
                    break;
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
            video.close();

            double fps_Video = totalFrames/(totalVideo*1e-9);
            double fps_RGB_GRAY = totalFrames/(totalRGB_GRAY*1e-9);
            double fps_Tracker = totalFrames/(totalTracker*1e-9);
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
            // Output to a file.  One file for all results
            System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                    fps_Video,fps_RGB_GRAY,fps_Tracker,totalFaults);
            // The runtime performance file will be used to track performance and measure the merits of different
            // approaches
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



            // save history to a file.  One file for EACH trial

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
        }
    }
}