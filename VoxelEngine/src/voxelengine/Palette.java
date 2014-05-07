package voxelengine;

public class Palette {
	private String name;
	private String[] palette;
	
	// constructor
	public Palette(String name, String[] palette) {
		super();
		this.name = name;
		this.palette = palette;
	}
	
	// methods
	public String get(int index) {
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
