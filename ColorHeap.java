/*   ColorHeap is a min-heap implementation for ColorPairs with a section
 *   of elements at the top of the heap that only have one child. The rest 
 *   of the heap is binary. It allows easy access to the top n elements. 
 *   Additionally, the method removeAllContaining(int color) provides a fast 
 *   way to remove ColorPairs with the given color.
 */

 import java.util.*;
 
public class ColorHeap {
	private ArrayList<ColorPair> heap;
	private int rootIndex;
	
	public ColorHeap(int numAtTop, int initialCapacity) {
		heap = new ArrayList<ColorPair>(initialCapacity);
		rootIndex = numAtTop - 1;
	}
	
	public void add(ColorPair cp) {
		heap.add(cp);
	}
	
	public void heapify() {
		for (int i = rootIndex + (heap.size() - rootIndex - 2)/2; i >= 0; i--) {
			this.bubbleDown(i);
		}
	}

	/* When this ColorHeap is already heapified this function
	 * adds the element preserving the ColorHeap properties.
	 */
	public void heapAdd(ColorPair cp) {
		int index = heap.size();
		heap.add(cp);
		this.bubbleUp(index);
	}
	
	/* This function moves the element at the given index up the
	 * heap until it is in place (ie, "greater than" its parent).
	 */
	private void bubbleUp(int index) {
		boolean placed = index == 0 ? true : false;
		while (!placed) {
			int compareIndex;
			if (index > rootIndex) {
				compareIndex = rootIndex + (index - rootIndex - 1)/2;
			} else {
				compareIndex = index - 1;
			}
			if (heap.get(index).compareTo(heap.get(compareIndex)) < 0) {
				swap(index, compareIndex);
				index = compareIndex;
			} else {
				placed = true;
			}	
		}
	} 
	
	/* This function moves the element at the given index down the
	 * heap until it is in place.
	 */
	private void bubbleDown(int index) {
		boolean placed = false;
		int left;
		int right;
		int swapIndex;
		while (!placed) {
			if (index < rootIndex) {
				swapIndex = index + 1;
				if (heap.get(index).compareTo(heap.get(swapIndex)) > 0) {
					swap(index, swapIndex);
					index = swapIndex;
				} else {
					placed = true;
				}
			} else {
				left = 2*index - rootIndex + 1;
				right = left + 1;
				swapIndex = index;
				if (left < heap.size() && heap.get(left).compareTo(heap.get(swapIndex)) < 0) {
					swapIndex = left;
				} 
				if (right < heap.size() && heap.get(right).compareTo(heap.get(swapIndex)) < 0) {
					swapIndex = right;
				}
				if (swapIndex != index) {
					swap(index, swapIndex);
					index = swapIndex;
				} else {
					placed = true;
				}
			}
		}
	}
	
	/* This function swaps the ColorPairs at index1 and index2.
	 */
	private void swap(int index1, int index2) {
		ColorPair cp1 = heap.get(index1);
		heap.set(index1, heap.get(index2));
		heap.set(index2, cp1);
	}
	
	public void removeAllContaining(int color) {
		for (int i = heap.size() - 1; i >= 0; i--) {
			if (heap.get(i).colorLesser == color || heap.get(i).colorGreater == color) {
				this.removeAtIndex(i);
			}
		}
	}
	
	public void removeAtIndex(int index) {
		int lastIndex = heap.size() - 1;
		heap.set(index, heap.get(lastIndex));
		this.bubbleDown(index);
		heap.remove(lastIndex);
	}
	
	
}