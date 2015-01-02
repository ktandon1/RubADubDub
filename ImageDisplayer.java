import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
public class ImageDisplayer extends JFrame implements ActionListener {//create class

    //constants
    public static final String SEG_HANDS_PATH = "C:\\Users\\Kaushik\\Documents\\SegHands\\";
    public static final double patch = 20;

    BufferedImage img1, img2, img3, img4,img5;
    JButton button;
    int counter;
    public static String backgroundDir, handsDir;
    public ArrayList<File> listJ, listC, listK;
    public double[][] backgroundImage;
    public double[] centroid;
    public static void main(String[] args) { //main
        if(args.length > 0)
        {
            backgroundDir = args[0];
            handsDir = args[1];
        }
        System.out.println(handsDir + " " + backgroundDir);
        ImageDisplayer id = new ImageDisplayer();

    }
    public ImageDisplayer() {//constructor
        super("Image Displayer"); //create frame
        setSize(1500,900);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        JButton button = new JButton();
        button.addActionListener(this);        
        add(button);
        counter = 0;    
        listJ = getFileList(handsDir,".csv","rawdepth_");
        listC = getFileList(backgroundDir,".csv","rawdepth_");
        listK = getFileList("C:\\Users\\Kaushik\\Documents\\RemappedHands",".csv","remapped_segmentedHands_");
        double[][] k;   
        backgroundImage = new double[320][240];
        for(int i = 0; i<10; i++)
        {
            k = readDepthImage(listC.get(i));
            for(int x = 0; x<k.length;x++)
            {
                for(int y = 0; y<k[x].length;y++)
                {
                    if(k[x][y] != 0)
                    {
                        backgroundImage[x][y] = Math.max(k[x][y],backgroundImage[x][y]); // result is the background image
                    }
                }
            }
        }   
        img1 = depthImageToBufferedImage(backgroundImage);
        paintComponent(getGraphics());
    }
    public ArrayList<File> getFileList(String directory, String ext, String prefix)
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
    public double[][] readDepthImage(File f)
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
    public double[][] readThirdImage(File csvFile)
    {
        double xCount = 0; 
        double yCount = 0;
        double xyCount = 0;
        centroid = new double[2];
        double[][] rgbHand = new double[Remapper.RGB_IMG_LENGTH][Remapper.RGB_IMG_WIDTH];
        int fileNum = getFileNumber(csvFile.getName());
        Scanner fromFile = OpenFile.openToRead(csvFile);       
        while(fromFile.hasNext())
        {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0,temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",")+1, temp.lastIndexOf(",")));
            rgbHand[x][y] = 1;
            xCount += x;
            yCount += y;
            xyCount++;
        }
        centroid[0] = xCount/xyCount;
        centroid[1] = yCount/xyCount;
        return rgbHand;
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

    public BufferedImage thirdDepthToBuffered(double[][] rgbHand, BufferedImage handImage)
    {
        BufferedImage img = new BufferedImage(rgbHand.length,rgbHand[0].length,BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x<rgbHand.length; x++)
        {
            for(int y = 0; y<rgbHand[0].length; y++)
            {
                int color;
                if(rgbHand[x][y]==1) {
                    color = handImage.getRGB(x,y);
                    img.setRGB(x,y,color);
                }
                if(Math.abs(x-centroid[0]) < 20 && Math.abs(y-centroid[1]) < 20) {
                    img.setRGB(x,y,150);
                }

            }
        }
        return img;
    }
    public BufferedImage depthImageToBufferedImage(double[][] depth)
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
    public void actionPerformed(ActionEvent e) 
    {
        System.out.println("Button pressed");
        for(int i = 0; i<listJ.size(); i++)
        {
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = SEG_HANDS_PATH + fileString;
            double[][] k = readDepthImage(listJ.get(i)); 
            System.out.println(listJ.get(i).getName()); 
            double[][] hands = subtractBackground(backgroundImage,k);
            depthToCSV(hands,filePath);

            img2 = depthImageToBufferedImage(hands);
            double[][] third = readThirdImage(listK.get(i));
            File f = new File("C:\\Users\\Kaushik\\Documents\\hands\\" + "img_" + i + ".jpg");
            BufferedImage handImage = loadImage(f);

            img3 = thirdDepthToBuffered(third, handImage);
            img4 = centroidPatch(handImage);
            img5 = handImage;
            paintComponent(getGraphics());
            goToSleep();
        }
    }
    public void goToSleep()
    {
         try {
                Thread.sleep(500);
            }catch(InterruptedException ex){}   
    }

    public double[][] subtractBackground(double[][] backgroundImage, double[][] handsImage)
    {
        double[][] difference = new double[320][240];
        double[][] foreground = new double[320][240];
        img2 = depthImageToBufferedImage(handsImage);
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
    public BufferedImage centroidPatch(BufferedImage img)
    {
        BufferedImage patchImg = new BufferedImage((int)(2 * patch),(int)(2 * patch),BufferedImage.TYPE_INT_RGB);           
        for(double x = centroid[0] - patch; x< centroid[0] + patch; x++)
        {
            for(double y = centroid[1] -patch; y<centroid[1] + patch; y++)
            {
                patchImg.setRGB((int)(x+patch-centroid[0]),(int)(y+patch-centroid[1]),img.getRGB((int)(x),(int)(y)));
                img.setRGB((int)x,(int)y,150);
            }
        }

        return patchImg;
    }
    public void depthToCSV(double[][] depthImage, String fileName)
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
    public void paintComponent(Graphics g)
    {
        g.drawImage(img1, 0, 50, 320,180,null); 
        g.drawImage(img2, 350, 50, 320,240,null); 
        g.drawImage(img3, 700,50,320,240,null);
        g.drawImage(img4, 1050,50,320,240,null);
        g.drawImage(img5,0,300,320,240,null);
    }
}
