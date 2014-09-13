package voxelengine;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import static java.lang.Math.*;

public class World {
	private static final int csz = 64; // chunk size
	private int dsize = 8; // decompressed chunk space size
	private int dmax = dsize/2; // maximum distance before decompressing additional chunks

	private int size;
	private byte[][][][] cchunks;
	private Chunk[][][] dchunks;
	private BlockLibrary library = new BlockLibrary();
	private ArrayList<BytePalette> palettes = new ArrayList<BytePalette>();
	private int xco = -100, yco = -100, zco = -100;
	private ObjectGenerator generator = new ObjectGenerator(this, csz);
	private Chunk emptyChunk = new Chunk(csz);
	private byte[] emptyChunkC = compressChunk(emptyChunk);

	public World(int size) {
		super();
		this.size = size;
		this.cchunks = new byte[size][size][size][];

		// set cchunks to compressed empty chunks
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				for (int z = 0; z < size; z++) {
					cchunks[x][y][z] = emptyChunkC.clone();
				}
	}


	/***************************************************************
	 ******************* System Variable Methods *******************
	 ***************************************************************/

	public void setdsize(int dsize) {
		this.dsize = dsize;
	}
	
	public void setdmax(int dmax) {
		this.dmax = dmax;
	}
	

	/***************************************************************
	 ********************* Chunk/Block Methods *********************
	 ***************************************************************/

	public Chunk getChunk(int x, int y, int z) {
		int xa = x - this.xco;
		int ya = y - this.yco;
		int za = z - this.zco;
		
		if ((xa >= 0) && (ya >= 0) && (za >= 0) && (xa < dsize) && (ya < dsize) && (za < dsize)) {
			return dchunks[xa][ya][za];
		} else {
			return emptyChunk;
		}
	}

	public byte getBlock(int x, int y, int z) {
		int xChunk = x / csz;
		int yChunk = y / csz;
		int zChunk = z / csz;
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		if (chunk != null)
			return chunk.getBlock(x % csz, y % csz, z % csz);
		else
			return 0;
	}

	public byte getBlock(double x, double y, double z) {
		int xi = (int) x;
		int yi = (int) y;
		int zi = (int) z;
		return getBlock(xi, yi, zi);
	}

	public byte[] compressChunk(Chunk c) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
			ObjectOutputStream out = new ObjectOutputStream(gzipOut);
			out.writeObject(c);
			out.close();
			return baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setChunk(int x, int y, int z, Chunk c) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
			ObjectOutputStream out = new ObjectOutputStream(gzipOut);
//			if (c.checkIfEmpty())
//				System.out.println("Chunk to be placed at ("+x+", "+y+", "+z+") is empty.");
			out.writeObject(c);
			out.close();
			cchunks[x][y][z] = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBlock(byte id, int x, int y, int z) {
		int xChunk = x / csz;
		int yChunk = y / csz;
		int zChunk = z / csz;
		manageChunks(xChunk, yChunk, zChunk, true);
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		chunk.setBlock(id, x % csz, y % csz, z % csz);
	}

	public void setBlock(byte id, double x, double y, double z) {
		int xi = (int) x;
		int yi = (int) y;
		int zi = (int) z;
		setBlock(id, xi, yi, zi);
	}

	public void manageChunks(int xc, int yc, int zc, boolean save) {
		int xco = xc - (dsize / 2);
		int yco = yc - (dsize / 2);
		int zco = zc - (dsize / 2);

		int xco_diff = this.xco - xco;
		int yco_diff = this.yco - yco;
		int zco_diff = this.zco - zco;

		if ((abs(xco_diff) <= dmax) && (abs(yco_diff) <= dmax) && (abs(zco_diff) <= dmax))
			return;

		// set world offsets
		this.xco = xco;
		this.yco = yco;
		this.zco = zco;

		// move or decompress chunks
		moveDecompressChunks(xco_diff, yco_diff, zco_diff, save);
	}

	private void moveDecompressChunks(int xshift, int yshift, int zshift, boolean save) {
		System.out.println("moveDecompressChunks("+xshift+", "+yshift+", "+zshift+")");
		
		// save chunks
		if (save) {
			for (int x = 0; x < dsize; x++)
				for (int y = 0; y < dsize; y++)
					for (int z = 0; z < dsize; z++) {
						if ((x + xco >= 0) && (y + yco >= 0) && (z + zco >= 0) && (x + xco < size) && (y + yco < size) && (z + zco < size))
							setChunk(this.xco + x, this.yco + y, this.zco + z, dchunks[x][y][z]);
					}
		}
		
		// x axis shift
		if (xshift > 0) {
			// positive shift on x-axis
			for (int x = dsize - 1; x >= 0; x--)
				for (int y = 0; y < dsize; y++)
					for (int z = 0; z < dsize; z++) {
						if (x < xshift) {
							// System.out.println("Decompressing chunk at "+(this.xco + x + xshift)+","+(this.yco + y)+","+(this.zco + z));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
							// System.out.println("Moving chunk from "+(x-xshift)+","+y+","+z+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x - xshift][y][z];
						}
					}
		} else if (xshift < 0) {
			// negative shift on x-axis
			for (int x = 0; x < dsize; x++)
				for (int y = 0; y < dsize; y++)
					for (int z = 0; z < dsize; z++) {
						if (x >= dsize + xshift) {
							// System.out.println("Decompressing chunk at "+(this.xco + x + xshift)+","+(this.yco + y)+","+(this.zco + z));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
							// System.out.println("Moving chunk from "+(x-xshift)+","+y+","+z+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x - xshift][y][z];
						}
					}
		}

		// y axis shift
		if (yshift > 0) {
			// positive shift on y-axis
			for (int y = dsize - 1; y >= 0; y--)
				for (int x = 0; x < dsize; x++)
					for (int z = 0; z < dsize; z++) {
						if (y < yshift) {
//							System.out.println("Decompressing chunk at "+(this.xco + x)+","+(this.yco + y + yshift)+","+(this.zco + z));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
//							System.out.println("Moving chunk from "+x+","+(y-yshift)+","+z+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x][y - yshift][z];
						}
					}
		} else if (yshift < 0) {
			// negative shift on y-axis
			for (int y = 0; y < dsize; y++)
				for (int x = 0; x < dsize; x++)
					for (int z = 0; z < dsize; z++) {
						if (y >= dsize + yshift) {
//							System.out.println("Decompressing chunk at "+(this.xco + x)+","+(this.yco + y + yshift)+","+(this.zco + z));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
//							System.out.println("Moving chunk from "+x+","+(y-yshift)+","+z+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x][y - yshift][z];
						}
					}
		}

		// z axis shift
		if (zshift > 0) {
			// positive shift on z-axis
			for (int z = dsize - 1; z >= 0; z--)
				for (int x = 0; x < dsize; x++)
					for (int y = 0; y < dsize; y++) {
						if (z < zshift) {
//							System.out.println("Decompressing chunk at "+(this.xco + x)+","+(this.yco + y)+","+(this.zco + z + zshift));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
//							System.out.println("Moving chunk from "+x+","+y+","+(z-zshift)+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x][y][z - zshift];
						}
					}
		} else if (zshift < 0) {
			// negative shift on z-axis
			for (int z = 0; z < dsize; z++)
				for (int x = 0; x < dsize; x++)
					for (int y = 0; y < dsize; y++) {
						if (z >= dsize + zshift) {
//							System.out.println("Decompressing chunk at "+(this.xco + x)+","+(this.yco + y)+","+(this.zco + z + zshift));
							dchunks[x][y][z] = decompressChunk(this.xco + x, this.yco + y, this.zco + z);
						} else {
//							System.out.println("Moving chunk from "+x+","+y+","+(z-zshift)+" to "+x+","+y+","+z);
							dchunks[x][y][z] = dchunks[x][y][z - zshift];
						}
					}
		}
	}

	private Chunk decompressChunk(int x, int y, int z) {
		Chunk c = emptyChunk;
		if ((x >= 0) && (x < size) && (y >= 0) && (y < size) && (z >= 0) && (z < size)) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(cchunks[x][y][z]);
				GZIPInputStream gzipIn = new GZIPInputStream(bais);
				ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
				c = (Chunk) objectIn.readObject();
				objectIn.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		return c;
	}
	
	public void decompressAllChunks(int xc, int yc, int zc) {
//		System.out.println("decompressAllChunks("+xc+","+yc+","+zc+")");
		int xco = xc - dsize / 2;
		int yco = yc - dsize / 2;
		int zco = zc - dsize / 2;

		int xco_diff = xco - this.xco;
		int yco_diff = yco - this.yco;
		int zco_diff = zco - this.zco;

		if ((xco_diff == 0) && (yco_diff == 0) && (zco_diff == 0))
			return;

		System.out.println("Decompressing all chunks...");
		for (int x = 0; x < dsize; x++)
			for (int y = 0; y < dsize; y++) 
				for (int z = 0; z < dsize; z++) {
					dchunks[x][y][z] = decompressChunk(x + xco, y + yco, z + zco);
				}

		// set world offsets
		this.xco = xco;
		this.yco = yco;
		this.zco = zco;
	}
	
	public void compressAll() {
		for (int x = 0; x < dsize; x++)
			for (int y = 0; y < dsize; y++) 
				for (int z = 0; z < dsize; z++) {
//					System.out.println("Compressing chunk "+x+","+y+","+z);
					if ((x + xco >= 0) && (y + yco >= 0) && (z + zco >= 0) && (x + xco < size) && (y + yco < size) && (z + zco < size))
						setChunk(this.xco + x, this.yco + y, this.zco + z, dchunks[x][y][z]);
				}
	}
	
	public void initializeDecompression() {
		dchunks = null;
		dchunks = new Chunk[dsize][dsize][dsize];
	}

	
	/***************************************************************
	 ********************** BlockType Methods **********************
	 ***************************************************************/

	public void addType(BlockType type) {
		library.addType(type);
	}

	public BlockType getType(byte id) {
		return library.getType(id);
	}

	public byte getByteFromName(String string) {
		return library.getType(string).getId();
	}


	/***************************************************************
	 ***************** Library and Palette Methods *****************
	 ***************************************************************/

	public void addColorBlockTypes() {
		library.addType(new BlockType(null, "red", 1, Color.RED));
		library.addType(new BlockType(null, "neonGreen", 2, Color.GREEN));
		library.addType(new BlockType(null, "blue", 3, Color.BLUE));
		library.addType(new BlockType(null, "yellow", 4, Color.YELLOW));
		library.addType(new BlockType(null, "gold", 5, Color.ORANGE));
		library.addType(new BlockType(null, "magenta", 6, Color.MAGENTA));
		library.addType(new BlockType(null, "pink", 7, Color.PINK));
		library.addType(new BlockType(null, "cyan", 8, Color.CYAN));
		library.addType(new BlockType(null, "black", 9, new Color(16, 16, 16)));
		library.addType(new BlockType(null, "white", 10, Color.WHITE));
		library.addType(new BlockType(null, "gray", 11, Color.GRAY));
		library.addType(new BlockType(null, "darkGray", 12, Color.DARK_GRAY));
		library.addType(new BlockType(null, "gloriousBlue", 13, new Color(7, 8, 114)));
		library.addType(new BlockType(null, "watermelon", 14, new Color(247, 70, 122)));
		library.addType(new BlockType(null, "violet", 15, new Color(73, 8, 162)));
		library.addType(new BlockType(null, "darkBlue", 16, new Color(14, 0, 42)));
		library.addType(new BlockType(null, "brownPurple", 17, new Color(40, 0, 22)));
		library.addType(new BlockType(null, "turquoise", 18, new Color(85, 135, 136)));
		library.addType(new BlockType(null, "chromeBlue", 19, new Color(136, 181, 178)));
		library.addType(new BlockType(null, "klineWhite", 20, new Color(224, 225, 219)));
		library.addType(new BlockType(null, "salmon", 21, new Color(255, 179, 158)));
		library.addType(new BlockType(null, "rust", 22, new Color(183, 63, 38)));
		library.addType(new BlockType(null, "strongCyan", 23, new Color(105, 210, 231)));
		library.addType(new BlockType(null, "muddledCyan", 24, new Color(167, 219, 216)));
		library.addType(new BlockType(null, "stormyWhite", 25, new Color(224, 228, 204)));
		library.addType(new BlockType(null, "goldfish", 26, new Color(243, 134, 48)));
		library.addType(new BlockType(null, "strongOrange", 27, new Color(250, 105, 0)));
		library.addType(new BlockType(null, "lime", 28, new Color(207, 240, 158)));
		library.addType(new BlockType(null, "greenFoam", 29, new Color(168, 219, 168)));
		library.addType(new BlockType(null, "seaGreen", 30, new Color(121, 189, 154)));
		library.addType(new BlockType(null, "darkTurquoise", 31, new Color(59, 134, 134)));
		library.addType(new BlockType(null, "royalBlue", 32, new Color(11, 72, 107)));
		library.addType(new BlockType(null, "dryTurquoise", 33, new Color(124,150,125)));
		library.addType(new BlockType(null, "dryOlive", 34, new Color(176,173,133)));
		library.addType(new BlockType(null, "brown", 35, new Color(129,66,26)));
		library.addType(new BlockType(null, "paleGold", 36, new Color(255,246,199)));
		library.addType(new BlockType(null, "deepOlive", 37, new Color(55,52,18)));
		library.addType(new BlockType(null, "redBrown", 38, new Color(153,77,37)));
		library.addType(new BlockType(null, "brightMud", 39, new Color(73,51,6)));
		library.addType(new BlockType(null, "royalTurquoise", 40, new Color(26,108,91)));
		library.addType(new BlockType(null, "paleTurquoise", 41, new Color(60,151,112)));
		library.addType(new BlockType(null, "leafGreen", 42, new Color(93,152,38)));
		library.addType(new BlockType(null, "deepGreen", 43, new Color(15,85,69)));
		library.addType(new BlockType(null, "blackTurquoise", 44, new Color(0,36,25)));
		library.addType(new BlockType(null, "tropical", 45, new Color(50,205,141)));
		library.addType(new BlockType(null, "dryGreen", 46, new Color(30,67,17)));
		library.addType(new BlockType(null, "trueGreen", 47, new Color(46,169,37)));
		library.addType(new BlockType(null, "rustRed", 48, new Color(226,109,64)));
		library.addType(new BlockType(null, "brightCyan", 49, new Color(174,238,207)));
		library.addType(new BlockType(null, "deepLavender", 50, new Color(158,126,154)));
		library.addType(new BlockType(null, "manilla", 51, new Color(236,223,191)));
		library.addType(new BlockType(null, "paleCrimson", 52, new Color(170,70,50)));
		library.addType(new BlockType(null, "forestGreen", 53, new Color(67,134,38)));
		library.addType(new BlockType(null, "forestGreen", 54, new Color(67,134,38)));
		library.addType(new BlockType(null, "lavender", 55, new Color(171,138,185)));
		library.addType(new BlockType(null, "strongBrown", 56, new Color(65,40,35)));
		library.addType(new BlockType(null, "bronze", 57, new Color(211,125,12)));
	}

	public void addPalettes() {
		palettes.add(new BytePalette("test", new byte[] { 10, 11, 12, 9, 1, 5, 4, 2, 8, 3, 6, 7 }));
		palettes.add(new BytePalette("Glory", new byte[] { 13, 14, 15, 16, 17, 13, 14, 15, 16, 17 }));
		palettes.add(new BytePalette("Boutique", new byte[] { 18, 19, 20, 21, 22, 18, 19, 20, 21, 22 }));
		palettes.add(new BytePalette("Goldfish", new byte[] { 23, 24, 25, 26, 27, 23, 24, 25, 26, 27 }));
		palettes.add(new BytePalette("Dreamy", new byte[] { 28, 29, 30, 31, 32, 28, 29, 30, 31, 32 }));
		palettes.add(new BytePalette("Technological", new byte[] { 10, 11, 12, 9, 3, 8, 10, 11, 12, 3, 8, 10 }));
		palettes.add(new BytePalette("Murderous", new byte[] { 11, 12, 9, 1, 22, 26 }));
		palettes.add(new BytePalette("Golddigger", new byte[] { 11, 12, 9, 5, 4, 53 }));
		palettes.add(new BytePalette("Undergrowth", new byte[] { 11, 12, 9, 46, 47, 2 }));
		palettes.add(new BytePalette("Feeling Blue", new byte[] { 49, 24, 23, 19, 18, 3, 32, 13 }));
		palettes.add(new BytePalette("Oranje", new byte[] { 51, 21, 53, 26, 48, 27, 22 }));
		palettes.add(new BytePalette("Tropical Sea", new byte[] { 36, 24, 19, 33, 41, 43, 44 }));
		palettes.add(new BytePalette("Motherboard", new byte[] { 47, 46, 34, 4, 5, 36, 10, 11, 25 }));
		palettes.add(new BytePalette("Fire in the Night", new byte[] { 9, 12, 11, 10, 5, 4, 27, 1, 22, 13 }));
		palettes.add(new BytePalette("Sulfur", new byte[] { 5, 36, 33, 36, 37, 38, 39 }));
		palettes.add(new BytePalette("Alien", new byte[] { 40, 9, 41, 42, 43, 44, 45, 46, 47 }));
		palettes.add(new BytePalette("Fruit Shake", new byte[] { 50, 10, 49, 45, 33, 36, 51, 26, 27 }));
		palettes.add(new BytePalette("Frosted Cactus", new byte[] { 53, 52, 41, 10, 36, 56, 54, 55 }));
	}

	public BytePalette getPalette(String name) {
		for (int i = 0; i < palettes.size(); i++) {
			if (palettes.get(i).getName().equals(name))
				return palettes.get(i);
		}
		return palettes.get(0);
	}


	/***************************************************************
	 ********************** Load/Save Methods **********************
	 ***************************************************************/

	public void load(String filename) {
		System.out.println("Loading file " + filename);
		try {
			FileInputStream fis = new FileInputStream(filename);
			GZIPInputStream gs = new GZIPInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(gs);
			cchunks = (byte[][][][]) in.readObject();
			in.close();
			fis.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void save(String filename) {
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			GZIPOutputStream gz = new GZIPOutputStream(fos);
			ObjectOutputStream out = new ObjectOutputStream(gz);
			out.writeObject(cchunks);
			out.flush();
			out.close();
			fos.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public ObjectGenerator getGenerator() {
		return generator;
	}

	public int getSize() {
		return size;
	}

}
