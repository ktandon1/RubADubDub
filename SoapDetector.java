import java.util.*;

public class SoapDetector
{
	public static final int nXnSize = 50;
	public static final double M = 1280;
	public static final double N = 720;


	public double[][] getDensityImage(ArrayList<Double> x, ArrayList<Double> y)
	{
			x.add(M);
			y.add(N);
			double[][] densityArray = Hist2D.hist(x,y,nXnSize,0);
			return densityArray;
	}



}