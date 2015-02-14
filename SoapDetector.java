import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;// import this library
import java.awt.image.BufferedImage;

class Patch {
    private double [][][] data;
    private int x; // patch coordinate in "patch image"
    private int y; // patch coordinate in "patch image"
    public Patch(double[][][] data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }

    public Patch(double[][][] data) {
        this(data, 0, 0);
    }
    double[][][] getData() {
        return this.data;
    }

    int getX() {
        return this.x;
    }

    int getY() {
        return this.y;
    }

}

public class SoapDetector {
    //constants
    public static final int nXnSize = 50;
    public static final double M = 1280;
    public static final double N = 720;

    //variables

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
                if (densityImage[xCoordInDensityImage][yCoordInDensityImage] > 0) {
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
        double[][] soapImg = new double[newDensityImage.length][newDensityImage[0].length];
        Patch reference = new Patch(meanPatchNoSoap);
        for (int i = 0; i < patches.size(); i++) {
            Patch currentPatch = patches.get(i);
            double sum = computePatchDifference(currentPatch, reference);
            soapImg[currentPatch.getX()][currentPatch.getY()] = sum;
        }
        return soapImg;
    }


    public static void main(String[] args) throws Exception {

        //test files
        String testFile = "C:/Users/Kaushik/Documents/hands/remapped_segmentedHands_10.csv";
        String biFile = "C:/Users/Kaushik/Documents/hands/img_10.jpg";
        String soapFile = "C:/Users/Kaushik/Documents/handssoap/img_10.jpg";
        BufferedImage noSoapImg = Utility.loadImage(new File(biFile));
        double[][][] meanPatchNoSoap = Utility.bufferedImagetoArray3D(noSoapImg);

        BufferedImage soapImg = Utility.loadImage(new File(soapFile));
        double[][][] newRGBImage = Utility.bufferedImagetoArray3D(soapImg);

        //read test file for density image
        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();
        BufferedReader r = new BufferedReader(new FileReader(testFile));
        String line = "";
        while ((line = r.readLine()) != null) {
            String[] toks = line.split(",");
            x.add(Double.parseDouble(toks[0]));
            y.add(Double.parseDouble(toks[1]));
        }

        double[][] soapArray = soapDetectorImage(newRGBImage, x, y, meanPatchNoSoap);
        BufferedImage bi = Utility. d2ArrToBufferedImage(soapArray);

        Utility.writeImage(bi, "test.jpg");

        //get density image
        //double[][] d = getDensityImage(y, x);

        //load rgb image
        //BufferedImage bi = Utility.loadImage(new File(biFile));
        //double[][][] rgb3D = Utility.bufferedImagetoArray3D(bi);
        //ArrayList<double[][][]> test = extractHandPatches (d, rgb3D);

        //extract hand patch
        //double[][][] mp = extractMeanPatch(test);
        //      double[][][] tp = test.get(0);
        //      System.out.println(tp.length + " " + tp[0].length + " " + tp[0][0].length);
        //Utility.writeImage(mp, "meanPatch.jpg");

        //save to file
        //      Hist2D.saveHistToFile(d,new int[]{0,20},"test2.png");

    }


}