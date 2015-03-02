import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;
class Utility {
    public static ArrayList<File> getFileList(String directory, String ext, String prefix) {
        ArrayList<File> list = new ArrayList<File>();
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().indexOf(ext) != -1 && listOfFiles[i].getName().indexOf(prefix) == 0 ) {
                String filePath  = directory + "/" + listOfFiles[i].getName();
                list.add(new File(filePath));

            }
        }
        Collections.sort(list, new StringLengthComparator());
        return list;
    }

    public static double[][] readDepthImage(File f) {
        // double[][] depth = new double[320][240];
        // Scanner fromFile = OpenFile.openToRead(f);
        // while (fromFile.hasNext()) {
        //     String temp = fromFile.nextLine();
        //     int x = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
        //     int y = Integer.parseInt(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
        //     double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.length()));
        //     depth[x][y] = z;
        // }
        return readDepthImage(f, 320, 240);
    }

    public static double[][] readDepthImage(File f, int width, int height) {
        double[][] depth = new double[width][height];
        Scanner fromFile = OpenFile.openToRead(f);
        while (fromFile.hasNext()) {
            String temp = fromFile.nextLine();
            int x = Integer.parseInt(temp.substring(0, temp.indexOf(",")));
            int y = Integer.parseInt(temp.substring(temp.indexOf(",") + 1, temp.lastIndexOf(",")));
            double z = Double.parseDouble(temp.substring(temp.lastIndexOf(",") + 1, temp.length()));
            depth[x][y] = z;
        }
        return depth;
    }

    public static double[][] transpose(double[][] d) {
        double[][] t = new double[d[0].length][d.length];
        for (int x = 0; x < d.length; x++) {
            for (int y = 0; y < d[0].length; y++) {
                t[y][x] = d[x][y];
            }
        }
        return t;
    }

    public static BufferedImage loadImage(File jpgFile) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(jpgFile);
            return img;
        } catch (IOException e) {
            return null;
        }
    }

    public static BufferedImage d2ArrToBufferedImage(double[][] depth, int[] clim) {
        return Hist2D.drawHistogram(transpose(depth), clim);
    }

    public static BufferedImage d2ArrToBufferedImage(double[][] depth) {
        BufferedImage img = new BufferedImage(depth.length, depth[0].length, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < depth.length; x++) {
            for (int y = 0; y < depth[0].length; y++) {
                int color = (int)(255 * depth[x][y] / 1000.0);
                img.setRGB(x, y, color);
            }
        }
        return img;
    }

    public static void goToSleep() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {}
    }
    public static void goToSleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ex) {}
    }

    public static double[][] scale(double[][] in, double s) {
        double [][] out = new double[in.length][in[0].length];
        for (int x = 0; x < in.length; x++) {
            for (int y = 0; y < in[0].length; y++ ) {
                out[x][y] = s * in[x][y];
            }
        }
        return out;
    }

    public static void d2ArrToCSV(double[][] depthImage, String fileName) {
        PrintWriter outFile = OpenFile.openToWrite(fileName);
        for (int x = 0; x < depthImage.length; x++) {
            for (int y = 0; y < depthImage[x].length; y++) {
                if (depthImage[x][y] != 0.0) {
                    outFile.println(y + "," + x + "," + depthImage[x][y]);
                }
            }
        }
        outFile.flush();
        outFile.close();

    }
    public static void d2ArrToCSV(double[][] results, String fileName, String header) {
        PrintWriter outFile = OpenFile.openToWrite(fileName);
        outFile.println(header);
        String input = "";
        for (int x = 0; x < results.length; x++) {
            for (int y = 0; y < results[x].length; y++) {
                input = input + results[x][y] + ",";
            }
            outFile.println(input.substring(0, input.length() - 2));
            input = "";
        }
        outFile.close();

    }

    public static void d2ArrToDataFile(double[][] d, String fileName) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
            oos.writeObject(d);
            oos.close();
        } catch (Exception e) {
            System.out.println("\n Error: File could not be created " + fileName);
            System.exit(1);
        }
    }

    public static double[][] DataFileToD2Arr(String fileName) {
        try {
            System.out.println("trying: " + fileName);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
            return (double[][]) ois.readObject();

        } catch (Exception e) {
            System.out.println(e);
            System.out.println("\n Error: File could not be read " + fileName);
            System.exit(1);
            return null;
        }
    }

    public static int getFileNumber(String fileName) {
        int x = fileName.lastIndexOf("_");
        int y = fileName.lastIndexOf(".");
        int a = Integer.parseInt(fileName.substring(x + 1, y));
        return a;
    }

    public static double[][] subtractBackground(double[][] backgroundImage, double[][] handsImage) {
        double DIFFERENCE_THRESH = -100.0;
        double HORIZON_THRESH = 505.0;
        double[][] difference = new double[320][240];
        double[][] foreground = new double[320][240];
        for (int a = 0; a < handsImage.length; a++) {
            for (int b = 0; b < handsImage[a].length; b++) {
                //We expect the hands to be closer to the camera than the background
                //Hence, the hands - background should be a large negative number
                //Example: hands are at 300mm from the camera, the background is at 500 mm
                //In this case, we expect the difference to be 300 - 500 = -200
                if (handsImage[a][b] != 0 && backgroundImage[a][b] != 0) {
                    difference[a][b] = handsImage[a][b] - backgroundImage[a][b];

                    //Two reasons to filter the point
                    //1. The difference is not negative enough
                    //2. The point in question is very far away
                    if (difference[a][b] > DIFFERENCE_THRESH || handsImage[a][b] > HORIZON_THRESH) {
                        continue;
                    } else {
                        foreground[a][b] = handsImage[a][b];
                    }
                }
            }
        }
        return foreground;

    }

    public static double[][][] bufferedImagetoArray3DSlow(BufferedImage b) {
        double[][][] rtn = new double[b.getHeight()][b.getWidth()][3];
        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                Color c = new Color(b.getRGB(x, y));
                rtn[y][x][0] = c.getRed();
                rtn[y][x][1] = c.getGreen();
                rtn[y][x][2] = c.getBlue();
            }
        }
        return rtn;
    }

    public static double[][][] bufferedImagetoArray3D(BufferedImage b) {
        int width = b.getWidth();
        int height = b.getHeight(); 
        double[][][] rtn = new double[b.getHeight()][b.getWidth()][3];

        int[] pixels = b.getRGB(0,0,b.getWidth(),b.getHeight(),null,0,b.getWidth());

        for (int y = 0; y < b.getHeight(); y++) {
            for (int x = 0; x < b.getWidth(); x++) {
                int rgb = pixels[y*width + x];
                int red = (rgb >> 16) & 0x000000FF;
                int green = (rgb >>8 ) & 0x000000FF;
                int blue = (rgb) & 0x000000FF;
                rtn[y][x][0] = red;
                rtn[y][x][1] = green;
                rtn[y][x][2] = blue;
            }
        }
        return rtn;
    }

    public static BufferedImage array3DToBufferedImage(double[][][] arr) {
        BufferedImage bi = new BufferedImage(arr[0].length, arr.length, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < arr.length; y++) {
            for (int x = 0; x < arr[y].length; x++) {
                int r = (int) arr[y][x][0];
                int g = (int) arr[y][x][1];
                int b = (int) arr[y][x][2];
                Color c = new Color(r, g, b);
                bi.setRGB(x, y, c.getRGB());
            }
        }
        return bi;
    }
    public static ArrayList<ArrayList<Double>> csvToArrayList(File f) {
        ArrayList<ArrayList<Double>> coordinates = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> x = new ArrayList<Double>();
        ArrayList<Double> y = new ArrayList<Double>();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        }
        String line = "";
        try {
            while ((line = r.readLine()) != null) {
                String[] toks = line.split(",");
                x.add(Double.parseDouble(toks[0]));
                y.add(Double.parseDouble(toks[1]));

            }
        } catch (IOException e) {
            System.out.println("IOException");
        }
        coordinates.add(x);
        coordinates.add(y);
        return coordinates;
    }

    public static void writeImage(BufferedImage bi, String file) {
        try {
            File outputfile = new File(file);
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeImage(double[][][] image, String file) {
        BufferedImage bi = array3DToBufferedImage(image);
        writeImage(bi, file);
    }


}