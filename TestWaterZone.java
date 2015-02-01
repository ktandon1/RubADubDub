import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
public class TestWaterZone extends JFrame {

    public static final int binSize = 10;

    protected BufferedImage img2, handsRGBImage;
    private boolean detection;
    public static void main(String[] args) { //main
        try {
            String backgroundDir = args[0];
            String backgroundShampooDir = args[1];
            String testDir = args[2];
            int displayResult = Integer.parseInt(args[3]);
            if (displayResult != 0 && displayResult != 1) {
                throw new Exception("Bad Input");
            }
            try {
                TestWaterZone bic = new TestWaterZone(backgroundDir, backgroundShampooDir, testDir, displayResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java TestWaterZone [/path/to/background/files] [/path/to/background/object/dir] [/path/to/test/data/dir] [0/1 where 1=display result]");
        }
    }

    public TestWaterZone(String backgroundDir, String backgroundShampooDir, String testDir, int displayResult) {
        super("Image Displayer"); //create frame

        //display if needed
        if (displayResult == 1) {

            //run display
            setSize(1000, 1000);
            setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
            setResizable(true);
            setVisible(true);//frame visible

            segmentation(backgroundDir, backgroundShampooDir, testDir, displayResult);

        } else {
            //quit
            System.exit(0);
        }
    }

    protected void segmentation(String backgroundDir, String backgroundShampooDir, String testDir, int displayResult) {

        //read in and store background image
        System.out.println("Loading Background Image...");
        double[][] backgroundImage = new double[320][240];
        double[][] waterZone = new double[320][240];
        //load background image
        String fileName = backgroundDir + "/background.csv";
        Scanner fromFile = OpenFile.openToRead(new File(fileName));
        while (fromFile.hasNext()) {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.length()));
            backgroundImage[y][x] = z;
        }

        System.out.println("Background image loaded.");
        //load shampoo image
        System.out.println("Loading Water Zone Image" + " ...");
        String filePath  = backgroundShampooDir + "/waterZone.csv";
        fromFile = OpenFile.openToRead(new File(filePath));
        while (fromFile.hasNext()) {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.length()));
            waterZone[y][x] = z;
        }


        ArrayList<File> handsFiles = Utility.getFileList(testDir, ".csv", "rawdepth_");
        ArrayList<File> handRGBFiles = Utility.getFileList(testDir, ".jpg", "img_");
        double[][] handArray = new double[320][240];
        System.out.println("Processing...");
        for (int i = 0; i < handsFiles.size() - 1; i++) {
            double[][] handsImage = Utility.readDepthImage(handsFiles.get(i));
            System.out.println(handsFiles.get(i).getName());
            double[][] hands = Utility.subtractBackground(backgroundImage, handsImage);
            handsRGBImage = Utility.loadImage(handRGBFiles.get(i));

            detection = checkWaterZone(hands, backgroundImage, waterZone);
            if (displayResult == 1) {
                repaint();
                Utility.goToSleep();
            }
        }

        System.out.println("...");
        System.out.println("Completed");
    }
    public boolean checkWaterZone(double[][] handsDepthImage, double[][] backgroundImage, double[][] waterZone) {
        boolean detection = false;
        double[][] handPixelsInWaterZone = new double[320][240];
        ArrayList<Double> xList = new ArrayList<Double>();
        ArrayList<Double> yList = new ArrayList<Double>();
        for (int x = 0; x < handsDepthImage.length; x++) {
            for (int y = 0; y < handsDepthImage[x].length; y++) {
                double dif = Math.abs(handsDepthImage[x][y] - waterZone[x][y]);
                if (dif < 50 && handsDepthImage[x][y] != 0 && waterZone[x][y] != 0) {
                    handPixelsInWaterZone[x][y] = 1000;
                    xList.add((double)x);
                    yList.add((double)y);
                } else {
                    handPixelsInWaterZone[x][y] = 0;
                }
            }
        }
        xList.add((double)handsDepthImage.length);
        yList.add((double)handsDepthImage[0].length);
        double[][] hist = Hist2D.hist(xList, yList, binSize, 0);
        for (int x = 0; x < hist.length; x++) {
            for (int y = 0; y < hist[x].length; y++) {
                hist[x][y] = hist[x][y] / (binSize * binSize);
                if (hist[x][y] > 0.5) {
                    System.out.println(hist[x][y]);
                    hist[x][y] = 100;
                    detection = true;
                } else {
                    hist[x][y] = 0;
                }
            }
        }
        BufferedImage histImage = Hist2D.drawHistogram(hist, null);
        img2 = histImage;//Utility.d2ArrToBufferedImage(handPixelsInWaterZone);

        return detection;
    }

    public void paint(Graphics g) {
        g.drawImage(img2, 0, 0, 320, 240, null);
        g.drawImage(handsRGBImage, 330, 0, 320, 240, null);
        if (detection) {
            Font myFont = new Font("SERIF", Font.BOLD, 25);
            g.setColor(Color.GREEN);
            g.drawString("HANDS IN WATER ZONE", 500, 200);
        }

    }
}