import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class BackgroundImageCreator extends JFrame 
{
	public static String backgroundDir;	
	public static ArrayList<File> backgroundFiles;
	public static double[][] backgroundImage;	
	public static BufferedImage img1;

	public static void main(String[] args)
	{
		if(args.length > 0)
        {
            backgroundDir = args[0];
        }
        BackgroundImageCreator bic = new BackgroundImageCreator();
	}
	public BackgroundImageCreator()
	{
		super("Image Displayer"); //create frame
        setSize(320,240);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        img1 = createImage();
        paintComponent(getGraphics());
	}
	public BufferedImage getBackgroundImage()
	{
		return img1;
	}
	public static BufferedImage createImage()
	{

		BufferedImage img;
        backgroundFiles = Utility.getFileList(backgroundDir,".csv","rawdepth_");
		double[][] k;   
        backgroundImage = new double[320][240];
        for(int i = 0; i<10; i++)
        {
            k = Utility.readDepthImage(backgroundFiles.get(i));
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
        String fileName = backgroundDir + "\\background.csv";
        Utility.depthToCSV(backgroundImage,fileName);
		img = Utility.depthImageToBufferedImage(backgroundImage);
     	return img;
	}
    public void paintComponent(Graphics g)
	{
        g.drawImage(img1, 0, 50, 320,180,null); 
	}
	


}