
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

import java.util.ArrayList;
import java.util.List;

/**
 * Example of how to benchmark large components easily.
 *
 * @author Peter Abeles
 */
public class PerformanceBenchmarkTracker {

    public static void main(String[] args) {
        MediaManager media = DefaultMediaManager.INSTANCE;
        String fileName = "data/example/wildcat_robot.mjpeg";

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

            // TODO Output to a file.  One file for all results
            System.out.printf("Summary: video %6.1f RGB_GRAY %6.1f Tracker %6.1f  Faults %d\n",
                    fps_Video,fps_RGB_GRAY,fps_Tracker,totalFaults);
            // The runtime performance file will be used to track performance and measure the merits of different
            // approaches

            // TODO save history to a file.  One file for EACH trial
        }

        // TODO write application which will read the history file and visualize the results.
        // Have it be a desktop application but doesn't need to be in java.  Just needs to run in Ubuntu and windows
        // would also be nice.  This will be used to sanity the tracker

        // Visualization is used as a sanity check.  Did the track diverge? How different are the results?  Takes
        // a little bit of effort but can save a lot of hassle down the road

    }
}