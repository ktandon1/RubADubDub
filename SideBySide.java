import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
public class SideBySide extends JFrame implements ActionListener {//create class
    BufferedImage img1, img2;
    JButton button;
    int counter;
    static String directory;
    public ArrayList<File> listJ, listC;
    public static void main(String[] args) { //main
        if(args.length > 0)
        {
            directory = args[0];
        }
        SideBySide id = new SideBySide();
    }
    public SideBySide() {//constructor
        super("SideBySide"); //create frame
        setSize(1300,500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); //How frame is closed
        setResizable(true);
        setVisible(true);//frame visible
        JButton button = new JButton();
        JPanel panel1 = new JPanel();
        button.addActionListener(this);        
        panel1.add(button);
        add(panel1);
        counter = 0;
        listJ = getFileList("C:\\Users\\Kaushik\\Documents\\hands",".jpg","img_");
        listC = getFileList(directory,".csv","segmentedHands_");
        double[][] k = readDepthImage(listC.get(20));        
        img1 = depthImageToBufferedImage(k);
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
        Scanner fromFile = OpenFile.openToRead(f    );
        while(fromFile.hasNext())
        {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0,temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",")+1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",")+1,temp.length()));
            depth[y][x] = z;
        }
        return depth;
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
            try
            {
                System.out.println(listJ.get(i).getName());
                img1 = ImageIO.read(listJ.get(i));
                double[][] k = readDepthImage(listC.get(i));
                img2 = depthImageToBufferedImage(k);
            } catch (IOException ex) {
                System.out.println("nope");        
            }
            paintComponent(getGraphics());
            try {
                Thread.sleep(50);
            }catch(InterruptedException ex){}
        }
        
        
        
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
        g.drawImage(img2, 640, 50, 320,240,null); 
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
}