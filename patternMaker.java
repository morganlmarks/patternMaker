
import java.io.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.lang.*;
import java.util.*;


public class patternMaker {

	public class ColorPair implements Comparable<ColorPair>{
		public int colorLesser;
		public int colorGreater;
		public float similarity;
		
		public ColorPair(int color1, int color2) {
			colorLesser = color1 > color2 ? color2 : color1;
			colorGreater = colorLesser == color2 ? color1 : color2;
			similarity = sim2(colorLesser, colorGreater);
		}
		
		public int compareTo(ColorPair compairPair) {
			if (compairPair.similarity < this.similarity) {
				return -1;
			} else if (compairPair.similarity > this.similarity) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	

	private BufferedImage origImg;
	private int numStrings;
	private int imgWidth;
	private int imgHeight;
	private int pixelWidth;
	private int gridWidth;
	private int gridHeight;
	private int[][] colorGrid;
	private HashSet<Integer> dmcColors;
	private BufferedImage pixImg;
	private BufferedImage clusterImg;
	private BufferedImage dmcClusteredImg;
	private HashMap<Integer, Set<Integer>> colorMap;
	private HashMap<Integer, Integer> colorCount;
	private HashMap<Integer, Integer> dmcMap;
	
	
	public patternMaker(BufferedImage img, int num)
	{
		origImg = img;
		numStrings = num;
		imgWidth = img.getWidth();
		imgHeight = img.getHeight();
		
		pixelWidth = (int) (imgWidth*2f/numStrings);
		pixelWidth = (pixelWidth % 2) == 1 ? pixelWidth : pixelWidth - 1; // Make sure pixel width is odd
		gridWidth = 2*(imgWidth/pixelWidth) - 1;
		gridHeight = imgHeight/pixelWidth - 1;
		colorGrid = new int[gridHeight][gridWidth];
		
		colorMap = new HashMap<Integer, Set<Integer>>();
		colorCount = new HashMap<Integer, Integer>();
	}
	
	
	public void pixelate()
	{
		int halfPW = pixelWidth/2;
		for (int a= 0; a < gridWidth; a++)
		{
			for (int b = 0; b < gridHeight; b++)
			{
				int rsum = 0;
				int gsum = 0;
				int bsum = 0;
				int incrX = (a%2) == 1 ? 0 : 1;
				int xStart = (a/2)*pixelWidth + (a%2)*(halfPW + 1); 
				int yStart = halfPW + pixelWidth*b + (a%2)*(halfPW);
				for (int i = 0; i < pixelWidth; i++)
				{
					for (int j = 0; j < halfPW + incrX; j++)
					{
						int x = xStart + j;
						int y = yStart - j;
						int rgb = origImg.getRGB(x, y);
						rsum += (rgb >> 16) & 0xFF;
						gsum += (rgb >> 8) & 0xFF;
						bsum += rgb & 0xFF;						
					}
					xStart = xStart + incrX;
					yStart = incrX == 1 ? yStart : yStart + 1;
					incrX = incrX == 1 ? 0 : 1;
				}
				int numPixels = (a%2) == 1 ? 2*(halfPW)*(halfPW +1) : halfPW*halfPW + (halfPW+1)*(halfPW+1);
				int ravg = rsum/(numPixels);
				int gavg = gsum/(numPixels);
				int bavg = bsum/(numPixels);
				int rgbavg = ravg;
				rgbavg = (rgbavg << 8) + gavg;
				rgbavg = (rgbavg << 8) + bavg;
				colorGrid[b][a] = rgbavg;
				
				Integer rgbAvg = new Integer(rgbavg);
				int count = 0;
				if (!colorCount.containsKey(rgbAvg)) {
					Set<Integer> tempSet = new HashSet<Integer>();
					tempSet.add(rgbAvg);
					colorMap.put(rgbAvg, tempSet);
				} else {
					count = colorCount.get(rgbAvg);
				}
				colorCount.put(rgbAvg, count + 1);
			}
		}
	}
	
	public void writeImage(String fileName)
	{
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		int halfPW = pixelWidth/2;
		for (int a= 0; a < gridWidth; a++)
		{
			for (int b = 0; b < gridHeight; b++)
			{
				int rgb = colorGrid[b][a];
				int incrX = (a%2) == 1 ? 0 : 1;
				int xStart = (a/2)*pixelWidth + (a%2)*(halfPW + 1); 
				int yStart = halfPW + pixelWidth*b + (a%2)*(halfPW);
				for (int i = 0; i < pixelWidth; i++)
				{
					for (int j = 0; j < halfPW + incrX; j++)
					{
						int x = xStart + j;
						int y = yStart - j;
						img.setRGB(x, y, rgb);
					}
					xStart = xStart + incrX;
					yStart = incrX == 1 ? yStart : yStart + 1;
					incrX = incrX == 1 ? 0 : 1;
				}
			}
		}
		File output = new File(fileName + ".png");
		try {
			ImageIO.write(img, "png", output);
		} catch (IOException e) {
			System.out.println(String.format("Error writing %s \nMessgage: %s", fileName, e.getMessage()));
		}			
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
		}
	}
	
	public void clusterColors2(int numColorsClustered, int numCandidates)
	{
		int numColors = colorCount.size();
		HashMap<Integer, ArrayList<ColorPair>> cpTable = new HashMap<Integer, ArrayList<ColorPair>>();
		PriorityQueue<ColorPair> cpHeap = new PriorityQueue<ColorPair>(numColors*numColors);
		Set<Integer> startColors = colorCount.keySet();
		Iterator<Integer> iterColors1 = startColors.iterator();
		while (iterColors1.hasNext())
		{
			int color1 = iterColors1.next();
			Iterator<Integer> iterColors2 = startColors.iterator();
			while (iterColors2.hasNext())
			{
				int color2 = iterColors2.next();
				if (color1 < color2)
				{
					ColorPair cp = new ColorPair(color1, color2);
					ArrayList<ColorPair> list1 = cpTable.containsKey(color1) ? cpTable.get(color1) : new ArrayList<ColorPair>(numColors);
					ArrayList<ColorPair> list2 = cpTable.containsKey(color2) ? cpTable.get(color2) : new ArrayList<ColorPair>(numColors);
					list1.add(cp);
					list2.add(cp);
					cpTable.put(color1, list1);
					cpTable.put(color2, list2);
					cpHeap.add(cp);
				}
			}
		}
		while (numColors > numColorsClustered)
		{
			//int NUM_CANDIDATES = numColors/(4* numColorsClustered) + 2;
			int NUM_CANDIDATES = numCandidates;
			ArrayList<ColorPair> mergeCandidates = new ArrayList<ColorPair>(NUM_CANDIDATES);
			float bestRatio = 100;
			int bestIndex = 0;
			for (int i=0; i < NUM_CANDIDATES; i++)
			{
				ColorPair candidate = cpHeap.poll();
				int numStrings1 = calculateNumStrings(candidate.colorLesser);
				int numStrings2 = calculateNumStrings(candidate.colorGreater);
				int numStringsMerged = calculateNumStrings(candidate);
				float stringRatio = (float)numStringsMerged/(numStrings1+numStrings2);
				if (stringRatio < bestRatio)
				{
					bestRatio=stringRatio;
					bestIndex=i;
				}
				mergeCandidates.add(candidate);
			}
			ColorPair cpToMerge = mergeCandidates.remove(bestIndex);
			for (int i=0; i < NUM_CANDIDATES - 1; i++)
			{
				cpHeap.add(mergeCandidates.get(i));
			}
			int count1 = colorCount.get(cpToMerge.colorLesser);
			int count2 = colorCount.get(cpToMerge.colorGreater);
			int newColor = averageColors(cpToMerge.colorLesser, cpToMerge.colorGreater, count1, count2);
			boolean alreadyExisted = colorCount.containsKey(newColor);
			int maybeCount = alreadyExisted ? colorCount.get(newColor) : 0;
			colorCount.remove(cpToMerge.colorLesser);
			colorCount.remove(cpToMerge.colorGreater);
			ArrayList<ColorPair> lesserList = cpTable.get(cpToMerge.colorLesser);
			ArrayList<ColorPair> greaterList = cpTable.get(cpToMerge.colorGreater);
			

			if (cpToMerge.colorLesser != newColor)
			{
				for (int i=0; i < lesserList.size();i++)
				{
					ColorPair toRemove = lesserList.get(i);
					cpHeap.remove(toRemove);
				}
				cpTable.remove(cpToMerge.colorLesser);
			}
			if (cpToMerge.colorGreater != newColor)
			{
				for (int i=0; i< greaterList.size();i++)
				{
					ColorPair toRemove = greaterList.get(i);
					cpHeap.remove(toRemove);
				}
				cpTable.remove(cpToMerge.colorGreater);
			}
			
			colorCount.put(newColor, count1 + count2 + maybeCount);
			numColors = colorCount.size();			
			Set<Integer> curColors = colorCount.keySet();
			Iterator<Integer> iterColors = curColors.iterator();
			if (!alreadyExisted)
			{
				ArrayList<ColorPair> newColorPairs = new ArrayList<ColorPair>(numColors);
				while (iterColors.hasNext())
				{
					int color = iterColors.next();
					if (color != newColor)
					{
						ColorPair cp = new ColorPair(color, newColor);
						ArrayList<ColorPair> oldList = cpTable.get(color);
						oldList.add(cp);
						cpTable.put(color, oldList);
						newColorPairs.add(cp);
						cpHeap.add(cp);
					}
				}
				cpTable.put(newColor, newColorPairs);
			}
			for (int x=0; x < gridWidth; x++)
			{
				for (int y=0; y < gridHeight; y++)
				{
					if (colorGrid[y][x] == cpToMerge.colorLesser || colorGrid[y][x] == cpToMerge.colorGreater)
					{
						colorGrid[y][x] = newColor;
					}
				}
			}	
			//this.writeImage(numColors + "colors");
		}
	}
	
	
	public void updateColorGrid()
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
				colorFun.put(orig, color);
			}
		}
		for (int x=0; x < gridWidth; x++)
		{
			for (int y=0;y < gridHeight; y++)
			{
				int rgbOld = colorGrid[y][x];
				int rgbNew = colorFun.containsKey(rgbOld) ? colorFun.get(rgbOld) : 16711680;
				colorGrid[y][x] = rgbNew;
			}
		}
	}
	
	public int getTotalNumStrings() {
		int sum = 0;
		Set<Integer> curColors = colorCount.keySet();
		Iterator<Integer> iterColors = curColors.iterator();
		while (iterColors.hasNext())
		{
			int color = iterColors.next();
			sum = sum + calculateNumStrings(color);
			/*
			StringBuffer printArray = new StringBuffer(gridHeight*gridWidth*2);
			for (int i=0; i< gridHeight*2; i++)
			{
				printArray.append(curColorGrid[i][0]);
				for (int j=1; j< gridWidth; j++)
				{
					printArray.append("\t");
					printArray.append(curColorGrid[i][j]);
				}
				printArray.append("\n");
			}
			System.out.println(printArray.toString());
			*/
		}
		System.out.println(String.format("%d strings total are needed", sum));
		return sum;
	}
	
	public int calculateNumStrings(int color)
	{
		int[][] curColorGrid = new int[gridHeight*2][gridWidth];
		for (int i=0; i< gridHeight*2;i++)
		{
			Arrays.fill(curColorGrid[i], -1);
		}
		for (int x=0; x < gridWidth; x++)
		{
			for (int y=0; y < gridHeight; y++)
			{
				if (colorGrid[y][x] == color)
				{
					curColorGrid[y*2 + (x%2)][x] = 0;
				}
			}
		}		
		int highest = 0;
		for (int x=1; x < gridWidth; x++)
		{
			for (int y=gridHeight*2 - 1; y >= 0; y--)
			{
				if (curColorGrid[y][x] != -1)
				{
					int i = -1;
					boolean placeFound;
					do {
						placeFound = true;
						i++;
						for (int j = Math.max(0,y - x + i); j < Math.min(gridHeight*2 - 1, y + x - i); j++)
						{
							if (curColorGrid[j][i] != -1)
							{
								if ((Math.abs(y-j)+1)/(Math.abs(x-curColorGrid[j][i])+1) == 0)
								{
									placeFound = false;
								}
							}
						}
					} while (!placeFound);
					curColorGrid[y][i] = x;
					curColorGrid[y][x] = -1;
					highest = Math.max(highest, i);
				}
			}
		}
		return highest + 1;
	}
	
	public int calculateNumStrings(ColorPair cp)
	{
		int[][] curColorGrid = new int[gridHeight*2][gridWidth];
		for (int i=0; i< gridHeight*2;i++)
		{
			Arrays.fill(curColorGrid[i], -1);
		}
		for (int x=0; x < gridWidth; x++)
		{
			for (int y=0; y < gridHeight; y++)
			{
				if (colorGrid[y][x] == cp.colorLesser || colorGrid[y][x] == cp.colorGreater)
				{
					curColorGrid[y*2 + (x%2)][x] = 0;
				}
			}
		}		
		int highest = 0;
		for (int x=1; x < gridWidth; x++)
		{
			for (int y=gridHeight*2 - 1; y >= 0; y--)
			{
				if (curColorGrid[y][x] != -1)
				{
					int i = -1;
					boolean placeFound;
					do {
						placeFound = true;
						i++;
						for (int j = Math.max(0,y - x + i); j < Math.min(gridHeight*2 - 1, y + x - i); j++)
						{
							if (curColorGrid[j][i] != -1)
							{
								if ((Math.abs(y-j)+1)/(Math.abs(x-curColorGrid[j][i])+1) == 0)
								{
									placeFound = false;
								}
							}
						}
					} while (!placeFound);
					curColorGrid[y][i] = x;
					curColorGrid[y][x] = -1;
					highest = Math.max(highest, i);
				}
			}
		}
		return highest + 1;
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
	
	public void writeDmcClusteredImg(int num1, int num2)
	{
		dmcMap = new HashMap<Integer, Integer>();
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
		float mag1Squared = r1*r1 + b1*b1 + g1*g1;
		float mag2Squared = r2*r2 + b2*b2 + g2*g2;
		float cosSimSquared = ((r1*r2 + b1*b2 + g1*g2)*(r1*r2 + b1*b2 + g1*g2)/(mag1Squared*mag2Squared));
		return cosSimSquared * ((float)Math.sqrt(Math.min(mag1Squared, mag2Squared)/Math.max(mag1Squared, mag2Squared)));
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
		int num3 = 7;
		try  {
			num2 = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println(String.format("Number of candidates set to %d.", num3));
		}
		
		BufferedImage img = null;
		try
		{
			File file = new File("C:\\Users\\Morgan\\Desktop\\pic.jpg");
			img = (BufferedImage)ImageIO.read(file);
		} catch (Exception e)
		{
			System.out.println("Could not load image");
		} 
		patternMaker pM = new patternMaker(img, num);
		pM.pixelate();
		System.out.println("Pixelating DONE");
		pM.writeImage("C:\\Users\\Morgan\\Desktop\\pix" + num);
		pM.clusterColors2(num2, num3);
		System.out.println("Clustering DONE");
		//pM.updateColorGrid();
		int totalStrings = pM.getTotalNumStrings();
		pM.writeImage(String.format("C:\\Users\\Morgan\\Desktop\\clustered%d_%d_%d_%d", num, num2, num3, totalStrings));
		//System.out.println("DONE");


	}

}