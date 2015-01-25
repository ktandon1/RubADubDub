import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;

public class Hist2D {

	/*
	hist2d takes as input
	xList: an array list of x-coordinates
	yList: an array list of y-coordinates [same num of elements as xList]
	binSize: The size of each bin in the histogram
	binStart: The first edge
*/
	public static double[][] hist(ArrayList<Double> xList, ArrayList<Double> yList, int binSize, int binStart)
	 {
		double xMax = xList.get(0);
		for (int i = 1; i < xList.size() ; i++ ) {
			if ( xList.get(i) > xMax)
			{
				xMax = xList.get(i);
			}
		}
		double yMax= yList.get(0);
		for(int a = 1; a<yList.size(); a++)
		{
			if(yList.get(a) > yMax)
			{
				yMax= yList.get(a);
			}
		}
		int xSize = (int) (xMax - binStart)/binSize + 1;	
		int ySize = (int) (yMax - binStart)/binSize + 1;
		double[][] histArray = new double [ xSize ] [ ySize ] ;
		for ( int i = 0 ; i < xList.size( ) ; i++ ) {
			double a = xList.get(i);
			double b = yList.get(i);
			int aInHist = (int) Math.floor ((double) (a - binStart )/binSize ) ;
			int bInHist = (int) Math.floor ((double) ( b - binStart ) / binSize ) ;
			histArray[aInHist][bInHist]++;
		}  
		return histArray;
	}
	
	public static BufferedImage drawHistogram(double[][] densityImage, int[] cLim) {
		
		//create buffered image
		BufferedImage bi = new BufferedImage(densityImage[0].length, densityImage.length,BufferedImage.TYPE_INT_ARGB);		
		
		//find min and max for color scaling (if not defined, use min/max of data)
		if(cLim==null) {
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			for(int y=0; y < densityImage.length; y++) {
				for(int x=0; x < densityImage[y].length; x++) {
					if(densityImage[y][x] < min) {
						min = (int) densityImage[y][x];
					}
					if(densityImage[y][x] > max) {
						max = (int) densityImage[y][x];
					}
				}
			}
			cLim = new int[2];
			cLim[0] = min;
			cLim[1] = max;
		}
		
		//loop
		for(int y=0; y < densityImage.length; y++) {
			for(int x=0; x < densityImage[y].length; x++) {
				int valToUse = (int) densityImage[y][x];
				if(valToUse < cLim[0])
					valToUse = cLim[0];
				if(valToUse > cLim[1])
					valToUse = cLim[1];
				double power = (valToUse - cLim[0]) / ((double) (cLim[1]-cLim[0]));
				Color powerColor = getPowerColor(power);
				int rgb = powerColor.getRGB();
				bi.setRGB(x,y,rgb);
			}
		}
		
		//return
		return bi;
	}
	
	protected static Color getPowerColor(double power)
	{
	    double H = power * 0.5; // Hue (note 0.4 = Green, see huge chart below)
	    double S = 0.9; // Saturation
	    double B = 0.9; // Brightness
	    return Color.getHSBColor((float)H, (float)S, (float)B);
	}
	
	public static void saveHistToFile(double[][] densityImage, int[] cLim, String file) {
		BufferedImage i = drawHistogram(densityImage,cLim);
		Utility.writeImage(i,file);		
	}	
	
	//test case
	public static void main(String[] args) {
		ArrayList<Double> xList = new ArrayList<Double>();
		ArrayList<Double> yList = new ArrayList<Double>();		
		xList.add(0.0);yList.add(0.0);
		xList.add(0.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(2.0);yList.add(1.0);
		xList.add(3.0);yList.add(1.0);
		xList.add(3.0);yList.add(1.0);
		xList.add(0.0);yList.add(2.0);
		xList.add(0.0);yList.add(2.0);
		xList.add(0.0);yList.add(2.0);
		double[][] densityImage = hist(yList,xList,1,0);
		saveHistToFile(densityImage, new int[] {0,5}, "test.png");
	}
}

//TEST!!!!!!!!!!!





