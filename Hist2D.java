import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
public class Hist2D {

	/*
	hist2d takes as input
	xList: an array list of x-coordinates
	yList: an array list of y-coordinates [same num of elements as xList]
	binSize: The size of each bin in the histogram
	binStart: The first edge
*/
	public double[][] hist2D(ArrayList<Double> xList, ArrayList<Double> yList, int binSize, int binStart)
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
		int xSize = (int) (xMax - binStart)/binSize;	
		int ySize = (int) (yMax - binStart)/binSize;
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

/*	public BufferedImage drawHistogram(double [][] hist2d) { 
		//http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
		BufferedImage img = new BufferedImage();
		return a;
	}
	*/
}

//TEST!!!!!!!!!!!





