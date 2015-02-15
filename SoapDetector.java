import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class SoapDetector extends JFrame {
    //constants
    public static final int nXnSize = 50;
    public static final double M = 1280;
    public static final double N = 720;
    public static final double densityThreshold = 100;

    //variables
    public static BufferedImage bi, rgbImage, soapImg, remap, meanPatch;
    public static double[][] getDensityImage(ArrayList<Double> x, ArrayList<Double> y) {
        x.add(N);
        y.add(M);
        double[][] densityArray = Hist2D.hist(x, y, nXnSize, 0);
        return densityArray;
    }

    public static ArrayList<Patch> extractHandPatches (double[][] densityImage, double[][][] rgbImage) {
        System.out.println(densityImage.length + " " + densityImage[0].length + " " + rgbImage.length + " " + rgbImage[0].length + " " + rgbImage[0][0].length);
        ArrayList<Patch> patches = new ArrayList<Patch>();
        for (int a = 0; a < (rgbImage.length - nXnSize); a += nXnSize) {
            for (int b = 0; b < (rgbImage[a].length - nXnSize); b += nXnSize) {
                double[][][] patch = new double[nXnSize][nXnSize][3];
                int xCoordInDensityImage = (int)Math.floor(a / nXnSize);
                int yCoordInDensityImage = (int)Math.floor(b / nXnSize);
                if (densityImage[xCoordInDensityImage][yCoordInDensityImage] > densityThreshold) {
                    for (int c = 0; c < nXnSize; c++) {
                        for (int d = 0; d < nXnSize; d++) {
                            for (int e = 0; e < 3; e++) {
                                patch[c][d][e] = rgbImage[a + c][b + d][e];
                            }
                        }
                    }
                    patches.add(new Patch(patch, xCoordInDensityImage, yCoordInDensityImage));
                }
            }
        }
        return patches;
    }

    public static double[][][] testExtractHandPatches (double[][] densityImage, double[][][] rgbImage) {
        double[][][] out = new double[rgbImage.length][rgbImage[0].length][rgbImage[0][0].length];
        for (int a = 0; a < (rgbImage.length - nXnSize); a += nXnSize) {
            for (int b = 0; b < (rgbImage[a].length - nXnSize); b += nXnSize) {
                double[][][] patch = new double[nXnSize][nXnSize][3];
                int xCoordInDensityImage = (int)Math.floor(a / nXnSize);
                int yCoordInDensityImage = (int)Math.floor(b / nXnSize);
                if (densityImage[xCoordInDensityImage][yCoordInDensityImage] > densityThreshold) {
                    for (int c = 0; c < nXnSize; c++) {
                        for (int d = 0; d < nXnSize; d++) {
                            for (int e = 0; e < 3; e++) {
                                out[a + c][b + d][e] = rgbImage[a + c][b + d][e];
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    public static double[][][] extractMeanPatch(ArrayList<Patch> patches) {
        double[][][] meanPatch = new double[nXnSize][nXnSize][3];
        for (int i = 0; i < patches.size(); i++) {
            double[][][] temp = patches.get(i).getData();
            for (int a = 0; a < temp.length; a++) {
                for (int b = 0; b < temp[a].length; b++) {
                    for (int c = 0; c < temp[a][b].length ; c++) {
                        meanPatch[a][b][c] += temp[a][b][c];
                    }
                }
            }

        }
        for (int a = 0; a < meanPatch.length; a++) {
            for (int b = 0; b < meanPatch[a].length; b++) {
                meanPatch[a][b][0] = meanPatch[a][b][0] / (patches.size());
                meanPatch[a][b][1] = meanPatch[a][b][1] / (patches.size());
                meanPatch[a][b][2] = meanPatch[a][b][2] / (patches.size());
            }
        }
        return meanPatch;
    }


    public static double computePatchDifference(double[][][] patch1, double[][][] patch2) {
        double patch1Dif = 0;
        double patch2Dif = 0;
        for (int a = 0; a < patch1.length; a++) {
            for (int b = 0; b < patch1[a].length; b++) {
                for (int c = 0; c < patch1[a][b].length ; c++) {
                    patch1Dif += patch1[a][b][c];
                    patch2Dif += patch2[a][b][c];
                }
            }
        }
        double totalDif = Math.abs(patch1Dif - patch2Dif);
        return totalDif;
    }

    public static double computePatchDifference(Patch patch1, Patch patch2) {
        return computePatchDifference(patch1.getData(), patch2.getData());
    }

    public static double[][] soapDetectorImage(double[][][] newRGBImage, ArrayList<Double> x, ArrayList<Double> y, double[][][] meanPatchNoSoap) {
        double[][] newDensityImage = getDensityImage(x, y);
        ArrayList<Patch> patches = extractHandPatches(newDensityImage, newRGBImage);
        double[][][] patchImg = testExtractHandPatches(newDensityImage, newRGBImage);
        soapImg = Utility.array3DToBufferedImage(patchImg);
        double[][] soapImg = new double[newDensityImage.length][newDensityImage[0].length];
        Patch reference = new Patch(meanPatchNoSoap);
        for (int i = 0; i < patches.size(); i++) {
            Patch currentPatch = patches.get(i);
            double sum = computePatchDifference(currentPatch, reference);
            soapImg[currentPatch.getX()][currentPatch.getY()] = sum;
        }
        return soapImg;
    }
    public void soapDetecting(String handsDir, String handsSoapDir, int displayResult) {
        ArrayList<File> handsFiles = Utility.getFileList(handsDir, ".jpg", "img_");
        ArrayList<File> remappedHandsFiles = Utility.getFileList(handsDir, ".csv", "remapped_segmentedHands_");
        ArrayList<ArrayList<Double>> remappedSegmentedHands = Utility.csvToArrayList(remappedHandsFiles.get((5)));
        //compute mean patch using the first (10th) RGB AND depth image in a no soap environment
        File noSoapFile = handsFiles.get(5);
        double[][] densityImage  = getDensityImage(remappedSegmentedHands.get(1), remappedSegmentedHands.get(0));
        rgbImage  = Utility.loadImage(noSoapFile);
        double[][][] rgbArray = Utility.bufferedImagetoArray3D(rgbImage);
        ArrayList<Patch> meanPatchList = extractHandPatches(densityImage, rgbArray);
        rgbArray = testExtractHandPatches(densityImage, rgbArray);
        rgbImage = Utility.array3DToBufferedImage(rgbArray);
        double [][][] meanPatchNoSoap = extractMeanPatch(meanPatchList);
        meanPatch = Utility.array3DToBufferedImage(meanPatchNoSoap);
        handsFiles = null;
        remappedHandsFiles = null; 

        ArrayList<File> handSoapFiles = Utility.getFileList(handsSoapDir, ".jpg", "img_");
        ArrayList<File> remappedHandSoapFiles = Utility.getFileList(handsSoapDir, ".csv", "remapped_segmentedHands_");

        double[][][] newRGBImage;
        for (int i = 10; i < handSoapFiles.size(); i++) {

            //load RGB image with SOAP
            //load remapped depth image with SOAP
            soapImg = Utility.loadImage(handSoapFiles.get(i));
            newRGBImage = Utility.bufferedImagetoArray3D(soapImg);

            // Write a function CSV to ArrayList in Utility that does this.
            ArrayList<ArrayList<Double>> coordinates = Utility.csvToArrayList(remappedHandSoapFiles.get(i));
            ArrayList<Double> x = coordinates.get(0);
            ArrayList<Double> y = coordinates.get(1);
            //read test file for density image

            double[][] remapped = new double[1280][720];
            for (int g = 0; g < x.size(); g++) {
                int a = x.get(g).intValue();
                int b = y.get(g).intValue();
                remapped[a][b] = 1000;
            }
            remap = Utility.d2ArrToBufferedImage(remapped);
            double[][] soapArray = soapDetectorImage(newRGBImage, y, x, meanPatchNoSoap);
            int[] clim = { -200000, 1000000};
            bi = Hist2D. drawHistogram(soapArray, clim);
            paintComponent(getGraphics());
            Utility.goToSleep();
            Utility.writeImage(bi, "test.jpg");
        }
        System.out.println("done");

    }
    public static void main(String[] args) {
        try {
            String handsDir = args[0];
            String handsSoapDir = args[1];
            int displayResult = Integer.parseInt(args[2]);
            if (displayResult != 0 && displayResult != 1) {
                throw new Exception("Bad Input");
            }
            try {
                SoapDetector bic = new SoapDetector(handsDir, handsSoapDir, displayResult);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("\nUSAGE: java SoapDetector [/path/to/hands/files] [/path/to/hands/soap/dir] [0/1 where 1=display result]");
        }
    }
    public SoapDetector(String handsDir, String handsSoapDir, int displayResult) {
        super("Image Displayer"); //create frame
        //display if needed
        if (displayResult == 1) {

            //run display
            setSize(1500, 1000);
            setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
            setResizable(true);
            setVisible(true);//frame visible

            soapDetecting(handsDir, handsSoapDir, displayResult);

        } else {
            //quit
            System.exit(0);
        }


    }
    public void paintComponent(Graphics g) {
        g.drawImage(bi, 0, 0, 250, 250, null);
        g.drawImage(rgbImage, 0, 300, 320, 240, null);
        g.drawImage(soapImg, 400, 0, 320, 240, null);
        g.drawImage(remap, 400, 400, 320, 240, null);
        g.drawImage(meanPatch, 150, 500, 320, 240, null);
    }
}

