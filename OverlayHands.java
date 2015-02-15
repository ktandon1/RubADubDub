import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class OverlayHands extends JFrame {
    //consts


    //display
    protected BufferedImage img2;

    public static void main(String[] args) { //main
        try {
            String handsDir = args[0];
            try {
                OverlayHands bic = new OverlayHands(handsDir);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java OverlayHands [/path/to/hands/dir]");
        }
    }

    public OverlayHands(String handsDir) {
        super("Image Displayer"); //create frame

        //display if needed


        //run display
        setSize(1290, 730);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible

        //algorithm
        segmentation( handsDir);


    }
    public static BufferedImage overlayImages(BufferedImage bgImage,
            double[][] densityImage, int[] cLim) {

        BufferedImage combined = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_ARGB);
        int w = combined.getWidth();
        int h = combined.getHeight();


        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                combined.setRGB(x, y, bgImage.getRGB(x, y));
                if (densityImage[x][y] != 0) {
                    int valToUse = (int) densityImage[x][y];
                    if (valToUse < cLim[0])
                        valToUse = cLim[0];
                    if (valToUse > cLim[1])
                        valToUse = cLim[1];
                    double power = (valToUse - cLim[0]) / ((double) (cLim[1] - cLim[0]));
                    Color powerColor = Hist2D.getPowerColor(power);
                    int rgb = powerColor.getRGB();
                    for (int dx = -1; dx < 1; dx++) {
                        for (int dy = -1; dy < 1; dy++) {
                            if (x + dx >= 0 && x + dx < w &&
                                    y + dy >= 0 && y + dy < h) {
                                combined.setRGB(x+dx, y+dy, rgb);
                            }
                        }
                    }
                }
            }
        }


        return combined;
    }

    protected void segmentation(String handsDir) {

        ArrayList<File> imageFiles = Utility.getFileList(handsDir, ".jpg", "img_" );

        //for each file in hands dir, do background subtraction and store result.
        System.out.println("Processing " + handsDir + " ...");
        ArrayList<File> handsFiles = Utility.getFileList(handsDir, ".csv", "remapped_segmentedHands_");
        //for (int i = 0; i < handsFiles.size(); i++) {
        for (int i = 0; i < 1; i++) {

            BufferedImage img = Utility.loadImage(imageFiles.get(i));

            //load file
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = handsDir + "/" + fileString;
            double[][] handsImage = Utility.readDepthImage(handsFiles.get(i), 1280, 720);
            System.out.println(handsFiles.get(i).getName());

            int[] clim = {0, 1200};
            BufferedImage hands = Utility.d2ArrToBufferedImage(handsImage, clim);
            img2 = overlayImages(img, handsImage, clim);
            Utility.writeImage(img2, "overlay.png");


            repaint();
            Utility.goToSleep();

        }
        System.out.println("...");
        System.out.println("Hand segmentations successfully written.");
    }

    public void paint(Graphics g) {
        g.drawImage(img2, 0, 0, 1280, 720, null);
    }
}
