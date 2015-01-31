import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class TrainWaterDetector extends JFrame 
{

//consts

//global vars
private static String handsDir;
private static BufferedImage threshold,histImg, img;
public static void main(String[] args)
{	
	try {
        handsDir = args[0];
		int displayResult = Integer.parseInt(args[1]);
		if(displayResult!=0 && displayResult!=1) {
			throw new Exception("Bad Input");
		}
		try {
			new TrainWaterDetector(handsDir, displayResult);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
    catch(Exception e) {
		System.out.println("\nUSAGE: java TrainWaterDetector [/path/to/rgb/images] [0/1 where 1=display result]");
	}
}

public TrainWaterDetector(String handsDir, int displayResult)
{
	super("Water Detector"); //init
	//display if needed, else exit.
	if(displayResult==1) {
        setSize(1000,1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible

        loadRGB();

		repaint();
	}
	else {
		System.exit(0);
	}
}
public void loadRGB()
{
	ArrayList<File> handsFiles = Utility.getFileList(handsDir,".jpg","img_");
	double[][] hist;
	double[][] currentTotal = null;
	for(int i = 5;i<handsFiles.size(); i++)
    {
    	System.out.println(handsFiles.get(i));
    	img = Utility.loadImage(handsFiles.get(i));
    	double[][][] img3D = Utility.bufferedImagetoArray3D(img);
    	double[][][] thresholdArray = WaterDetector.thresholdImage(img3D,0,210,0,210,250,255); 
    	threshold = Utility.array3DToBufferedImage(thresholdArray);
    	hist =WaterDetector.countBluePixels(thresholdArray);
    	if(i == 5)
    	{
    		currentTotal = hist;
    	}
    	else
    	{
    		currentTotal = Hist2D.addHistograms(hist,currentTotal);
    	}
    	//histImg = Hist2D.drawHistogram(hist,null);
    	//paintComponent(getGraphics());
    	
    }
    histImg = Hist2D.drawHistogram(currentTotal,null);
    for(int a = 0; a<currentTotal.length; a++)
    {
    	for(int b = 0; b<currentTotal[a].length; b++)
    	{
    		currentTotal[a][b] = currentTotal[a][b]/(handsFiles.size() * WaterDetector.getBinSize() * WaterDetector.getBinSize());
    	}
    }
  	String filePath = handsDir + "/waterDetector.data";
    Utility.d2ArrToDataFile(currentTotal, filePath);
  	paintComponent(getGraphics());
  	try {
                Thread.sleep(30000);
        }catch(InterruptedException ex){}

}

public void paintComponent(Graphics g)
{
        g.drawImage(img, 0, 50, 320,180,null); 
        g.drawImage(histImg, 330, 50, 640,360,null); 
}
}