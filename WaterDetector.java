import java.util.*; 
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;// import this library

public class WaterDetector
{


	public static void main(String[] args)
    {   
    
    }
    public double[][][] thresholdImage(double [][][] inputRGBImage, int red_low, int red_high, int green_low, int green_high, int blue_low, int blue_high) 
    {
    	double[][][] thresholdImg = new double[1280][720][3];
    	for(int a = 0; a<inputRGBImage.length; a++)
    	{
    		for(int b = 0; b<inputRGBImage[a].length; b++)
    		{
    			if((inputRGBImage[a][b][0] >= red_low && inputRGBImage[a][b][0] <= red_high) && (inputRGBImage[a][b][1] >= green_low && inputRGBImage[a][b][1] <= green_high) && (inputRGBImage[a][b][2] >= blue_low && inputRGBImage[a][b][2] <= blue_high))
    			{
    				thresholdImg[a][b][0] = inputRGBImage[a][b][0];
    				thresholdImg[a][b][1] = inputRGBImage[a][b][1];
    				thresholdImg[a][b][2] = inputRGBImage[a][b][2];
    			}
    			else
    			{
    				thresholdImg[a][b][0] = 0;
    				thresholdImg[a][b][1] = 0;
    				thresholdImg[a][b][2] = 0;
    			}
    		}
    	}
    	countBluePixels(thresholdImg);
    	return thresholdImg;
    }

    public void countBluePixels(double[][][] thresholdImg)
    {
    	ArrayList<Integer> xList = new ArrayList<Integer>();
    	ArrayList<Integer> yList = new ArrayList<Integer>();
    	for(int x = 0; x<thresholdImg.length; x++)
    	{
    		for(int y = 0; y<thresholdImg[x].length; y++)
    		{
    			if(thresholdImg[a][b][0] != 0)
    			{
    				xList.add(x);
    				yList.add(y);
    			}
    		}
    	}
    	double[][] hist = soapDetector.getDensityImage(xList,yList);
    }


}