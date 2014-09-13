package voxelengine;

import java.io.Serializable;

public class Chunk implements Serializable {
	private static final long serialVersionUID = -4256447442964188571L;
	private int sz;
	private byte[][][] blocks;
	
	// constructor
	public Chunk(int sz) {
		this.sz = sz;
		this.blocks = new byte[sz][sz][sz];
	}
	public Chunk(int sz, byte[][][] blocks) {
		this.sz = sz;
		this.blocks = blocks;
	}
	
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
	public Chunk clone() {
		return new Chunk(this.sz, this.blocks);
	}
	public int getSize() {
		return sz;
	}
}
