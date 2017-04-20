
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



            // TODO save history to a file.  One file for EACH trial

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


        TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
        SimpleImageSequence video_file = media.openVideo(fileName, ImageType.pl(3,GrayU8.class)); 
        Quadrilateral_F64 loc = new Quadrilateral_F64(0,0,0,0,0,0,0,0);

        System.out.print("Starting the visualization,enter the filename just created:!!");

        Scanner scanner = new Scanner(System.in);
        String history_file = scanner.next();

        try{

        FileReader input = new FileReader(history_file);
        BufferedReader bufRead = new BufferedReader(input);
        String myLine = null;


        //Read first frame quad positions!.
          
                myLine = bufRead.readLine(); //read A
                String[] array1 = myLine.split(":");
                String[] array2 = array1[1].split(" ");
                loc.a.x = Double.parseDouble(array2[0]);
                loc.a.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read B
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.b.x = Double.parseDouble(array2[0]);
                loc.b.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read C
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.c.x = Double.parseDouble(array2[0]);
                loc.c.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read D
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.d.x = Double.parseDouble(array2[0]);
                loc.d.y = Double.parseDouble(array2[1]);

           


        ImageBase frame = video_file.next(); //Get the first frame
        
        // For displaying the results
        //TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
        gui.setPreferredSize(new Dimension(frame.getWidth(),frame.getHeight()));
        gui.setBackGround((BufferedImage)video_file.getGuiImage());
        gui.setTarget(loc,true);
        ShowImages.showWindow(gui,"Tracking Results", true);

        // Track the object across each video frame and display the results
        long previous = 0;
        while( video_file.hasNext() ) {
            frame = video_file.next();

            //boolean visible = tracker.process(frame,location);

            gui.setBackGround((BufferedImage) video_file.getGuiImage());

            if((myLine = bufRead.readLine())!=null)
            {     
                //myLine = bufRead.readLine(); //read A
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.a.x = Double.parseDouble(array2[0]);
                loc.a.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read B
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.b.x = Double.parseDouble(array2[0]);
                loc.b.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read C
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.c.x = Double.parseDouble(array2[0]);
                loc.c.y = Double.parseDouble(array2[1]);

                myLine = bufRead.readLine(); //read D
                array1 = myLine.split(":");
                array2 = array1[1].split(" ");
                loc.d.x = Double.parseDouble(array2[0]);
                loc.d.y = Double.parseDouble(array2[1]);
            }

            gui.setTarget(loc, true);
            gui.repaint();

            // shoot for a specific frame rate
            long time = System.currentTimeMillis();
            BoofMiscOps.pause(Math.max(0,80-(time-previous)));
            previous = time;
        }
    }
    catch (IOException e)
            {
                System.err.println("Error: " + e.getMessage());
            }


        scanner.close();



        
          
       



        // TODO write application which will read the history file and visualize the results.
        // Have it be a desktop application but doesn't need to be in java.  Just needs to run in Ubuntu and windows
        // would also be nice.  This will be used to sanity the tracker

        // Visualization is used as a sanity check.  Did the track diverge? How different are the results?  Takes
        // a little bit of effort but can save a lot of hassle down the road

    }
}