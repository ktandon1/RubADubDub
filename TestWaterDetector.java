import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class TestWaterDetector extends JFrame {

    //consts

    //global vars
    private static String handsDir, waterDir;
    private double[][] expectedWaterLocation;
    private static BufferedImage threshold, histImg, img;
    public static void main(String[] args) {
        try {
            handsDir = args[1];
            waterDir = args[0];
            int displayResult = Integer.parseInt(args[2]);
            if (displayResult != 0 && displayResult != 1) {
                throw new Exception("Bad Input");
            }
            try {
                new TestWaterDetector(handsDir, displayResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java TestWaterDetector [/path/to/trained/water/detector] [/path/to/rgb/images] [0/1 where 1=display result]");
        }
    }

    public TestWaterDetector(String handsDir, int displayResult) {
        super("Water Detector"); //init
        //display if needed, else exit.
        if (displayResult == 1) {
            setSize(1000, 1000);
            setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
            setResizable(true);
            setVisible(true);//frame visible

            loadRGB();

            repaint();
        } else {
            System.exit(0);
        }
    }
    public void loadRGB() {
        String waterpath = waterDir + "/waterDetector.data";
        expectedWaterLocation = Utility.DataFileToD2Arr(waterpath);
        System.out.println(waterpath);

        ArrayList<File> handsFiles = Utility.getFileList(handsDir, ".jpg", "img_");
        for (int i = 0; i < handsFiles.size(); i++) {
            System.out.println(handsFiles.get(i));
            img = Utility.loadImage(handsFiles.get(i));
            double[][][] newRGBImage = Utility.bufferedImagetoArray3D(img);

            boolean waterDetected = checkForWater(newRGBImage, expectedWaterLocation);
            paintComponent(getGraphics(), waterDetected);
            try {
                Thread.sleep(30);
            } catch (InterruptedException ex) {}
        }
    }

    public static boolean checkForWater (double[][][] img3D, double[][] expectedWaterLocation) {
        //double[][][] img3D = Utility.bufferedImagetoArray3D(img);
        double[][][] thresholdArray = WaterDetector.thresholdImage(img3D, 0, 210, 0, 210, 250, 255);
        //threshold = Utility.array3DToBufferedImage(thresholdArray);
        double[][] hist = WaterDetector.countBluePixels(thresholdArray);

        int[] clim = {0, WaterDetector.getBinSize() ^ 2};
        //histImg = Hist2D.drawHistogram(hist, clim);

        //normalize
        for (int x = 0; x < hist.length; x++) {
            for (int y = 0; y < hist[x].length; y++) {
                hist[x][y] = hist[x][y] / (WaterDetector.getBinSize() * WaterDetector.getBinSize());
            }
        }

        //active vs total
        double total = 0;
        double active = 0;
        for (int x = 0; x < hist.length; x++) {
            for (int y = 0; y < hist[x].length; y++) {
                if (expectedWaterLocation[x][y] > 0.5) {
                    total++;
                }
                if (expectedWaterLocation[x][y] > 0.5 && hist[x][y] > 0.5) {
                    active++;
                }
            }
        }
        if (active / total > 0.1) {
            System.out.println("Water Detected!");
            return true;
        } else {
            return false;
        }
    }

    public void paintComponent(Graphics g, boolean waterDetected) {
        g.drawImage(img, 0, 50, 320, 180, null);
        g.drawImage(histImg, 330, 50, 640, 360, null);
        g.drawImage(threshold, 0, 350, 640, 360, null);
        if (waterDetected) {
            Font myFont = new Font("SERIF", Font.BOLD, 25);
            g.setColor(Color.RED);
            g.drawString("WATER DETECTED", 500, 200);
        }


    }
}