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
			System.out.print(get(i)+",");
		System.out.println();
	}
	public BytePalette remap(int size) {
		byte[] a = new byte[size];
		for (int i = 0; i < size; i++) {
			int n = (getSize() - 1) * (i+1)/size;
			a[i] = palette[n];
		}
		BytePalette p = new BytePalette(name+"-remapped", a);
		return p;
	}
	public int getSize() {
		return palette.length;
	}
}
