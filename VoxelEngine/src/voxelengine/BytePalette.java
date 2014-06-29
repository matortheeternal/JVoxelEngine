package voxelengine;

public class BytePalette {
	private String name;
	private byte[] palette;
	
	// constructor
	public BytePalette(String name, byte[] palette) {
		super();
		this.name = name;
		this.palette = palette;
	}
	
	// methods
	public byte get(int index) {
		return palette[index];
	}
	public String getName() {
		return name;
	}
	public void printContents() {
		for (int i = 0; i < palette.length; i++)
			System.out.println(get(i));
	}
}
