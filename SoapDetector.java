import java.util.*; 
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;// import this library

public class SoapDetector
{
    //constants
    public static final int nXnSize = 50;
    public static final double M = 1280;
    public static final double N = 720;
    
    //variables

    public double[][] getDensityImage(ArrayList<Double> x, ArrayList<Double> y)
    {
        x.add(M);
        y.add(N);
        double[][] densityArray = Hist2D.hist(x,y,nXnSize,0);
        return densityArray;
    }
    public ArrayList<double[][][]> extractHandPatches (double[][] densityImage, double[][][] rgbImage)
    {
        ArrayList<double[][][]> patches = new ArrayList<double[][][]>();
        for(int a = 0; a<rgbImage.length; a+=nXnSize)
        {
            for(int b = 0; b<rgbImage[a].length; b+= nXnSize)
            {
                double[][][] patch = new double[nXnSize][nXnSize][3];
                if(densityImage[a][b] > 0)
                {
                    for(int c = 0; c<nXnSize; c++)
                    {
                       for(int d = 0; d<nXnSize; d++)
                       {
                          for(int e = 0; e<3; e++)
                          { 
                             patch[c][d][e] = rgbImage[a+c][b+d][e];
                          }
                       }
                    }
                patches.add(patch);
                }
            }
        }
        return patches;
    }
    public double[][][] extractMeanPatch(ArrayList<double[][][]> patches)
    {
        double[][][] meanPatch = new double[nXnSize][nXnSize][3];
        for(int i = 0; i< patches.size(); i++)
        {
            double[][][] temp = patches.get(i);
            for(int a = 0; a<temp.length; a++)
            {
                for(int b = 0; b<temp[a].length; b++)
                {
                    for(int c = 0; c<temp[a][b].length ; c++)
                    {
                        meanPatch[a][b][c] += temp[a][b][c];
                    }
                }
            }
            
        }
        for(int a = 0; a<meanPatch.length; a++)
        {
            for(int b = 0; b<meanPatch[a].length; b++)
            {
                meanPatch[a][b][0] = meanPatch[a][b][0]/(patches.size());
                meanPatch[a][b][1] = meanPatch[a][b][1]/(patches.size());
                meanPatch[a][b][2] = meanPatch[a][b][2]/(patches.size());
            }
        }
        return meanPatch;
    }
    public double computePatchDifference(double[][][] patch1, double[][][] patch2)
    {
        double patch1Dif = 0;
        double patch2Dif = 0;
        for(int a = 0; a<patch1.length; a++)
        {
            for(int b = 0; b<patch1[a].length; b++)
            {
                for(int c = 0; c<patch1[a][b].length ; c++)
                {
                    patch1Dif += patch1[a][b][c];
                    patch2Dif += patch2[a][b][c];
                }
            }
        }
        double totalDif = Math.abs(patch1Dif-patch2Dif);
        return totalDif;
    }


    public static void main(String[] args)
    {
    
        
    
    }


}