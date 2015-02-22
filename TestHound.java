import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class TestHound extends JFrame {
    BufferedImage img1, img2, img3, img4, img5, img6;
    boolean waterDetected, handsInWater;
    public static void main(String[] args) { //main
        try {
            String waterZoneDir = args[0];
            String waterLocationDir = args[1];
            String noSoapHandsDir = args[2];
            String testDir = args[3];
            try {
                TestHound bic = new TestHound(waterZoneDir, waterLocationDir, noSoapHandsDir, testDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java TestHound [/path/to/waterzone/files] [/path/to/waterlocation/files] [/path/to/nosoaphands/dir] [/path/to/test/dir]");

        }
    }

    public TestHound(String waterZoneDir, String waterLocationDir, String noSoapHandsDir, String testDir) {
        super("TestHound"); //create frame

        //run display
        setSize(1000, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        //read in and store water location image
        System.out.println("Loading Water Location Image...");
        //load background image
        String waterpath = waterLocationDir + "/waterDetector.data";
        double[][] expectedWaterLocation = Utility.DataFileToD2Arr(waterpath);
        System.out.println("Water Location image loaded.");

        // load water zone
        String wzFile = waterZoneDir + "/waterZone.csv";
        double[][] waterZone = Utility.transpose(Utility.readDepthImage(new File(wzFile), 240, 320));
        System.out.println("Water Zone image loaded.");

        // get mean patch
        ArrayList<File> handsFiles = Utility.getFileList(noSoapHandsDir, ".jpg", "img_");
        ArrayList<File> remappedHandsFiles = Utility.getFileList(noSoapHandsDir, ".csv", "remapped_segmentedHands_");
        ArrayList<ArrayList<Double>> remappedSegmentedHands = Utility.csvToArrayList(remappedHandsFiles.get((5)));
        //compute mean patch using the first (10th) RGB AND depth image in a no soap environment
        double[][] densityImage  = SoapDetector.getDensityImage(remappedSegmentedHands.get(1), remappedSegmentedHands.get(0));
        BufferedImage rgbImage  = Utility.loadImage(handsFiles.get(5));
        double[][][] rgbArray = Utility.bufferedImagetoArray3D(rgbImage);
        ArrayList<Patch> meanPatchList = SoapDetector.extractHandPatches(densityImage, rgbArray);
        rgbArray = SoapDetector.testExtractHandPatches(meanPatchList);
        rgbImage = Utility.array3DToBufferedImage(rgbArray);
        img4 = rgbImage;
        double [][][] meanPatchNoSoap = SoapDetector.extractMeanPatch(meanPatchList);

        runHound(meanPatchNoSoap, waterZone, expectedWaterLocation, testDir);

    }
    public void runHound(double[][][] meanPatchNoSoap, double[][] waterZone, double[][] expectedWaterLocation, String testDir) {
        img1 = Utility.array3DToBufferedImage(meanPatchNoSoap);
        img2 = Utility.d2ArrToBufferedImage(waterZone);
        img3 = Utility.d2ArrToBufferedImage(Utility.scale(expectedWaterLocation, 1000));

        ArrayList<File> testRGBFiles = Utility.getFileList(testDir, ".jpg", "img_");
        ArrayList<File> testSegmentedHands = Utility.getFileList(testDir, ".csv", "segmentedHands_");
        ArrayList<File> testRemappedSegmentedFiles = Utility.getFileList(testDir, ".csv", "remapped_segmentedHands_");

        int step = 2;
        int numFramesWaterDetected = 0;
        int numFramesHandsInWater = 0;
        for (int i = 0; i < testRGBFiles.size(); i++) {
            BufferedImage rgb = Utility.loadImage(testRGBFiles.get(i));
            double[][] handsDepthArray = Utility.transpose(Utility.readDepthImage(testSegmentedHands.get(i), 240, 320));
            waterDetected = TestWaterDetector.checkForWater(rgb, expectedWaterLocation);
            handsInWater = TestWaterZone.checkWaterZone(handsDepthArray, waterZone);

            ArrayList<ArrayList<Double>> coordinates = Utility.csvToArrayList(testRemappedSegmentedFiles.get(i));
            ArrayList<Double> x = coordinates.get(0);
            ArrayList<Double> y = coordinates.get(1);
            double[][][] newRGBImage = Utility.bufferedImagetoArray3D(Utility.loadImage(testRGBFiles.get(i)));
            double[][] soapDetectorArray = SoapDetector.soapDetectorImage(newRGBImage, y, x, meanPatchNoSoap);
            int[] clim = { -200000, 1000000};
            if (waterDetected && (step == 2 || step == 6)) {
                numFramesWaterDetected++;
            }
            if (handsInWater && (step == 3 || step == 7)) {
                numFramesHandsInWater++;
            }
            if (step == 2) {
                if (numFramesWaterDetected == 5) {
                    System.out.println("Step 2 completed");
                    step++;
                    numFramesWaterDetected = 0;
                }
            }
            if (step == 3 || step == 7) {
                if (numFramesHandsInWater >= 12 && waterDetected) {
                    System.out.println("Step " + step + " complete");
                    step++;
                    numFramesHandsInWater = 0;
                }
            }
            if (step == 4 || step == 8) {
                if (!waterDetected) {
                    numFramesWaterDetected--;
                    if (numFramesWaterDetected == -5) {
                        System.out.println("Step " + step + " complete");
                        step++;
                        numFramesWaterDetected = 0;
                    }
                }
            }
            if (step == 5) {
                //scrub hands with soap for 20 seconds/60 frames
                step++;
            }
            if (step == 6) {
                if (numFramesWaterDetected == 5) {
                    System.out.println("Step 6 completed");
                    step++;
                    numFramesWaterDetected = 0;
                }
            }
            img6 = Hist2D. drawHistogram(soapDetectorArray, clim);
            img5 = rgb;
            paintComponent(getGraphics());
            Utility.goToSleep();


        }
        if (step == 9) {
            System.out.println("You completed the hand washing process");
        } else {
            System.out.println("You got stuck on step " + step);
        }
    }
    public void paintComponent(Graphics g) {
        g.drawImage(img1, 0, 0, 320, 240, null);
        g.drawImage(img2, 330, 0, 320, 240, null);
        g.drawImage(img3, 0, 250, 320, 240, null);
        g.drawImage(img4, 330, 250, 320, 240, null);
        g.drawImage(img5, 0, 500, 320, 240, null);
        g.drawImage(img6, 330, 500, 320, 240, null);
        if (waterDetected) {
            Font myFont = new Font("SERIF", Font.BOLD, 25);
            g.setColor(Color.RED);
            g.drawString("WATER DETECTED", 0, 700);
        }
        if (handsInWater) {
            Font myFont = new Font("SERIF", Font.BOLD, 25);
            g.setColor(Color.GREEN);
            g.drawString("HANDS IN WATER ZONE", 500, 200);
        }
    }

}
