
import java.io.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.*;
import java.util.*;


public class patternMaker {

	private BufferedImage bimg;
	private int numStrings;
	private int width;
	private int height;
	private HashSet<Integer> dmcColors;
	private BufferedImage pixImg;
	private BufferedImage clusterImg;
	private BufferedImage dmcClusteredImg;
	private HashMap<Integer, Set<Integer>> colorMap;
	private HashMap<Integer, Integer> colorCount;
	private HashMap<Integer, Integer> dmcMap;
	
	
	public patternMaker(BufferedImage img, int num)
	{
		bimg = img;
		width = bimg.getWidth();
		height = bimg.getHeight();
		numStrings = num;
		colorMap = new HashMap<Integer, Set<Integer>>();
		colorCount = new HashMap<Integer, Integer>();
	}
	
	public void pixelate()
	{
		pixImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		int step = (int)(width*2f/numStrings);
		if ((step % 2) == 1) {
			step = step - 1;
		}
		int halfStep = step/2;
		int rsum;
		int gsum;
		int bsum;
		int rgb;
		int ravg;
		int bavg;
		int gavg;
		int rgbavg;
		int[] rgbArray;
		int x;
		int y;
		for (int a= 0; a < width - step; a=a+step)
		{
			for (int b= halfStep; b < height - halfStep; b=b+step)
			{
				rsum = 0;
				gsum = 0;
				bsum = 0;
				for (int i=0; i < halfStep; i++)
				{
					x = a + i;
					y = b + i;
					for (int j=0; j < halfStep; j++)
					{
						rgb = bimg.getRGB(x + j, y - j);
						rsum += (rgb >> 16) & 0xFF;
						gsum += (rgb >> 8) & 0xFF;
						bsum += rgb & 0xFF;
					}
				}
				
				for (int i=0; i < halfStep - 1; i++)
				{
					x = a + i + 1;
					y = b + i;
					for (int j=0; j < halfStep - 1; j++)
					{
						rgb = bimg.getRGB(x + j, y - j);
						rsum += (rgb >> 16) & 0xFF;
						gsum += (rgb >> 8) & 0xFF;
						bsum += rgb & 0xFF;
					}
				}
				int numPixels = 2*halfStep*halfStep - 2*halfStep + 1;
				ravg = rsum/(numPixels);
				gavg = gsum/(numPixels);
				bavg = bsum/(numPixels);
				rgbavg = ravg;
				rgbavg = (rgbavg << 8) + gavg;
				rgbavg = (rgbavg << 8) + bavg;
				Integer rgbAvg = new Integer(rgbavg);
				Set<Integer> tempSet = new HashSet<Integer>();
				tempSet.add(rgbAvg);
				colorMap.put(rgbAvg, tempSet);
				int count = colorCount.containsKey(rgbAvg) ? colorCount.get(rgbavg) : 0;
				colorCount.put(rgbAvg, count + 1);
				for (int i=0; i < halfStep; i++)
				{
					x = a + i;
					y = b - i;
					for (int j=0; j < halfStep; j++)
					{
						pixImg.setRGB(x + j, y + j, rgbavg);
					}
				}
				
				for (int i=0; i < halfStep - 1; i++)
				{
					x = a + i + 1;
					y = b - i;
					for (int j=0; j < halfStep - 1; j++)
					{
						pixImg.setRGB(x + j, y + j, rgbavg);
					}
				}
			}
		}
		
		for (int a= halfStep; a < width - step; a=a+step)
		{
			for (int b= step; b < height - step; b=b+step)
			{
				rsum = 0;
				gsum = 0;
				bsum = 0;
				for (int i=0; i < halfStep; i++)
				{
					x = a + i;
					y = b + i;
					for (int j=0; j < halfStep; j++)
					{
						rgb = bimg.getRGB(x + j, y - j);
						rsum += (rgb >> 16) & 0xFF;
						gsum += (rgb >> 8) & 0xFF;
						bsum += rgb & 0xFF;
					}
				}
				
				for (int i=0; i < halfStep - 1; i++)
				{
					x = a + i + 1;
					y = b + i;
					for (int j=0; j < halfStep - 1; j++)
					{
						rgb = bimg.getRGB(x + j, y - j);
						rsum += (rgb >> 16) & 0xFF;
						gsum += (rgb >> 8) & 0xFF;
						bsum += rgb & 0xFF;
					}
				}
				int numPixels = 2*halfStep*halfStep - 2*halfStep + 1;
				ravg = rsum/(numPixels);
				gavg = gsum/(numPixels);
				bavg = bsum/(numPixels);
				rgbavg = ravg;
				rgbavg = (rgbavg << 8) + gavg;
				rgbavg = (rgbavg << 8) + bavg;
				Integer rgbAvg = new Integer(rgbavg);
				Set<Integer> tempSet = new HashSet<Integer>();
				tempSet.add(rgbAvg);
				colorMap.put(rgbAvg, tempSet);
				int count = colorCount.containsKey(rgbAvg) ? colorCount.get(rgbavg) : 0;
				colorCount.put(rgbAvg, count + 1);
				for (int i=0; i < halfStep; i++)
				{
					x = a + i;
					y = b - i;
					for (int j=0; j < halfStep; j++)
					{
						pixImg.setRGB(x + j, y + j, rgbavg);
					}
				}
				
				for (int i=0; i < halfStep - 1; i++)
				{
					x = a + i + 1;
					y = b - i;
					for (int j=0; j < halfStep - 1; j++)
					{
						pixImg.setRGB(x + j, y + j, rgbavg);
					}
				}
			}
		}
		File output = new File("C:\\Users\\Morgan\\Desktop\\pix.png");
		try {
			ImageIO.write(pixImg, "png", output);
		} catch (IOException e) {
			// Do something
		}
	}
	

	private float sim1(int rgb1, int rgb2)
	{
		float r1 = (rgb1 >> 16) & 0xFF;
		float g1 = (rgb1 >> 8) & 0xFF;
		float b1 = rgb1 & 0xFF;
		float r2 = (rgb2 >> 16) & 0xFF;
		float g2 = (rgb2 >> 8) & 0xFF;
		float b2 = rgb2 & 0xFF;
		float mag1 = (float)Math.sqrt(r1*r1 + b1*b1 + g1*g1);
		float mag2 = (float)Math.sqrt(r2*r2 + b2*b2 + g2*g2);
		return ((r1*r2 + b1*b2 + g1*g2)/(mag1*mag2)) * ((float)Math.min(mag1, mag2)/Math.max(mag1, mag2));
	}
	
		private float sim2(int rgb1, int rgb2)
	{
		float r1 = (rgb1 >> 16) & 0xFF;
		float g1 = (rgb1 >> 8) & 0xFF;
		float b1 = rgb1 & 0xFF;
		float r2 = (rgb2 >> 16) & 0xFF;
		float g2 = (rgb2 >> 8) & 0xFF;
		float b2 = rgb2 & 0xFF;
		float mag1 = (float)Math.sqrt(r1*r1 + b1*b1 + g1*g1);
		float mag2 = (float)Math.sqrt(r2*r2 + b2*b2 + g2*g2);
		float cosSim = ((r1*r2 + b1*b2 + g1*g2)/(mag1*mag2));
		return cosSim * cosSim * ((float)Math.min(mag1, mag2)/Math.max(mag1, mag2));
	}
	
	
	public void clusterColors(int numColorsClustered)
	{
		int numColors = colorCount.size();
		while (numColors > numColorsClustered)
		{
			Set<Integer> curColors = colorCount.keySet();
			float bestSim = 0;
			int colorToMerge1 = 0;
			int colorToMerge2 = 0;
			int color1 = 0;
			int color2 = 0;
			
			Iterator<Integer> iterColors1 = curColors.iterator();
			while (iterColors1.hasNext())
			{
				color1 = iterColors1.next();
				Iterator<Integer> iterColors2 = curColors.iterator();
				while (iterColors2.hasNext()) 
				{
					color2 = iterColors2.next().intValue();
					if (color1 != color2)
					{
						float sim = sim2(color1, color2);
						if (sim >= bestSim)
						{
							bestSim = sim;
							colorToMerge1 = color1;
							colorToMerge2 = color2;
						}
					}
				}
			}
			//System.out.println(colorToMerge1 + " " + colorToMerge2 + " " + color1 + " " + color2);
			int count1 = colorCount.get(colorToMerge1);
			int count2 = colorCount.get(colorToMerge2);
			int newColor = averageColors(colorToMerge1, colorToMerge2, count1, count2);
			int maybeCount = colorCount.containsKey(newColor) ? colorCount.get(newColor) : 0;
			Set<Integer> orig1 = colorMap.get(colorToMerge1);
			Set<Integer> orig2 = colorMap.get(colorToMerge2);
			Set<Integer> newSet = new HashSet<Integer>();
			Set<Integer> maybeSet = colorMap.containsKey(newColor) ? colorMap.get(newColor) : new HashSet<Integer>();
			newSet.addAll(orig1);
			newSet.addAll(orig2);
			newSet.addAll(maybeSet);
			colorMap.remove(colorToMerge1);
			colorMap.remove(colorToMerge2);
			colorMap.put(newColor, newSet);
			colorCount.remove(colorToMerge1);
			colorCount.remove(colorToMerge2);
			colorCount.put(newColor, count1 + count2 + maybeCount);
			numColors = colorCount.size();
			//System.out.println(String.format("map %d		%d count",colorMap.size(), numColors));
		}
		
	}
	
	private void loadColors()
	{
		File file = new File("colors.txt");
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			dmcColors = new HashSet<Integer>();
			while ((line = in.readLine()) != null)
			{
				String[] splitLine = line.split("\t");
				String rString= splitLine[2];
				String gString = splitLine[3];
				String bString = splitLine[4];
				int r = Integer.decode(rString);
				int g = Integer.decode(gString);
				int b = Integer.decode(bString);
				int rgbInt = r;
				rgbInt = (rgbInt << 8) + g;
				rgbInt = (rgbInt << 8) + b;
				dmcColors.add(rgbInt);
			}
			
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public void writeClusteredImg(int num1, int num2)
	{
		HashMap<Integer, Integer> colorFun = new HashMap<Integer, Integer>();
		Set<Integer> mapSet = colorMap.keySet();
		Set<Integer> countSet = colorCount.keySet();
		Iterator<Integer> iter = mapSet.iterator();
		while (iter.hasNext())
		{
			int color = iter.next();
			Set<Integer> tempSet = colorMap.get(color);
			Iterator<Integer> iter2 = tempSet.iterator();
			while(iter2.hasNext())
			{
				int orig = iter2.next();
				//System.out.println(orig + " : " + color);
				colorFun.put(orig, color);
			}
		}
		colorFun.put(0, 0);
		clusterImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x=0; x < width; x++)
		{
			for (int y=0;y < height; y++)
			{
				int color1 = pixImg.getRGB(x,y);
				int r = (color1 >> 16) & 0xFF;
				int g = (color1 >> 8) & 0xFF;
				int b = color1 & 0xFF;
				color1 = r;
				color1 = (color1 << 8) + g;
				color1 = (color1 << 8) + b;
				int color2 =0;
				if (colorFun.containsKey(color1))
				{
					color2 = colorFun.get(color1);
				} else
				{
					//System.out.println(color1);
				}
				clusterImg.setRGB(x, y, color2);
			}
		}
		File output = new File(String.format("C:\\Users\\Morgan\\Desktop\\cluster%d_%d.png",num1,num2));
		try {
			ImageIO.write(clusterImg, "png", output);
		} catch (IOException e) {
			// Do something
		}
	}
	
	public void writeDmcClusteredImg(int num1, int num2)
	{
		dmcMap = new HashMap<Integer, Integer>();
		dmcClusteredImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Set<Integer> curColors = colorCount.keySet();
		Iterator<Integer> iterColors = curColors.iterator();
		while (iterColors.hasNext())
		{
			int color = iterColors.next();
			float bestSim = 0;
			int mostSimColor = 0;
			Iterator<Integer> iterDMC = dmcColors.iterator();
			while (iterDMC.hasNext())
			{
				int dmcColor = iterDMC.next();
				float sim = sim2(color, dmcColor);
				if (sim > bestSim)
				{
					bestSim = sim;
					mostSimColor = dmcColor;
				}
			}
			dmcMap.put(color, mostSimColor);
		}
		dmcMap.put(0,0);
		for (int x=0; x < width; x++)
		{
			for (int y=0;y < height; y++)
			{
				int color1 = clusterImg.getRGB(x,y);
				int r = (color1 >> 16) & 0xFF;
				int g = (color1 >> 8) & 0xFF;
				int b = color1 & 0xFF;
				color1 = r;
				color1 = (color1 << 8) + g;
				color1 = (color1 << 8) + b;
				int color2 =0;
				if (dmcMap.containsKey(color1))
				{
					color2 = dmcMap.get(color1);
				} else
				{
					//System.out.println(color1);
				}
				dmcClusteredImg.setRGB(x, y, color2);
			}
		}
		File output = new File(String.format("C:\\Users\\Morgan\\Desktop\\dmcCluster%d_%d.png",num1,num2));
		try {
			ImageIO.write(dmcClusteredImg, "png", output);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	private int averageColors(int rgb1, int rgb2, int count1, int count2)
	{
		int r1 = (rgb1 >> 16) & 0xFF;
		int g1 = (rgb1 >> 8) & 0xFF;
		int b1 = rgb1 & 0xFF;
		int r2 = (rgb2 >> 16) & 0xFF;
		int g2 = (rgb2 >> 8) & 0xFF;
		int b2 = rgb2 & 0xFF;
		int ravg = (r1*count1 + r2*count2)/(count1 + count2);
		int gavg = (g1*count1 + g2*count2)/(count1 + count2);
		int bavg = (b1*count1 + b2*count2)/(count1 + count2);
		int rgbavg = ravg;
		rgbavg = (rgbavg << 8) + gavg;
		rgbavg = (rgbavg << 8) + bavg;
		return rgbavg;
	}
	
	
	public static void main(String[] args)
	{
		int num = 40;
		try  {
			num = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Number of strings set to 40.");
		}
		int num2 = num/2;
		try  {
			num2 = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(String.format("Number of colors set to %d.", num2));
		}
		
		BufferedImage img = null;
		try
		{
			File file = new File("C:\\Users\\Morgan\\Desktop\\pic.jpg");
			img = (BufferedImage)ImageIO.read(file);
		} catch (Exception e)
		{
			System.out.println("Something didn't work");
		} 
		patternMaker pM = new patternMaker(img, num);
		pM.pixelate();
		System.out.println("Pixelation DONE");
		pM.clusterColors(num2);
		System.out.println("Clustering DONE");
		pM.writeClusteredImg(num, num2);
		System.out.println("DONE");
		pM.loadColors();
		pM.writeDmcClusteredImg(num, num2);


	}

}