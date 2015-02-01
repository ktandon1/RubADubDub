/*******************************************************************************

INTEL CORPORATION PROPRIETARY INFORMATION
This software is supplied under the terms of a license agreement or nondisclosure
agreement with Intel Corporation and may not be copied or disclosed except in
accordance with the terms of that agreement
Copyright(c) 2013 Intel Corporation. All Rights Reserved.

*******************************************************************************/
import intel.pcsdk.*;
import java.lang.System.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;

public class HelloJava extends JApplet {
    public static void main(String s[]) { 
		PXCUPipeline pp=new PXCUPipeline();
		if (!pp.Init(PXCUPipeline.COLOR_VGA|PXCUPipeline.GESTURE)) {
			System.out.print("Failed to initialize PXCUPipeline\n");
			System.exit(3);
		}

		int[] dsize=new int[2];
		pp.QueryDepthMapSize(dsize);
		int[] csize=new int[2];
		pp.QueryRGBSize(csize);

        HelloJava hj=new HelloJava(); 
		DrawFrame df=new DrawFrame(csize[0],csize[1]);
		DrawFrame df2=new DrawFrame(csize[0],csize[1]);

        hj.add(df);

        JFrame frame=new JFrame("Intel(R) Perceptual Computing SDK Java Sample"); 
		Listener listener=new Listener();
        frame.addWindowListener(listener);
		frame.setSize(csize[0],csize[1]); 
        frame.add(hj);
        frame.setVisible(true); 

		short[]          depthmap=new short[dsize[0]*dsize[1]];
		PXCMPoint3DF32[] p3=new PXCMPoint3DF32[dsize[0]*dsize[1]];
		PXCMPointF32[]   p2=new PXCMPointF32[dsize[0]*dsize[1]];
		float[]          untrusted=new float[2];
		float[] depth_smoothing = new float[1];
		depth_smoothing[0] = 0;
		pp.QueryDeviceProperty(PXCMCapture.Device.PROPERTY_DEPTH_SATURATION_VALUE,untrusted);
		pp.SetDeviceProperty(PXCMCapture.Device.PROPERTY_DEPTH_SMOOTHING, depth_smoothing);
		System.out.println("untrusted: " + untrusted[0] + ", " + untrusted[1]);
		for (int xy=0,y=0;y<dsize[1];y++)
			for (int x=0;x<dsize[0];x++,xy++)
				p3[xy]=new PXCMPoint3DF32(x,y,0);

		while (!listener.exit) {
			if (!pp.AcquireFrame(true)) break;
			if (pp.QueryRGB(df.image) && pp.QueryDepthMap(depthmap)) {

				/*
				for (int x = 0; x < csize[0]; x++) { 
					for (int y = 0; y < csize[1]; y++) { 
						df.image.setRGB(x,y,0);
					}
				}
				*/

				for (int xy=0;xy<p3.length;xy++) { 
					p3[xy].z=(float)depthmap[xy];
					if (p3[xy].z > 50 & p3[xy].z < 1000) {
						df.image.setRGB((int) p3[xy].x, (int) p3[xy].y, 0xff0000 + (int)(255*(p3[xy].z / 1000.0)));
					}
				}

				
				if (pp.MapDepthToColorCoordinates(p3,p2)) {
					for (int xy=0;xy<p2.length;xy++) {
						if (depthmap[xy]==untrusted[0] || depthmap[xy]==untrusted[1]) continue;
						int x1=(int)p2[xy].x, y1=(int)p2[xy].y;
						if (x1<0 || x1>=csize[0] || y1<0 || y1>=csize[1]) continue;
						float z1 = p3[xy].z;
						if (z1 > 1000) continue;
						int g = (int)(255*(z1 / 1000.0));
						df.image.setRGB(x1,y1,(int) g);
					}
				}
				
				df.repaint(); 
			}
			pp.ReleaseFrame();
		}

		pp.Close();
		System.exit(0);
    } 
}

class Listener extends WindowAdapter {
	public boolean exit=false;
	@Override public void windowClosing(WindowEvent e) {
		exit=true;
	}
}

class DrawFrame extends Component { 
    public BufferedImage image; 

    public DrawFrame(int width, int height) { 
        image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    } 
  
    public void paint(Graphics g) { 
        ((Graphics2D)g).drawImage(image,0,0,null); 
    }
}
  
