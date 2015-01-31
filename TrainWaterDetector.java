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
	String handsDir;
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
	        setSize(320,240);
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
		for(int i = 0; i<handsFiles.size(); i++)
        {
	
            String fileString = "segmentedHands_" + i + ".csv";
	}
}