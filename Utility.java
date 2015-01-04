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
    public static void goToSleep()
    {
         try {
                Thread.sleep(50);
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
    public static double[][] subtractBackground(double[][] backgroundImage, double[][] handsImage)
    {
        double[][] difference = new double[320][240];
        double[][] foreground = new double[320][240];
     //   img2 = depthImageToBufferedImage(handsImage);
        for(int a = 0; a<handsImage.length;a++)
        {
            for(int b = 0; b<handsImage[a].length; b++)
            {
                if(handsImage[a][b] != 0 && backgroundImage[a][b] != 0) {
                    difference[a][b] = Math.abs(handsImage[a][b] - backgroundImage[a][b]);
                }
                if(difference[a][b] < 100 || handsImage[a][b] > 505)
                {
                     difference[a][b] = 0;
                }
                if(difference[a][b] != 0)
                {
                    foreground[a][b] = handsImage[a][b];
                }
            }

        }
        return foreground;
       
    } 
}