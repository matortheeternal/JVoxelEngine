package voxelengine;

import java.util.ArrayList;

public class BlockLibrary {
	private ArrayList<BlockType> library = new ArrayList<BlockType>();
	
	// methods
	public void addType(BlockType type) {
		library.add(type);
	}
	public BlockType getType(byte id) {
		for (int i = 0; i < library.size(); i++)
			if (library.get(i).getId() == (id))
				return library.get(i);
		
		// if not found, return null
		return null;
	}
	public BlockType getType(String string) {
		for (int i = 0; i < library.size(); i++)
			if (library.get(i).getName().equals(string))
				return library.get(i);
		
		// if not found, return null
		return null;
	}
}
