import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class SegmentHands extends JFrame {
    //consts


    //display
    protected BufferedImage img2;

    public static void main(String[] args) { //main
        try {
            String backgroundDir = args[0];
            String handsDir = args[1];
            int displayResult = Integer.parseInt(args[2]);
            if (displayResult != 0 && displayResult != 1) {
                throw new Exception("Bad Input");
            }
            try {
                SegmentHands bic = new SegmentHands(backgroundDir, handsDir, displayResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java SegmentHands [/path/to/background/files] [/path/to/hands/dir] [0/1 where 1=display result]");
        }
    }

    public SegmentHands(String backgroundDir, String handsDir, int displayResult) {
        super("Image Displayer"); //create frame

        //display if needed
        if (displayResult == 1) {

            //run display
            setSize(330, 250);
            setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
            setResizable(true);
            setVisible(true);//frame visible

            //algorithm
            segmentation(backgroundDir, handsDir, displayResult);

        } else {

            //algorithm
            segmentation(backgroundDir, handsDir, displayResult);

            //quit
            System.exit(0);
        }
    }

    protected void segmentation(String backgroundDir, String handsDir, int displayResult) {

        //read in and store background image
        System.out.println("Loading Background Image...");
        String bgFile = backgroundDir + "/background.csv";
        double[][] backgroundImage = Utility.transpose(Utility.readDepthImage(new File(bgFile), 240, 320));
        System.out.println("Background image loaded.");

        //for each file in hands dir, do background subtraction and store result.
        System.out.println("Processing " + handsDir + " ...");
        ArrayList<File> handsFiles = Utility.getFileList(handsDir, ".csv", "rawdepth_");
        for (int i = 0; i < handsFiles.size(); i++) {


            //load file
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = handsDir + "/" + fileString;
            double[][] handsImage = Utility.readDepthImage(handsFiles.get(i));
            System.out.println(handsFiles.get(i).getName());
            //subtract background
            double[][] hands = Utility.subtractBackground(backgroundImage, handsImage);

            //write hands
            Utility.d2ArrToCSV(hands, filePath);

            //display if needed
            if (displayResult == 1) {
                int[] clim = {0, 1200};
                img2 = Utility.d2ArrToBufferedImage(hands,clim);
                repaint();
                Utility.goToSleep();
            }
        }
        System.out.println("...");
        System.out.println("Hand segmentations successfully written.");
    }

    public void paint(Graphics g) {
        g.drawImage(img2, 0, 0, 320, 240, null);
    }
}
