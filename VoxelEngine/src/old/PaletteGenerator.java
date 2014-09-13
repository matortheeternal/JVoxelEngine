package old;

import voxelengine.BytePalette;

public class PaletteGenerator {
	private static final int low = 1;
	private static final int high = 32;
	private static final int num = 10;
	private static final int size = 32;
	
	public static void main(String[] args) {
		PaletteGenerator p = new PaletteGenerator();
	}
	
	public PaletteGenerator() {
		for (int i = 0; i < num; i++) {
			System.out.print("palettes.add(new BytePalette(\"r"+(i+1)+"\", new byte[] ");
			generate();
			System.out.print("));");
			System.out.println();
		}
	}

	private void generate() {
		System.out.print("{ ");
		for (int i = 0; i < size; i++) {
			int r = (int) (Math.random() * high + low);
			System.out.print(r);
			if (i != size - 1)
				System.out.print(", ");
		}
		System.out.print(" }");
	}
}
