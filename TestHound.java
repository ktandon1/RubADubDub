import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
import java.text.*;



public class TestHound extends JFrame {
    public static final double frameRate = 4;
    public static final int wetHandsTime = 4 * (int) frameRate;
    public static final int soapTime = 20 * (int) frameRate;
    public static final int faucetOnTime = (int) frameRate / 2;
    public static final int waterOffTime = (int) frameRate / 2;
    public static final int soapThreshold = 700;

    BufferedImage img, img1, img2, img3, img4, img5, img6;
    boolean waterDetected, handsInWater, soapDetected;
    int step, numFramesWaterDetected, numFramesHandsInWater, numFramesSoap, numFramesWaterOff;
    double score;
    double[][] soapDetectorArray;
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
        setSize(1280, 800);
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
    public void runHound(final double[][][] meanPatchNoSoap, final double[][] waterZone, final double[][] expectedWaterLocation, String testDir) {
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
        double[][][] rgbArr = new double[720][1280][3];
        for (int i = 0; i < testRGBFiles.size() - 3; i++) {
            long startTime = System.currentTimeMillis();

            BufferedImage rgb = Utility.loadImage(testRGBFiles.get(i));
            final double[][] handsDepthArray = Utility.transpose(Utility.readDepthImage(testSegmentedHands.get(i), 240, 320));
            final double[][][] newRGBImage = Utility.bufferedImagetoArray3D(rgb, rgbArr);
            final ArrayList<ArrayList<Double>> coordinates = Utility.csvToArrayList(testRemappedSegmentedFiles.get(i));
            final ArrayList<Double> x = coordinates.get(0);
            final ArrayList<Double> y = coordinates.get(1);

            Thread t1 = new Thread(new Runnable() {
                public void run() {
                    waterDetected = TestWaterDetector.checkForWater(newRGBImage, expectedWaterLocation);
                }
            });
            Thread t2 = new Thread(new Runnable() {
                public void run() {
                    handsInWater = TestWaterZone.checkWaterZone(handsDepthArray, waterZone);
                }
            });
            Thread t3 = new Thread(new Runnable() {
                public void run() {
                    soapDetectorArray = SoapDetector.soapDetectorImage(newRGBImage, y, x, meanPatchNoSoap);
                    //soapDetectorArray = new double[10][10];
                }
            });

            t1.start();
            t2.start();
            t3.start();
            try {
                t1.join();
                t2.join();
                t3.join();
            } catch (Exception e) {

            }
            int[] clim = { 00000, 300000};
            score = soapScore(soapDetectorArray);
            if (score > soapThreshold) {
                soapDetected = true;
            } else {
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
                if (soapDetected) {
                    numFramesSoap++;
                    if (numFramesSoap == soapTime) {
                        step++;
                        System.out.println("You haves scrubbed your hands with soap. Please turn on water.");
                    }
                }

            }
            img6 = Hist2D.drawHistogram(soapDetectorArray, clim);
            img5 = rgb;
            int[] clim2 = {0, 1200};
            img = Utility.d2ArrToBufferedImage(handsDepthArray, clim2);
            final int frameIndex = i;
            paintComponent(getGraphics(), frameIndex); // can't do threading without breaking things

            System.out.println(System.currentTimeMillis() - startTime);


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

        for (int x = 0; x < hist.length; x++) {
            for (int y = 0; y < hist[x].length; y++) {
                sum += hist[x][y];
                total++;
            }
        }
        return sum / total;
    }
    public void paintComponent(Graphics inputG, int frameNum) {

        Graphics2D g2 = (Graphics2D) inputG;
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                           RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                           RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                           RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING,
                           RenderingHints.VALUE_DITHER_DISABLE);
        BufferedImage j = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = j.createGraphics();

        // g.setColor(Color.WHITE);
        // g.fillRect(0, 0, 900, 500);
        // g.clearRect(0, 0, 900, 500);
        String currentStep = "";
        String progressString  = "";
        String soap = "";
        String water = "";
        String hands = "";
        DecimalFormat df = new DecimalFormat("####");

        g.drawImage(img, 960, 50, 320, 240, null);
        g.drawImage(img5, 0, 50, 960, 540, null);
        g.drawImage(img6, 960, 300, 320, 180, null);


        water = "Water Detected: " + waterDetected;
        hands = "Hands in Water Zone: " + handsInWater;
        soap = "Soap: " + soapDetected + ", Score: " + df.format(score);

        if (step == 3 || step == 7) {
            currentStep = "Put your hands into water for 4 seconds";
            progressString = "Time Detected: " + numFramesHandsInWater / frameRate + "/" + wetHandsTime / frameRate + " seconds";
        } else if (step == 4 || step == 8) {
            currentStep = "Turn water off";
            progressString = "Time Detected: "  +  numFramesWaterOff / frameRate + "/" + waterOffTime / frameRate + " seconds";
        } else if (step == 5) {
            currentStep = "Scrub your hands with soap";
            progressString = "Time Detected: " + numFramesSoap / frameRate + "/" + soapTime / frameRate + " seconds";
        } else if (step == 2 || step == 6) {
            currentStep = "Turn on water";
            progressString = "Time Detected: " + numFramesWaterDetected / frameRate + "/" + faucetOnTime / frameRate + " seconds";
        } else if (step == 9) {
            currentStep = "Success!";
            progressString = "You have followed the CDC Protocol for handwashing!";
        }
        Font myFont = new Font("SANS_SERIF", Font.BOLD, 35);
        g.setFont(myFont);
        g.setColor(Color.WHITE);
        currentStep = "Step " + (step - 1) + ": " + currentStep;
        g.drawString(currentStep, 120, 625);
        g.drawString(progressString, 120, 675);
        Font myFont2 = new Font("SANS_SERIF", Font.BOLD, 24);
        g.setFont(myFont2);
        if (score < 400) {
            g.setColor(Color.RED);
        } else if (score < soapThreshold) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.GREEN);
        }
        g.drawString(soap, 960, 500);
        if (handsInWater) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawString(hands, 960, 550);
        if (waterDetected) {
            g.setColor(Color.GREEN);
        } else {
            g.setColor(Color.RED);
        }
        g.drawString(water, 960, 600);


        g2.drawImage(j, 0, 0, null);

        // String temp = "video/" + frameNum + ".png";
        //Utility.writeImage(j, temp);
    }

}
