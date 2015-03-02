import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class TestHound extends JFrame {
    public static final double frameRate = 4;
    public static final int wetHandsTime = 4 * (int) frameRate;
    public static final int soapTime = 20 * (int) frameRate;
    public static final int faucetOnTime = (int) frameRate/2;
    public static final int waterOffTime = (int) frameRate/2;

    BufferedImage img1, img2, img3, img4, img5, img6;
    boolean waterDetected, handsInWater, soapDetected;
    int step, numFramesWaterDetected,numFramesHandsInWater,numFramesSoap,numFramesWaterOff;
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

        step = 2;
        numFramesWaterDetected = 0;
        numFramesHandsInWater = 0;
        numFramesSoap = 0;
        numFramesWaterOff = 0;
        for (int i = 0; i < testRGBFiles.size()-3; i++) {
            BufferedImage rgb = Utility.loadImage(testRGBFiles.get(i));
            double[][] handsDepthArray = Utility.transpose(Utility.readDepthImage(testSegmentedHands.get(i), 240, 320));
            waterDetected = TestWaterDetector.checkForWater(rgb, expectedWaterLocation);
            handsInWater = TestWaterZone.checkWaterZone(handsDepthArray, waterZone);

            ArrayList<ArrayList<Double>> coordinates = Utility.csvToArrayList(testRemappedSegmentedFiles.get(i));
            ArrayList<Double> x = coordinates.get(0);
            ArrayList<Double> y = coordinates.get(1);
            double[][][] newRGBImage = Utility.bufferedImagetoArray3D(Utility.loadImage(testRGBFiles.get(i)));
            double[][] soapDetectorArray = SoapDetector.soapDetectorImage(newRGBImage, y, x, meanPatchNoSoap);
            int[] clim = { 00000, 300000};
            double score = soapScore(soapDetectorArray);
            if(score > 394)
            {
                soapDetected = true;
            }
            else
            {
                soapDetected = false;
            }
            System.out.println("SoapScore " + score + ", " + soapDetected);
            if (waterDetected && (step == 2 || step == 6)) {
                numFramesWaterDetected++;
            }
            if (handsInWater && waterDetected && (step == 3 || step == 7)) {
                numFramesHandsInWater++;
            }
            if (step == 2 || step == 6) {
                if (numFramesWaterDetected == faucetOnTime) {
                    System.out.println("Step " + step + " completed. You have turned on water. Please put your hands under the water now.");
                    step++;
                    numFramesWaterDetected = 0;
                }
            }
            if (step == 3 || step == 7) {
                if (numFramesHandsInWater >= wetHandsTime && waterDetected) {
                    System.out.println("Step " + step + " complete. You have put your hands under the water. Please turn off the water.");
                    step++;
                    numFramesHandsInWater = 0;
                }
            }
            if (step == 4 || step == 8) {
                if (!waterDetected) {
                    numFramesWaterOff++;
                    if (numFramesWaterOff == waterOffTime) {
                        if (step == 4) {
                            System.out.println("Step " + step + " complete. You have turned off water. Please scrub your hands with soap. ");
                        } else {
                            System.out.println("You have successfully washed your hands");
                        }
                        step++;
                        numFramesWaterOff = 0;
                    }
                }
            }
            if (step == 5) {
                //scrub hands with soap for 20 seconds/60 frames
                    if(soapDetected)
                    {
                        numFramesSoap++;
                        if(numFramesSoap == soapTime)
                        {             
                            step++;
                            System.out.println("You haves scrubbed your hands with soap. Please turn on water.");
                        }
                    }
                
            }
            img6 = Hist2D.drawHistogram(soapDetectorArray, clim);
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
    public double soapScore(double[][] hist) {        
        double sum = 0;
        double total = 0;

        for(int x = 0; x<hist.length; x++)
        {
            for(int y = 0; y<hist[x].length; y++)
            {
                sum += hist[x][y];
                total++;
            }
        }
        return sum / total;
    }
    public void paintComponent(Graphics g) {
        String currentStep = "";
        String progressString  = "";
        g.drawImage(img4, 0, 0, 320, 240, null);
        g.drawImage(img5, 0, 250, 320, 240, null);
        g.drawImage(img6, 330, 500, 320, 240, null);
        System.out.println("waterDetected = " + waterDetected);
        Font myFont = new Font("SERIF", Font.BOLD, 25);
        if (waterDetected) {
            g.setColor(Color.RED);
            g.drawString("WATER DETECTED", 0, 0);
        }
        System.out.println("handsInWater = " + handsInWater);
        if (handsInWater) {
            System.out.println("Hands in Water Zone");
        }

        myFont = new Font("SERIF", Font.BOLD, 75);
        g.setColor(Color.WHITE);
        g.drawString("Step " + step, 25, 100);
        if(step == 3 || step == 7)
        {
            currentStep = "Put your hands into water for 4 seconds";
            progressString = numFramesHandsInWater/frameRate + "/" + wetHandsTime/frameRate + " seconds";
        }
        else if(step == 4 || step == 8)
        {
            currentStep = "Turn water off";
            progressString = numFramesWaterOff/frameRate + "/" + waterOffTime/frameRate + " seconds";
        }
        else if(step == 5)
        {
            currentStep = "Now scrub your hands with soap";
            progressString = numFramesSoap/frameRate + "/" + soapTime/frameRate + " seconds";
        }
        else if(step == 2 || step == 6)
        {
            currentStep = "Turn on water";
            progressString = numFramesWaterDetected/frameRate + "/" + faucetOnTime/frameRate + " seconds";
        }
        else if(step == 9)
        {
            currentStep = "Success!";
            progressString = "You have washed your hands!";
        }
        g.drawString(currentStep, 100,100);
        g.drawString(progressString,100, 150);
    }

}
