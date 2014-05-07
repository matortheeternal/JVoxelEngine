package voxelengine;

public class Block {
	private BlockType type;
	
	// constructor
	public Block(BlockType type) {
		super();
		this.type = type;
	}
	
	// getters
	public BlockType getType() {
		return type;
	}
}
