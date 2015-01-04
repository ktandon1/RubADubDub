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
	public static String backgroundDir, handsDir;
    public ArrayList<File> handsFiles;
	public BufferedImage img2;
	public static void main(String[] args) { //main
        if(args.length > 0)
        {
            backgroundDir = args[0];
            handsDir = args[1];
        }
            SegmentHands bic = new SegmentHands();

    }
	public SegmentHands()
	{
		super("Image Displayer"); //create frame
        setSize(330,250);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        segmentation();

        paintComponent(getGraphics());
	}

	public void segmentation()
	{
		double[][] backgroundImage = new double[320][240];
		String fileName = backgroundDir + "\\background.csv";
		Scanner fromFile = OpenFile.openToRead(new File(fileName));       
        while(fromFile.hasNext())
        {
        	String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0,temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",")+1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",")+1,temp.length()));
            backgroundImage[y][x] = z;
        }
		handsFiles = Utility.getFileList(handsDir,".csv","rawdepth_");
		for(int i = 0; i<handsFiles.size(); i++)
        {
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = handsDir + "\\" + fileString;
            double[][] handsImage = Utility.readDepthImage(handsFiles.get(i)); 
            System.out.println(handsFiles.get(i).getName()); 
            double[][] hands = Utility.subtractBackground(backgroundImage,handsImage);
            Utility.depthToCSV(hands,filePath);

            img2 = Utility.depthImageToBufferedImage(hands);
            paintComponent(getGraphics());
            Utility.goToSleep();
        }
	}
	public void paintComponent(Graphics g)
	{
        g.drawImage(img2, 0, 0, 320,240,null); 
	}
}