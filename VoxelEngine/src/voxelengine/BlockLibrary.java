package voxelengine;

import java.util.ArrayList;

public class BlockLibrary {
	private ArrayList<BlockType> library = new ArrayList<BlockType>();
	
	// methods
	public void addType(BlockType type) {
		library.add(type);
	}
	public BlockType getType(String id) {
		for (int i = 0; i < library.size(); i++)
			if ((library.get(i).getId().equals(id)) || (library.get(i).getName().equals(id)))
				return library.get(i);
		
		// if not found, return null
		return null;
	}
}
