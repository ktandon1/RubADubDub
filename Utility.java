import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class Utility
{
	public Utility()
	{

	}
	public static ArrayList<File> getFileList(String directory, String ext, String prefix)
    {
        ArrayList<File> list = new ArrayList<File>();
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
          if (listOfFiles[i].isFile() && listOfFiles[i].getName().indexOf(ext) != -1 &&listOfFiles[i].getName().indexOf(prefix) != -1 ) {
              String filePath  = directory + "\\" + listOfFiles[i].getName();
              list.add(new File(filePath));
              
          }
        }
        Collections.sort(list, new StringLengthComparator());
        return list;
    }
   public static double[][] readDepthImage(File f)
    {
        double[][] depth = new double[320][240];
        Scanner fromFile = OpenFile.openToRead(f);
        while(fromFile.hasNext())
        {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0,temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",")+1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",")+1,temp.length()));
            depth[x][y] = z;
        }
        return depth;
    }
    public BufferedImage loadImage(File jpgFile)
    {
        BufferedImage img = null;
         try {
             img = ImageIO.read(jpgFile);
             return img;
        } catch (IOException e) {
            return null;
        }
    }
    public static BufferedImage depthImageToBufferedImage(double[][] depth)
    {
        BufferedImage img = new BufferedImage(depth.length,depth[0].length,BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x<depth.length; x++)
        {
            for(int y = 0; y<depth[0].length; y++)
            {
                int color = (int)(255 * depth[x][y]/1000.0);
                img.setRGB(x,y,color);
            }
        }
        return img;
    }
    public void goToSleep()
    {
         try {
                Thread.sleep(500);
            }catch(InterruptedException ex){}   
    }
     public static void depthToCSV(double[][] depthImage, String fileName)
    {
        PrintWriter outFile = OpenFile.openToWrite(fileName);  
        for(int x = 0; x<depthImage.length;x++)
        {
            for(int y =0; y<depthImage[x].length;y++)
            {
                if(depthImage[x][y] != 0.0)
                {
                    outFile.println(y + "," + x + "," + depthImage[x][y]);
                }
            }
        }
        outFile.flush();
        outFile.close();

    }
    public int getFileNumber(String fileName)
    {
       int x = fileName.lastIndexOf("_");
       int y = fileName.lastIndexOf(".");
       int a = Integer.parseInt(fileName.substring(x+1,y));
       return a;
    }
}