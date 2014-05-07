package voxelengine;

import java.awt.Color;
import java.awt.Image;

public class BlockType {
	private Image texture;
	private String name;
	private String id;
	private Color color;
	
	// constructor
	public BlockType(Image texture, String name, String id, Color color) {
		super();
		this.texture = texture;
		this.name = name;
		this.id = id;
		this.color = color;
	}
	
	// getters
	public Image getTexture() {
		return texture;
	}
	public String getName() {
		return name;
	}
	public String getId() {
		return id;
	}
	public Color getColor() {
		return color;
	}
}
