import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class BackgroundImageCreator extends JFrame 
{
	
	//consts
	
	//global vars
	protected BufferedImage img1;
	
	public static void main(String[] args)
	{	
		try {
            String backgroundDir = args[0];
			int displayResult = Integer.parseInt(args[1]);
			if(displayResult!=0 && displayResult!=1) {
				throw new Exception("Bad Input");
			}
			try {
				new BackgroundImageCreator(backgroundDir, displayResult);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
        catch(Exception e) {
			System.out.println("\nUSAGE: java BackgroundImageCreator [/path/to/background/images] [0/1 where 1=display result]");
		}
	}
	
	public BackgroundImageCreator(String backgroundDir, int displayResult)
	{
		super("Image Displayer"); //init
		
		//create image
        img1 = createImage(backgroundDir);

		//display if needed, else exit.
		if(displayResult==1) {
	        setSize(320,240);
	        setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
	        setResizable(true);
	        setVisible(true);//frame visible
			repaint();
		}
		else {
			System.exit(0);
		}
	}
		
	public static BufferedImage createImage(String backgroundDir)
	{
        ArrayList<File> backgroundFiles = Utility.getFileList(backgroundDir,".csv","rawdepth_");
        double[][] backgroundImage = new double[320][240];
		System.out.println("Processing...");
        for(int i = 0; i < backgroundFiles.size() - 1; i++)
        {
			System.out.println(backgroundFiles.get(i));
            double[][] k = Utility.readDepthImage(backgroundFiles.get(i));
            for(int x = 0; x<k.length;x++)
            {
                for(int y = 0; y<k[x].length;y++)
                {
                    if(k[x][y] != 0)
                    {
                        backgroundImage[x][y] = Math.max(k[x][y],backgroundImage[x][y]);
                    }
                }
            }
        }
		System.out.println("...");
        String fileName = backgroundDir + "/background.csv";
        Utility.d2ArrToCSV(backgroundImage,fileName);
		BufferedImage img = Utility.d2ArrToBufferedImage(backgroundImage);
		System.out.println("Image generated.");
     	return img;
	}
	
    public void paint(Graphics g)
	{
        g.drawImage(img1, 0, 50, 320,180,null); 
	}	
}
