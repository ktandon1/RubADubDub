import intel.pcsdk.*;
import java.lang.System.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;

public class RTS extends JFrame {

    public static String BACKGROUND_DIR = "C:\\Users\\Pankaj Tandon\\Documents\\GitHub\\RubADubDub\\Logger\\bg";
	public static String WATER_LOCATION_DIR = "";
	public static String WATER_ZONE_DIR = "";
	public static BufferedImage img1;

	public RTS() {
		setSize(1200, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE); //How frame is closed
		setResizable(true);
        setVisible(true);//frame visible
	}

    public static void main(String[] args) {

		RTS r = new RTS();

		//read training data
        String bgFile = BACKGROUND_DIR + "/background.csv";
        double[][] backgroundImage = Utility.transpose(Utility.readDepthImage(new File(bgFile), 240, 320));
        //String waterpath = WATER_LOCATION_DIR + "/waterDetector.data";
        //double[][] expectedWaterLocation = Utility.DataFileToD2Arr(waterpath);
        //String wzFile = WATER_ZONE_DIR + "/waterZone.csv";
        //double[][] waterZone = Utility.transpose(Utility.readDepthImage(new File(wzFile), 240, 320));

        //init and start pipeline
        PXCUPipeline pp = new PXCUPipeline();
        //      if (!pp.Init(PXCUPipeline.COLOR_VGA|PXCUPipeline.GESTURE)) {
        if (!pp.Init(PXCUPipeline.COLOR_WXGA | PXCUPipeline.GESTURE)) {
            System.out.print("Failed to initialize PXCUPipeline\n");
            System.exit(3);
        }

        //query pipeline for RGB image size and create buffered image for storing new images from RGB camera.
        int[] csize = new int[2];
        pp.QueryRGBSize(csize);
        BufferedImage image = new BufferedImage(csize[0], csize[1], BufferedImage.TYPE_INT_RGB);

        //query pipeline for depth image size and set up structures for storing new depth images from depth cam
        //also, query device for trusted pixels
        int[] dsize = new int[2];
        pp.QueryDepthMapSize(dsize);
        short[]          depthmap = new short[dsize[0] * dsize[1]];
        PXCMPoint3DF32[] p3 = new PXCMPoint3DF32[dsize[0] * dsize[1]];
        PXCMPointF32[]   p2 = new PXCMPointF32[dsize[0] * dsize[1]];
        float[]          untrusted = new float[2];
        pp.QueryDeviceProperty(PXCMCapture.Device.PROPERTY_DEPTH_SATURATION_VALUE, untrusted);
        for (int xy = 0, y = 0; y < dsize[1]; y++)
            for (int x = 0; x < dsize[0]; x++, xy++)
                p3[xy] = new PXCMPoint3DF32(x, y, 0);

        //start loop for acquiring and logging images
        int iter_ctr = 0;
        int iter_mod = 2;
        int im_name_ctr = 0;
        int start_frame = 5; // start capturing after this frame number
        long lastTime =  System.currentTimeMillis();
        while (true) {

            //seeing if image can be gotten at all
            if (!pp.AcquireFrame(true)) break;

            //acquire image and store data
            if (pp.QueryRGB(image) && pp.QueryDepthMap(depthmap)) {

            	//obtain intensity value of depth map
				double[][] depthMap = new double[dsize[0]][dsize[1]];
                for (int xy = 0; xy < p3.length; xy++)
                	p3[xy].z = (float) depthmap[xy];
                for (int xy = 0; xy < p3.length; xy++) {
                	if (depthmap[xy] == untrusted[0] || depthmap[xy] == untrusted[1]) continue;
                    int x1 = (int)p3[xy].x;
                    int y1 = (int)p3[xy].y;
                    float z1 = (float) p3[xy].z; //depth of pixel (x,y)
					depthMap[x1][y1] = z1;
				}

				//segment out the hands
				double[][] hands = SegmentHands.segmentation(backgroundImage,depthMap);

				//remaps segment hands
				double[][] handsRemapped = Remapper.mapPoints2(pp,hands);
				int[] clim = {0, 1200};
				img1 = Utility.d2ArrToBufferedImage(handsRemapped, clim);
				r.repaint();
				System.out.println(handsRemapped.length + " " + handsRemapped[0].length);
            }

            //tells camera done with this frame
            pp.ReleaseFrame();
        }

        //quit cleanly
        pp.Close();
        System.exit(0);

    }

    public void paint(Graphics g) {
        g.drawImage(img1, 10, 10, 320 * 3, 240 * 3, null);
    }
}