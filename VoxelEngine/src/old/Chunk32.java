package old;

import java.io.Serializable;

public class Chunk32 implements Serializable {
	private static final long serialVersionUID = -4256447442964188571L;
	private static int sz = 32;
	private byte[][][] blocks = new byte[sz][sz][sz];
	private int x, y, z = 0;
	
	// methods
	public void setBlock(byte type, int x, int y, int z) {
		if ((x >= 0) && (y >= 0) && (z >= 0) && (x < sz) && (y < sz) && (z < sz)) 
			blocks[x][y][z] = type;
	}
	public byte getBlock(int x, int y, int z) {
		if ((x >= 0) && (y >= 0) && (z >= 0) && (x < sz) && (y < sz) && (z < sz)) 
			return blocks[x][y][z];
		else
			return 0;
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
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getZ() {
		return z;
	}
	public void setZ(int z) {
		this.z = z;
	}
}
