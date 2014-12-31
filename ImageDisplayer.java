    import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
public class ImageDisplayer extends JFrame implements ActionListener {//create class
    BufferedImage img1, img2;
    JButton button;
    int counter;
    public static String backgroundDir, handsDir;
    public ArrayList<File> listJ, listC;
    public double[][] backgroundImage;
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
        setSize(1300,500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        JButton button = new JButton();
        button.addActionListener(this);        
        add(button);
        counter = 0;    
        listJ = getFileList(handsDir,".csv","rawdepth_");
        listC = getFileList(backgroundDir,".csv","rawdepth_");
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
    public void actionPerformed(ActionEvent e) 
    {
        System.out.println("Button pressed");
        for(int i = 0; i<listJ.size(); i++)
        {
            String fileString = "segmentedHands_" + i + ".csv";
            String filePath  = handsDir + "\\" + fileString;
            double[][] k = readDepthImage(listJ.get(i)); 
            System.out.println(listJ.get(i).getName()); 
            double[][] hands = subtractBackground(backgroundImage,k);
            depthToCSV(hands,filePath);

            img2 = depthImageToBufferedImage(hands);
            paintComponent(getGraphics());

            try {
                Thread.sleep(15);
            }catch(InterruptedException ex){}

        }
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
    public void depthToCSV(double[][] depthImage, String fileName)
    {
        PrintWriter outFile = OpenFile.openToWrite(fileName);  
        for(int x = 0; x<depthImage.length;x++)
        {
            for(int y =0; y<depthImage[x].length;y++)
            {
                if(depthImage[x][y] != 0.0)
                {
                    outFile.println(x + "," + y + "," + depthImage[x][y]);
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
        g.drawImage(img1, 0, 50, 640,360,null); 
        g.drawImage(img2, 640, 50, 640,480,null); 
    }
}
class StringLengthComparator implements Comparator<File> {
    public int compare(File o1, File o2) {
        int a = getFileNumber(o1.getName());
        int b = getFileNumber(o2.getName());
        if (a < b) {
            return -1;
        } else if (a > b) {
            return 1;
        } else {
            return 0;
        }
    }
    public int getFileNumber(String fileName)
    {
       int x = fileName.lastIndexOf("_");
       int y = fileName.lastIndexOf(".");
       int a = Integer.parseInt(fileName.substring(x+1,y));
       return a;
    }
}
class OpenFile {
    public static Scanner openToRead(File fileName)
    {
        Scanner fromFile = null;
        try {
            fromFile = new Scanner(fileName);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("\n Error: File could not be found");
            System.exit(1);
        }
        return fromFile;
    }
    public static PrintWriter openToWrite(String fileString)
    {
        PrintWriter outFile = null;
        try {
                outFile = new PrintWriter(fileString);
        }
        catch(Exception e)
        {
            System.out.println("\n Error: File could not be created");
            System.exit(1);
        }
        return outFile;
    }   
}

