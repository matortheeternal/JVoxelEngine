package voxelengine;

import java.awt.Color;
import java.awt.Image;

public class BlockType {
	private Image texture;
	private String name;
	private byte id;
	private Color color;
	
	// constructor
	public BlockType(Image texture, String name, int id, Color color) {
		super();
		this.texture = texture;
		this.name = name;
		this.id = (byte) id;
		this.color = color;
	}
	
	// getters
	public Image getTexture() {
		return texture;
	}
	public String getName() {
		return name;
	}
	public byte getId() {
		return id;
	}
	public Color getColor() {
		return color;
	}
}
