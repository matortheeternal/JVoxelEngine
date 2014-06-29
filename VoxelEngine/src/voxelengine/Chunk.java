package voxelengine;

public class Chunk {
	private static int sz = 16;
	private byte[][][] blocks = new byte[sz][sz][sz];
	
	// methods
	public void setBlock(byte type, int x, int y, int z) {
		blocks[x][y][z] = type;
	}
	public byte getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}
	public boolean checkIfEmpty() {
		for (int x = 0; x < sz; x++) {
			for (int y = 0; y < sz; y++) {
				for (int z = 0; z < sz; z++) {
					if (blocks[x][y][z] != 0) 
						return false;
				}
			}
		}
		return true;
	}
}
