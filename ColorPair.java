

	public class ColorPair implements Comparable<ColorPair>{
		public int colorLesser;
		public int colorGreater;
		public float similarity;
		
		public ColorPair(int color1, int color2) {
			colorLesser = color1 > color2 ? color2 : color1;
			colorGreater = colorLesser == color2 ? color1 : color2;
			similarity = PatternMaker.sim2(colorLesser, colorGreater);
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