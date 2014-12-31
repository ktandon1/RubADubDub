import intel.pcsdk.*;
import java.io.*;
import java.util.*;

public class Remapper {

	//consts
	public static final int DEPTH_IMG_LENGTH = 320;
	public static final int DEPTH_IMG_WIDTH = 240;
	public static final int RGB_IMG_LENGTH = 1280;
	public static final int RGB_IMG_WIDTH = 720;

	protected static PXCMPoint3DF32[] makeDepthImgStruct(String file) throws IOException {

		//read file into structure
		List<Integer> xList = new ArrayList<Integer>();
		List<Integer> yList = new ArrayList<Integer>();
		List<Float> zList = new ArrayList<Float>();
		BufferedReader in  = new BufferedReader(new FileReader(file));
		String line = "";
		while((line=in.readLine())!=null) {
			String[] tok = line.split(",");
			int x = Integer.parseInt(tok[0]);
			int y = Integer.parseInt(tok[1]);
			float z = Float.parseFloat(tok[2]);
			xList.add(x);
			yList.add(y);
			zList.add(z);
		}

		//init structure
		PXCMPoint3DF32[] rtn = new PXCMPoint3DF32[DEPTH_IMG_LENGTH*DEPTH_IMG_WIDTH];
		Map<String,Integer> xyMap = new HashMap<String,Integer>();
		for(int x=0, xy=0; x < DEPTH_IMG_WIDTH; x++) {
			for(int y=0; y < DEPTH_IMG_LENGTH; y++, xy++) {
				rtn[xy] = new PXCMPoint3DF32(x,y,0);
				xyMap.put(x + "," + y, xy);
			}
		}

		//place values in structure
		for(int ind=0; ind < xList.size(); ind++) {
			String key = xList.get(ind) + "," + yList.get(ind);
			int xy = xyMap.get(key);
			rtn[xy].z = zList.get(ind);
//			System.out.println(rtn[xy].x + " " + rtn[xy].y + " " + rtn[xy].z);
		}

		return rtn;
	}

	protected static List<String> mapPoints(PXCUPipeline pp, PXCMPoint3DF32[] p3) {
		List<String> rtn = new ArrayList<String>();
		PXCMPointF32[] p2 = new PXCMPointF32[DEPTH_IMG_LENGTH*DEPTH_IMG_WIDTH];
		pp.MapDepthToColorCoordinates(p3,p2);
		for (int xy=0;xy<p2.length;xy++) {
			if(p2[xy]!=null) {
				int x1=(int)p2[xy].x; //x of RGB image
				int y1=(int)p2[xy].y; //y of RGB image
				if (x1<0 || x1>=RGB_IMG_LENGTH || y1<0 || y1>=RGB_IMG_LENGTH) continue;
				float z1 = (float) p3[xy].z; //depth of pixel (x,y) in RGB image
				String line = x1 + "," + y1 + "," + z1;
				rtn.add(line);
			}
		}
		return rtn;
	}

	protected static void writeFile(String file, List<String> fileLines) throws IOException{
		PrintWriter out = new PrintWriter(new FileWriter(file,true));
		for(String line : fileLines) {
			out.println(line);
		}
		out.close();
	}

	public static void remap(String IN_DIR, String OUT_DIR) {
		try {
			//init
			Remapper r = new Remapper();
			PXCUPipeline pp=new PXCUPipeline();
			pp.Init(PXCUPipeline.COLOR_WXGA|PXCUPipeline.GESTURE);
			int[] csize=new int[2];
			pp.QueryRGBSize(csize);
			int[] dsize=new int[2];
			pp.QueryDepthMapSize(dsize);
			System.out.println(csize[0] + " " + csize[1] + " " + dsize[0] + " " + dsize[1]);

			//create out file dir
			(new File(OUT_DIR)).mkdir();

			//get files in directory and iterate
			File[] files = (new File(FILE_DIR)).listFiles();
			for(File f : files) {
				try {
					System.out.println(f.getName());
					PXCMPoint3DF32[] p3 = r.makeDepthImgStruct(FILE_DIR + f.getName());
					List<String> fileLines = r.mapPoints(pp,p3);
					r.writeFile(OUT_DIR + "remapped_" + f.getName(), fileLines);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}