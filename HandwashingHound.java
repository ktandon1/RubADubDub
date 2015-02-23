import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class HandwashingHound extends JFrame {
    BufferedImage img1, img2, img3, img4, img5, img6;
    boolean waterDetected, handsInWater;
    public static void main(String[] args) { //main
        try {
            String waterZoneDir = args[0];
            String waterLocationDir = args[1];
            String noSoapHandsDir = args[2];
            String testDir = args[3];
            int expectedWaterDetectionScore = Integer.parseInt(args[4]);
            int expectedHandLocationScore = Integer.parseInt(args[5]);
            int expectedSoapScore = Integer.parseInt(args[6]);

            try {
                HandwashingHound bic = new HandwashingHound(waterZoneDir, waterLocationDir, noSoapHandsDir, testDir, expectedWaterDetectionScore, expectedHandLocationScore, expectedSoapScore);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java HandwashingHound [/path/to/waterzone/files] [/path/to/waterlocation/files] [/path/to/nosoaphands/dir] [/path/to/test/dir] [expected water detection score] [expected hand location score] [expected soap detection score]");

        }
    }

    public HandwashingHound(String waterZoneDir, String waterLocationDir, String noSoapHandsDir, String testDir, int expectedWaterDetectionScore, int expectedHandLocationScore, int expectedSoapScore) {
        super("HandwashingHound"); //create frame

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

        runHound(meanPatchNoSoap, waterZone, expectedWaterLocation, testDir, expectedWaterDetectionScore, expectedWaterDetectionScore, expectedSoapScore);

    }
    public void runHound(double[][][] meanPatchNoSoap, double[][] waterZone, double[][] expectedWaterLocation, String testDir, int expectedWaterDetectionScore, int expectedHandLocationScore, int expectedSoapScore) {
        img1 = Utility.array3DToBufferedImage(meanPatchNoSoap);
        img2 = Utility.d2ArrToBufferedImage(waterZone);
        img3 = Utility.d2ArrToBufferedImage(Utility.scale(expectedWaterLocation, 1000));

        ArrayList<File> testRGBFiles = Utility.getFileList(testDir, ".jpg", "img_");
        ArrayList<File> testSegmentedHands = Utility.getFileList(testDir, ".csv", "segmentedHands_");
        ArrayList<File> testRemappedSegmentedFiles = Utility.getFileList(testDir, ".csv", "remapped_segmentedHands_");
        int NUM_SAMPLES = testRGBFiles.size() - 2;
        double[][] results = new double[NUM_SAMPLES][7];
        for (int i = 0; i < testRGBFiles.size() - 2; i++) {
        //for (int i = 0; i < 2; i++) {
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

            results[i][0] = i + 1;
            if (waterDetected) {
                results[i][1] = 1;
            } else {
                results[i][1] = 0;
            }
            if (handsInWater) {
                results[i][2] = 1;
            } else {
                results[i][2] = 0;
            }
            results[i][3] = soapScore(soapDetectorArray);
            results[i][4] = expectedWaterDetectionScore;
            results[i][5] = expectedHandLocationScore;
            results[i][6] = expectedSoapScore;
            System.out.println(results[i][0] + " " + results[i][1] + " " + results[i][2] + " " + results[i][3] + " " );
            
            img6 = Hist2D.drawHistogram(soapDetectorArray, clim);
            img5 = rgb;
            paintComponent(getGraphics());
            Utility.goToSleep();


        }
        File tDir = new File(testDir);
        String tDirName = tDir.getName(); 
        //String fileName = testDir.replace('\\','_').replace('/', '_') + ".txt";
        String fileName = tDirName + ".txt";
        Utility.d2ArrToCSV(results, fileName, "Frame_Num,Water_Detected,Hands_in_Water,Soap_Score,Expected_Water_Detection,Expected_Water_Location,Expected_Soap_Score");
        System.out.println(fileName + " created");
        System.exit(0);


    }
    public double soapScore(double[][] hist) {
        double max = hist[0][0];
        for(int x = 0; x<hist.length; x++)
        {
        	for(int y = 0; y<hist[x].length; y++)
        	{
        		if(max < hist[x][y])
        		{
        			max = hist[x][y];
        		}
        	}
        }
        return max;
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
