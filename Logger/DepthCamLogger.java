import intel.pcsdk.*;
import java.lang.System.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;

public class DepthCamLogger {

    public static String FILE_DIR = "img/";

    public static void main(String[] args) {

    	if (args.length > 0) {
    		FILE_DIR = args[0] + "/"; 
    	}

        //make directory for images
        (new File(FILE_DIR)).mkdir();

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

                //store data (store only every iter_mod steps)
                if ((iter_ctr % iter_mod) == 0 && iter_ctr > start_frame) {

                    System.out.println("writing: " + (System.currentTimeMillis() - lastTime));
                    lastTime = System.currentTimeMillis();


                    //store image
                    File f = new File(FILE_DIR + "img_" + im_name_ctr + ".jpg");
                    try {
                        ImageIO.write(image, "jpg", f);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //obtain intensity value of depth map
                    for (int xy = 0; xy < p3.length; xy++)
                        p3[xy].z = (float)depthmap[xy];

                    try {
                        PrintWriter writer = new PrintWriter(new FileWriter(FILE_DIR + "rawdepth_" + im_name_ctr + ".csv", true));
                        for (int xy = 0; xy < p3.length; xy++) {
                            if (depthmap[xy] == untrusted[0] || depthmap[xy] == untrusted[1]) continue;

                            int x1 = (int)p3[xy].x;
                            int y1 = (int)p3[xy].y;
                            float z1 = (float) p3[xy].z; //depth of pixel (x,y)
                            writer.println(x1 + "," + y1 + "," + z1);
                        }
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //inc image name counter
                    im_name_ctr++;
                }
                iter_ctr++;

            }

            //tells camera done with this frame
            pp.ReleaseFrame();
        }

        //quit cleanly
        pp.Close();
        System.exit(0);

    }
}