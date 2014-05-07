package voxelengine;

public class Chunk {
	private static int sz = 16;
	private Block[][][] blocks = new Block[sz][sz][sz];
	
	// methods
	public void setBlock(BlockType type, int x, int y, int z) {
		blocks[x][y][z] = new Block(type);
	}
	public Block getBlock(int x, int y, int z) {
		return blocks[x][y][z];
	}
	public boolean checkIfEmpty() {
		for (int x = 0; x < sz; x++) {
			for (int y = 0; y < sz; y++) {
				for (int z = 0; z < sz; z++) {
					if (blocks[x][y][z] != null) 
						return false;
				}
			}
		}
		return true;
	}
	
	// saving and loading
	public String saveChunk() {
		// data header
		String data = Integer.toString(sz) + "\n";
		
		// save data
		for (int x = 0; x < sz; x++) {
			for (int y = 0; y < sz; y++) {
				for (int z = 0; z < sz; z++) {
					if (blocks[x][y][z] != null)
						data += blocks[x][y][z].getType().getId();
					else
						data += "0";
				}
			}
		}
		
		return data;
	}
	public void loadChunk(String data, BlockLibrary lib) throws Exception {
		// first check if chunk sizes match
		int size = Integer.parseInt(data.substring(0, data.indexOf("\n") - 1));
		if (size != sz)
			throw new Exception();
		
		// then load data
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == '0')
				continue;
			// calculate current 3d location
			int z = (int) (i/(sz*sz));
			int y = (int) ((i - z)/sz);
			int x = i - z - y;
			blocks[x][y][z] = new Block(lib.getType(String.valueOf(data.charAt(i))));
		}
	}
}
