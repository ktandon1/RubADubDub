import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
public class DrawOneImage extends JFrame {
    BufferedImage img;
    public static void main(String[] args) {
        try {
            String imgDir = args[0];
            int numImage = Integer.parseInt(args[1]);
            try {
                DrawOneImage bic = new DrawOneImage(imgDir, numImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java DrawOneImage [/path/to/image/files] [# image to be shown]");


        }
    }

    public DrawOneImage(String imgDir, int numImage) {
        super("One Image"); //create frame

        //run display
        setSize(1000, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible

        ArrayList<File> testRGBFiles = Utility.getFileList(imgDir, ".csv", "rawdepth_");
        for (int i = 0; i < testRGBFiles.size(); i++) {
            if (i == numImage) {
                int[] clim = {0, 1600};
                double[][] temp = Utility.readDepthImage(testRGBFiles.get(numImage));
                img = Utility.d2ArrToBufferedImage(temp,clim);
                paintComponent(getGraphics());
                Utility.goToSleep(500);
            }
        }

    }
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, 320, 240, null);
    }
}
