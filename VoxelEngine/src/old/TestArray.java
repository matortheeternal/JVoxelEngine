package old;

public class TestArray {
	private int size = 100;
	private int[][][] array = new int[size][size][size];
	
	public TestArray() {
		long time = System.nanoTime();
		initializeArray();
		System.out.println("Initialization: "+(System.nanoTime() - time)/1000);
		if (size <= 5) printArray();
		time = System.nanoTime();
		shiftArray(1, 0, 0);
		System.out.println("Move with fors: "+(System.nanoTime() - time)/1000);
		if (size <= 5) printArray();
		
		initializeArray();
		if (size <= 5) printArray();
		time = System.nanoTime();
		shiftArraySys(1, 0, 0);
		System.out.println("Move with sys: "+(System.nanoTime() - time)/1000);
		if (size <= 5) printArray();
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		TestArray t = new TestArray();
	}
	
	private void initializeArray() {
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				for (int z = 0; z < size; z++) {
					array[x][y][z] = x + y + z;
				}
	}
	
	private void shiftArraySys(int xshift, int yshift, int zshift) {
		int[][][] newarray = array.clone();
		
		// xshift
		if (xshift > 0) {
			System.arraycopy(newarray, 0, array, xshift, array.length - xshift);
		} else if (xshift < 0) {
			System.arraycopy(newarray, xshift, array, 0, array.length + xshift);
		}
		
		// yshift
		if (yshift > 0) {
			for (int x = 0; x < size; x++) {
				System.arraycopy(newarray[x], 0, array[x], yshift, array[x].length - yshift);
			}
		} else if (yshift < 0) {
			for (int x = 0; x < size; x++) {
				System.arraycopy(newarray[x], yshift, array[x], 0, array[x].length + yshift);
			}
		}
		
		// zshift
		if (zshift > 0) {
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					System.arraycopy(newarray[x][y], 0, array[x][y], zshift, array[x][y].length - zshift);
				}
			}
		} else if (yshift < 0) {
			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					System.arraycopy(newarray[x][y], zshift, array[x][y], 0, array[x][y].length + zshift);
				}
			}
		}
	}
	
	private void zeroArraySys(int xshift, int yshift, int zshift) {
		if (xshift > 0) {
			for (int x = 0; x < xshift; x++)
				for (int y = 0; y < size; y++)
					for (int z = 0; z < size; z++) {
						System.out.println("Zeroing "+x+","+y+","+z);
						array[x][y][z] = 0;
					}
		} else if (xshift < 0) {
			for (int x = size - 1; x >= size - xshift; x--)
				for (int y = 0; y < size; y++)
					for (int z = 0; z < size; z++) {
						array[x][y][z] = 0;
					}
		}
		
		if (yshift > 0) {
			for (int y = 0; y < yshift; y++)
				for (int x = 0; x < size; x++)
					for (int z = 0; z < size; z++) {
						array[x][y][z] = 0;
					}
		} else if (yshift < 0) {
			for (int y = size - 1; y >= size - yshift; y--)
				for (int x = 0; x < size; x++)
					for (int z = 0; z < size; z++) {
						array[x][y][z] = 0;
					}
		}
		
		if (zshift > 0) {
			for (int z = 0; z < zshift; z++)
				for (int y = 0; y < size; y++)
					for (int x = 0; x < size; x++) {
						array[x][y][z] = 0;
					}
		} else if (zshift < 0) {
			for (int z = size - 1; z >= size - zshift; z--)
				for (int y = 0; y < size; y++)
					for (int x = 0; x < size; x++) {
						array[x][y][z] = 0;
					}
		}
		
	}

	private void shiftArray(int xshift, int yshift, int zshift) {
		// xshift
		if (xshift > 0) {
			// positive shift on x
			for (int x = size - 1; x >= 0; x--)
				for (int y = 0; y < size; y++) 
					for (int z = 0; z < size; z++) {
						if (x < xshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x - xshift][y][z];
					}
		} else if (xshift < 0) {
			// negative shift on x
			for (int x = 0; x < size; x++)
				for (int y = 0; y < size; y++) 
					for (int z = 0; z < size; z++) {
						if (x >= size + xshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x - xshift][y][z];
					}
		}
		
		// yshift
		if (yshift > 0) {
			// positive shift on y
			for (int y = size - 1; y >= 0; y--)
				for (int x = 0; x < size; x++)  
					for (int z = 0; z < size; z++) {
						if (y < yshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x][y - yshift][z];
					}
		} else if (yshift < 0) {
			// negative shift on y
			for (int y = 0; y < size; y++)
				for (int x = 0; x < size; x++) 
					for (int z = 0; z < size; z++) {
						if (y >= size + yshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x][y - yshift][z];
					}
		}
		
		// zshift
		if (zshift > 0) {
			// positive shift on z
			for (int z = size - 1; z >= 0; z--)
				for (int x = 0; x < size; x++)  
					for (int y = 0; y < size; y++) {
						if (z < zshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x][y][z - zshift];
					}
		} else if (zshift < 0) {
			// negative shift on z
			for (int z = 0; z < size; z++)
				for (int x = 0; x < size; x++) 
					for (int y = 0; y < size; y++) {
						if (z >= size + zshift)
							array[x][y][z] = 0;
						else
							array[x][y][z] = array[x][y][z - zshift];
					}
		}
		
		
	}
	
	private void printArray() {
		for (int x = 0; x < size; x++) {
			System.out.println("Plane ["+x+"]");
			for (int y = 0; y < size; y++) {
				for (int z = 0; z < size; z++) {
					System.out.print(array[x][y][z]+",");
				}
				System.out.println();
			}
		}
		System.out.println();
		System.out.println();
	}
}
