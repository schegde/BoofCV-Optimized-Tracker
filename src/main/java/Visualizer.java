/**
 * Created by ashu on 4/20/17.
 */

import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageType;
import georegression.struct.shapes.Quadrilateral_F64;
import boofcv.struct.image.ImageBase;

import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;

import boofcv.misc.BoofMiscOps;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.awt.image.BufferedImage;
import java.awt.*;


public class Visualizer {
    public static void main(String[] args) {
        MediaManager media = DefaultMediaManager.INSTANCE;
        TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
        String fileName = "data/example/wildcat_robot.mjpeg";
        SimpleImageSequence video_file = media.openVideo(fileName, ImageType.pl(3, GrayU8.class));
        Quadrilateral_F64 loc = new Quadrilateral_F64(0, 0, 0, 0, 0, 0, 0, 0);
        System.out.printf("Enter the history file location: ");

        Scanner scanner = new Scanner(System.in);
        String history_file = scanner.next();

        try {

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
            gui.setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
            gui.setBackGround((BufferedImage) video_file.getGuiImage());
            gui.setTarget(loc, true);
            ShowImages.showWindow(gui, "Tracking Results", true);

            // Track the object across each video frame and display the results
            long previous = 0;
            while (video_file.hasNext()) {
                frame = video_file.next();

                //boolean visible = tracker.process(frame,location);

                gui.setBackGround((BufferedImage) video_file.getGuiImage());

                if ((myLine = bufRead.readLine()) != null) {
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
                BoofMiscOps.pause(Math.max(0, 80 - (time - previous)));
                previous = time;
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        scanner.close();
    }
}
