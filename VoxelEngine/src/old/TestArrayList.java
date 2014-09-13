package old;

import java.util.ArrayList;

public class TestArrayList {
	private int size = 100;
	private ArrayList<ArrayList<ArrayList<Integer>>> array = new ArrayList<ArrayList<ArrayList<Integer>>>(size);
	
	public static void main(String[] args) {
		TestArrayList t = new TestArrayList();
	}
	
	public TestArrayList() {
		Long time = System.nanoTime();
		initializeArray();
		System.out.println("Initialization: "+(System.nanoTime() - time)/1000);
		if (size <= 5) printArray();
		time = System.nanoTime();
		shiftArray(0, 0, 1);
		System.out.println("Move with fors: "+(System.nanoTime() - time)/1000);
		if (size <= 5) printArray();
	}

	private void shiftArray(int xshift, int yshift, int zshift) {
		if (xshift > 0 ) {
			for (int x = 0; x < xshift; x++) {
				array.remove(size - 1);
				array.add(0, new ArrayList<ArrayList<Integer>>(size));
				for (int y = 0; y < size; y++) {
					array.get(x).add(new ArrayList<Integer>(size));
					for (int z = 0; z < size; z++) {
						array.get(x).get(y).add(0);
					}
				}
			}
		} else if (xshift < 0) {
			for (int x = 0; x < xshift; x++) {
				array.remove(0);
				array.add(new ArrayList<ArrayList<Integer>>(size));
				for (int y = 0; y < size; y++) {
					array.get(size - 1).add(new ArrayList<Integer>(size));
					for (int z = 0; z < size; z++) {
						array.get(x).get(y).add(0);
					}
				}
			}
		}
		
		if (yshift > 0) {
			for (int x = 0; x < size; x++) 
				for (int y = 0; y < yshift; y++) {
					array.get(x).remove(size - 1);
					array.get(x).add(0, new ArrayList<Integer>(size));
					for (int z = 0; z < size; z++) {
						array.get(x).get(y).add(0);
					}
				}
		} else if (yshift < 0) {
			for (int x = 0; x < size; x++) 
				for (int y = 0; y < yshift; y++) {
					array.get(x).remove(0);
					array.get(x).add(new ArrayList<Integer>(size));
					for (int z = 0; z < size; z++) {
						array.get(x).get(size - 1).add(0);
					}
				}
		}
		
		if (zshift > 0) {
			for (int x = 0; x < size; x++) 
				for (int y = 0; y < size; y++) 
					for (int z = 0; z < zshift; z++) {
						array.get(x).get(y).remove(size - 1);
						array.get(x).get(y).add(0, 0);
					}
		} else if (zshift < 0) {
			for (int x = 0; x < size; x++) 
				for (int y = 0; y < size; y++) 
					for (int z = 0; z < zshift; z++) {
						array.get(x).get(y).remove(0);
						array.get(x).get(y).add(0);
					}
		}
	}

	private void printArray() {
		for (int x = 0; x < size; x++) {
			System.out.println("Plane ["+x+"]");
			for (int y = 0; y < size; y++) {
				for (int z = 0; z < size; z++) {
					System.out.print(array.get(x).get(y).get(z)+",");
				}
				System.out.println();
			}
		}
		System.out.println();
		System.out.println();
	}

	private void initializeArray() {
		for (int x = 0; x < size; x++) {
			array.add(new ArrayList<ArrayList<Integer>>(size));
			for (int y = 0; y < size; y++) {
				array.get(x).add(new ArrayList<Integer>(size));
				for (int z = 0; z < size; z++) {
					array.get(x).get(y).add(x + y + z);
				}
			}
		}
	}
}
