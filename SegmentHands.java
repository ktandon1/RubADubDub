import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class SegmentHands extends JFrame
{
	//consts
	public static final double DIFFERENCE_THRESH = -100.0;
	public static final double HORIZON_THRESH = 505.0;
	
	//display
	protected BufferedImage img2;

	public static void main(String[] args) { //main
		try
		{
    		String backgroundDir = args[0];
        	String handsDir = args[1];
			int displayResult = Integer.parseInt(args[2]);
			if(displayResult!=0 && displayResult!=1) 
			{
				throw new Exception("Bad Input");
			}			
			try
			{
        		SegmentHands bic = new SegmentHands(backgroundDir, handsDir, displayResult);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e) 
		{
			System.out.println("\nUSAGE: java SegmentHands [/path/to/background/files] [/path/to/hands/dir] [0/1 where 1=display result]");
		}
    }

	public SegmentHands(String backgroundDir, String handsDir, int displayResult)
	{
		super("Image Displayer"); //create frame
		        
		//display if needed
		if(displayResult==1) 
		{
			
			//run display
        	setSize(330,250);
        	setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
        	setResizable(true);
        	setVisible(true);//frame visible

			//algorithm
			segmentation(backgroundDir,handsDir,displayResult);

		}
		else {
			
			//algorithm
			segmentation(backgroundDir,handsDir,displayResult);
			
			//quit
			System.exit(0);
		}
	}
	
    protected double[][] subtractBackground(double[][] backgroundImage, double[][] handsImage)
    {
        double[][] difference = new double[320][240];
        double[][] foreground = new double[320][240];
        for(int a = 0; a<handsImage.length;a++)
        {
            for(int b = 0; b<handsImage[a].length; b++)
            {
                //We expect the hands to be closer to the camera than the background 
                //Hence, the hands - background should be a large negative number
                //Example: hands are at 300mm from the camera, the background is at 500 mm
                //In this case, we expect the difference to be 300 - 500 = -200
                if(handsImage[a][b] != 0 && backgroundImage[a][b] != 0) {
                    difference[a][b] = handsImage[a][b] - backgroundImage[a][b];

                    //Two reasons to filter the point
                    //1. The difference is not negative enough 
                    //2. The point in question is very far away 
                    if(difference[a][b] > DIFFERENCE_THRESH || handsImage[a][b] > HORIZON_THRESH)
                    {
                      continue;
                    } else
                    {
                      foreground[a][b] = handsImage[a][b];
                    }
                }
            }
        }
        return foreground;
       
    } 

	protected void segmentation(String backgroundDir, String handsDir, int displayResult)
	{
		
		//read in and store background image
		System.out.println("Loading Background Image...");
		double[][] backgroundImage = new double[320][240];
		String fileName = backgroundDir + "/background.csv";
		Scanner fromFile = OpenFile.openToRead(new File(fileName));       
        while(fromFile.hasNext())
        {
        	String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0,temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",")+1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",")+1,temp.length()));
            backgroundImage[y][x] = z;
        }
		System.out.println("Background image loaded.");

		//for each file in hands dir, do background subtraction and store result.
		System.out.println("Processing " + handsDir + " ...");
		ArrayList<File> handsFiles = Utility.getFileList(handsDir,".csv","rawdepth_");
		for(int i = 0; i<handsFiles.size(); i++)
        {
	
			//compute hands segmentation and write csv
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = handsDir + "/" + fileString;
            double[][] handsImage = Utility.readDepthImage(handsFiles.get(i)); 
            System.out.println(handsFiles.get(i).getName()); 
            double[][] hands = subtractBackground(backgroundImage,handsImage);
            Utility.d2ArrToCSV(hands,filePath);
			
			//display if needed
			if(displayResult==1)
			{
            	img2 = Utility.d2ArrToBufferedImage(hands);
				repaint();
            	Utility.goToSleep();
			}
        }
		System.out.println("...");
		System.out.println("Hand segmentations successfully written.");
	}
	
	public void paint(Graphics g)
	{
        g.drawImage(img2, 0, 0, 320,240,null); 
	}
}
