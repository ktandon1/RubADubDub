import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;// import this library
import java.awt.image.BufferedImage;

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

    public static ArrayList<double[][][]> extractHandPatches (double[][] densityImage, double[][][] rgbImage) {
        System.out.println(densityImage.length + " " + densityImage[0].length + " " + rgbImage.length + " " + rgbImage[0].length + " " + rgbImage[0][0].length);
        ArrayList<double[][][]> patches = new ArrayList<double[][][]>();
        for (int a = 0; a < (rgbImage.length - nXnSize); a += nXnSize) {
            for (int b = 0; b < (rgbImage[a].length - nXnSize); b += nXnSize) {
                double[][][] patch = new double[nXnSize][nXnSize][3];
                if (densityImage[(int)Math.floor(a / nXnSize)][(int)Math.floor(b / nXnSize)] > 0) {
                    for (int c = 0; c < nXnSize; c++) {
                        for (int d = 0; d < nXnSize; d++) {
                            for (int e = 0; e < 3; e++) {
                                patch[c][d][e] = rgbImage[a + c][b + d][e];
                            }
                        }
                    }
                    patches.add(patch);
                }
            }
        }
        return patches;
    }

    public static double[][][] extractMeanPatch(ArrayList<double[][][]> patches) {
        double[][][] meanPatch = new double[nXnSize][nXnSize][3];
        for (int i = 0; i < patches.size(); i++) {
            double[][][] temp = patches.get(i);
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

    public double computePatchDifference(double[][][] patch1, double[][][] patch2) {
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


    public static void main(String[] args) throws Exception {

        //test files
        String testFile = "/Users/prateek/Desktop/kaushik_data/new_data/bathroom_nosoap/remapped_segmentedHands_0.csv";
        String biFile = "/Users/prateek/Desktop/kaushik_data/new_data/bathroom_nosoap/img_0.jpg";

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

        //get density image
        double[][] d = getDensityImage(y, x);

        //load rgb image
        BufferedImage bi = Utility.loadImage(new File(biFile));
        double[][][] rgb3D = Utility.bufferedImagetoArray3D(bi);
        ArrayList<double[][][]> test = extractHandPatches (d, rgb3D);

        //extract hand patch
        double[][][] mp = extractMeanPatch(test);
        //      double[][][] tp = test.get(0);
        //      System.out.println(tp.length + " " + tp[0].length + " " + tp[0][0].length);
        Utility.writeImage(mp, "meanPatch.jpg");

        //save to file
        //      Hist2D.saveHistToFile(d,new int[]{0,20},"test2.png");

    }


}