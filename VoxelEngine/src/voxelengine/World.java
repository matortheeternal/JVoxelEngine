package voxelengine;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
	private int size;
	private Chunk[][][] chunks;
	private boolean[][][] isEmpty;
	private BlockLibrary library = new BlockLibrary();
	private ArrayList<BytePalette> palettes = new ArrayList<BytePalette>();
	
	// constructor
	public World(int size) {
		super();
		this.size = size;
		this.chunks = new Chunk[size][size][size];
		this.isEmpty = new boolean[size][size][size];
		
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				for (int z = 0; z < size; z++) {
					this.chunks[x][y][z] = new Chunk();
					this.isEmpty[x][y][z] = false;
				}
	}
	
	// methods
	public int getSize() {
		return size;
	}
	
	// chunk/block methods
	public Chunk getChunk(int x, int y, int z) {
		if ((x < size && x >= 0) && (y < size && y >= 0) && (z < size && z >= 0))
			return chunks[x][y][z];
		else
			return null;
	}
	public byte getBlock(int x, int y, int z) {
		int xChunk = x/16;
		int yChunk = y/16;
		int zChunk = z/16;
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		if (chunk != null)
			return chunk.getBlock(x % 16, y % 16, z % 16);
		else
			return 0;
	}
	public byte getBlock(double x, double y, double z) {
		int xi = (int) x;
		int yi = (int) y;
		int zi = (int) z;
		return getBlock(xi, yi, zi);
	}
	public void setBlock(byte id, int x, int y, int z) {
		int xChunk = x/16;
		int yChunk = y/16;
		int zChunk = z/16;
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		chunk.setBlock(id, x % 16, y % 16, z % 16);
	}
	public void setBlock(byte id, double x, double y, double z) {
		int xi = (int) x;
		int yi = (int) y;
		int zi = (int) z;
		setBlock(id, xi, yi, zi);
	}
	
	// isEmpty methods
	public boolean getIsEmpty(int x, int y, int z) {
		return isEmpty[x][y][z];
	}
	public void updateIsEmpty(int x, int y, int z) {
		Chunk chunk = getChunk(x, y, z);
		isEmpty[x][y][z] = chunk.checkIfEmpty();
	}
	public void updateIsEmptyAll() {
		for (int x = 0; x < size; x++)
			for (int y = 0; y < size; y++)
				for (int z = 0; z < size; z++)
					isEmpty[x][y][z] = getChunk(x,y,z).checkIfEmpty();
	}
	
	// block type methods
	public void addType(BlockType type) {
		library.addType(type);
	}
	public BlockType getType(byte id) {
		return library.getType(id);
	}
	
	// world generation methods
	public void generateMengerSponge(byte id, int d0, int xOffset, int yOffset, int zOffset) {
		// place block and return if dimension = 1
		if (d0 == 1) {
			setBlock(id, xOffset, yOffset, zOffset);
			return;
		}
		
		// 1/3 of dimension
		int d1 = d0/3;
		
		// recursion loop
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if ((y == 1) && (x == 1))
					continue;
				for (int z = 0; z < 3; z++) {
					if (((y == 1) && (z == 1)) || ((x == 1) && (z == 1)))
	                    continue;
					// recursion
					if (d0 > 3)
						generateMengerSponge(id, d1, x*d1 + xOffset, y*d1 + yOffset, z*d1 + zOffset);
					else {
						setBlock(id, x + xOffset, y + yOffset, z + zOffset);
					}
				}
			}
		}
	}
	
	public void generateBox(byte id, int d0, int xOffset, int yOffset, int zOffset) {
		for (int x = 0; x < d0; x++) {
			for (int y = 0; y < d0; y++) {
				setBlock(id, x + xOffset, y + yOffset, zOffset);
				setBlock(id, x + xOffset, y + yOffset, (d0 - 1) + zOffset);
			}
		}
		for (int y = 0; y < d0; y++) {
			for (int z = 0; z < d0; z++) {
				setBlock(id, xOffset, y + yOffset, z + zOffset);
				setBlock(id, (d0 - 1) + xOffset, y + yOffset, z + zOffset);
			}
		}
		for (int x = 0; x < d0; x++) {
			for (int z = 0; z < d0; z++) {
				setBlock(id, x + xOffset, yOffset, z + zOffset);
				setBlock(id, x + xOffset, (d0 - 1) + yOffset, z + zOffset);
			}
		}
	}
	
	// generate mandelbulb
	public void generateMandelbulb(String name, int d, double power, double cutoff, int xOffset, int yOffset, int zOffset, int itMin, int itMax) {
	    BytePalette palette = getPalette(name);
	    //palette.printContents();
	    for (int x = 0; x < d; x++) {
	        double xc = tf(x, d);
	        for (int y = 0; y < d; y++) {
	            double yc = tf(y, d);
	            for (int z = 0; z < d; z++) {
	                double zc = tf(z, d);
	                
	                int iterations = -1;
	                double[] C = new double[]{xc, yc, zc};
	                double[] Z = new double[]{0, 0, 0};
	                
	                // iterate over vectors
	                while ((mag(Z) <= cutoff) && (iterations < itMax)) {
	                    Z = add(formula(Z, power), C);
	                    iterations++;
	                }
	                
	                // place block if cutoff reached in itMin -> itMax range
	                if ((iterations >= itMin) && (iterations < itMax)) {
                        byte id = palette.get(iterations - itMin);
                        //System.out.println("Placing block "+id+" at ("+(xOffset + x)+","+(yOffset + y)+","+(zOffset + z)+")");
	                    setBlock(id, xOffset + x, yOffset + y, zOffset + z);
	                }
	            }
	        }
	    }
	}
	
	private double tf(int v, int d) {
		return (2.0 * v)/((double) d) - 1.0;
	}
	
	private double mag(double[] vec) {
		return Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]);
	}
	
	private double[] add(double[] vec1, double[] vec2) {
		return new double[]{vec1[0] + vec2[0], vec1[1] + vec2[1], vec1[2] + vec2[2]};
	}
	
	// mandelbulb vector growth formula
	// if current vector magnitude > 1 and t is not too close to a multiple of (pi/2)
	// this will give a vector of greater magnitude than the vector put in
	// n, which is param, is the rate at which vector magnitude grows.
	private double[] formula(double[] vec, double n) {
	    double t = theta(vec);
	    double p = phi(vec);
	    double k = Math.pow(mag(vec), n);
	    return new double[] {
		        k*Math.sin(n*t)*Math.cos(n*p), 
		        k*Math.sin(n*t)*Math.sin(n*p), 
		        k*Math.cos(n*t)};
	}

	// theta vector value (arccos)
	private double theta(double[] vec) {
	    return (Math.acos(vec[2]/(mag(vec) + 0.000000001)));
	}

	// phi vector value (arctan)
	private double phi(double[] vec) {
	    return (Math.atan(vec[1]/(vec[0] + 0.000000001)));
	}
	
	// generate mandelbox
	public void generateMandelbox(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, int xOffset, int yOffset, int zOffset) {
	    double tfsub = tfmult/2;
	    BytePalette palette = getPalette(name);
		for (int x = 0; x < d; x++) {
	        double xc = (tfmult * x)/(d - 1) - tfsub;
	        for (int y = 0; y < d; y++) {
	            double yc = (tfmult * y)/(d - 1) - tfsub;
	            for (int z = 0; z < d; z++) {
	                double zc = (tfmult * z)/(d - 1) - tfsub;

	                int iterations = -1;
	                double[] C = new double[]{xc, yc, zc};
	                double[] Z = new double[]{0, 0, 0};

	                while ((mag(Z) < cutoff) && (iterations < itMax)) {
	                    Z = add(mult(scale, boxformula(Z)), C);
	                    iterations++;
	                }
	                
	                if ((iterations >= itMin) && (iterations < itMax)) {
		                // place block if cutoff reached in itMin -> itMax range
		                if ((iterations >= itMin) && (iterations < itMax)) {
	                        byte id = palette.get(iterations - itMin);
	                        //System.out.println("Placing block "+id+" at ("+(xOffset + x)+","+(yOffset + y)+","+(zOffset + z)+")");
		                    setBlock(id, xOffset + x, yOffset + y, zOffset + z);
		                }
	                }
	            }
	        }
	    }
	}
	
	// mandelbox vector growth formula
	private double[] boxformula(double[] vec) {
	    double x = vec[0];
	    double y = vec[1];
	    double z = vec[2];
	    
	    if (x > 1)
	        x = 2 - x;
	    else if (x < -1)
	        x = -2 - x;
	        
	    if (y > 1)
	        y = 2 - y;
	    else if (y < -1)
	        y = -2 - y;
	    
	    if (z > 1)
	        z = 2 - z;
	    else if (z < -1)
	        z = -2 - z;
	    
	    double[] output = new double[]{x, y, z};
	    double m = mag(output);
	    
	    if (m < 0.5)
	        output = mult(4, output);
	    else if (m < 1)
	        output = mult(1/(m*m), output);
	    
	    return output;
	}

	// multiplies a constant and a vector together
    private double[] mult(double c, double[] vec) {
	    double[] output = new double[]{
	        c * vec[0], 
	        c * vec[1], 
	        c * vec[2]};
	    
	    return output;
	}
    
    // greek cross fractal
    public void generateCross(byte id, int d0, int xOffset, int yOffset, int zOffset, int scale, int mode) {
    	int d1 = (d0 - 1)/2;
        if ((Math.floor(d1) - d1 != 0) || (d1 < scale))
            return;
        // make cross
        if (mode != 1) {
            for (int x = 0; x < d0; x++) {
                if (x == d1)
                    continue;
                setBlock(id, x + xOffset, d1 + yOffset, d1 + zOffset);
            }
        }
        if (mode != 2) {
            for (int y = 0; y < d0; y++) {
                if (y == d1)
                    continue;
                setBlock(id, d1 + xOffset, y + yOffset, d1 + zOffset);
            }
        }
        if (mode != 3) {
            for (int z = 0; z < d0; z++) {
                if (z == d1)
                    continue;
                setBlock(id, d1 + xOffset, d1 + yOffset, z + zOffset);
            }
        }
        d1++;
        // recursion close
        generateCross(id, d1 - 1, 0 + xOffset, d1/2 + yOffset, d1/2 + zOffset, scale, 1);
        generateCross(id, d1 - 1, d1/2 + xOffset, 0 + yOffset, d1/2 + zOffset, scale, 2);
        generateCross(id, d1 - 1, d1/2 + xOffset, d1/2 + yOffset, 0 + zOffset, scale, 3);
        // recursion far
        generateCross(id, d1 - 1, d1 + xOffset, d1/2 + yOffset, d1/2 + zOffset, scale, 1);
        generateCross(id, d1 - 1, d1/2 + xOffset, d1 + yOffset, d1/2 + zOffset, scale, 2);
        generateCross(id, d1 - 1, d1/2 + xOffset, d1/2 + yOffset, d1 + zOffset, scale, 3);
    }

    // octahedron fractal
    public void generateOctahedron(byte id, int d0, int xOffset, int yOffset, int zOffset, int oscale) {
        int d1 = (d0 - 1)/2;
        int d2 = (d1 - 1)/2;
        // create octahedron when minimum scale reached
        if (d1 < oscale) {
            int width = 0;
            for (int y = 0; y < d0; y++) {
                for (int z = 0; z < d0; z++) {
                    for (int x = 0; x < d0; x++) {
                        if (Math.abs(z - d1) + Math.abs(x - d1) <= width) {
                            setBlock(id, x + xOffset, y + yOffset, z + zOffset);
                        }
                    }
                }
                if (y > d1 - 1) 
                    width--;
                else
                    width++;
            }
        }
        else { // recursion
            generateOctahedron(id, d1, d2 + 1 + xOffset, d1 + 1 + yOffset, d2 + 1 + zOffset, oscale); // top octahedron
            generateOctahedron(id, d1, d2 + 1 + xOffset, yOffset, d2 + 1 + zOffset, oscale); // bottom octahedron
            generateOctahedron(id, d1, xOffset, d2 + 1 + yOffset, d2 + 1 + zOffset, oscale); // close right octahedron
            generateOctahedron(id, d1, d2 + 1 + xOffset, d2 + 1 + yOffset, zOffset, oscale); // close left octahedron
            generateOctahedron(id, d1, d2 + 1 + xOffset, d2 + 1 + yOffset, d1 + 1 + zOffset, oscale); // far right octahedron
            generateOctahedron(id, d1, d1 + 1 + xOffset, d2 + 1 + yOffset, d2 + 1 + zOffset, oscale); // far left octahedron
        }
    }
	
	// palettes and block types
	public void addColorBlockTypes() {
		library.addType(new BlockType(null, "red", 1, Color.RED));
		library.addType(new BlockType(null, "green", 2, Color.GREEN));
		library.addType(new BlockType(null, "blue", 3, Color.BLUE));
		library.addType(new BlockType(null, "yellow", 4, Color.YELLOW));
		library.addType(new BlockType(null, "orange", 5, Color.ORANGE));
		library.addType(new BlockType(null, "magenta", 6, Color.MAGENTA));
		library.addType(new BlockType(null, "pink", 7, Color.PINK));
		library.addType(new BlockType(null, "cyan", 8, Color.CYAN));
		library.addType(new BlockType(null, "black", 9, Color.BLACK));
		library.addType(new BlockType(null, "white", 10, Color.WHITE));
		library.addType(new BlockType(null, "gray", 11, Color.GRAY));
		library.addType(new BlockType(null, "darkGray", 12, Color.DARK_GRAY));
		library.addType(new BlockType(null, "gloriousBlue", 13, new Color(7, 8, 114)));
		library.addType(new BlockType(null, "watermelon", 14, new Color(247, 70, 122)));
		library.addType(new BlockType(null, "gloriousViolet", 15, new Color(73, 8, 162)));
		library.addType(new BlockType(null, "darkBlue", 16, new Color(14, 0, 42)));
		library.addType(new BlockType(null, "brownPurple", 17, new Color(40, 0, 22)));
		library.addType(new BlockType(null, "turquoise", 18, new Color(85, 135, 136)));
		library.addType(new BlockType(null, "lightTurquoise", 19, new Color(136, 181, 178)));
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
	}
	
	public void addPalettes() {
		palettes.add(new BytePalette("test", new byte[] { 10, 11, 12, 9, 1, 5, 4, 2, 8, 3, 6, 7 }));
		palettes.add(new BytePalette("blackNblue", new byte[] { 10, 11, 12, 9, 3, 8, 10, 11, 12, 3, 8, 10 }));
		palettes.add(new BytePalette("glory", new byte[] { 13, 14, 15, 16, 17, 13, 14, 15, 16, 17 }));
		palettes.add(new BytePalette("boutique", new byte[] { 18, 19, 20, 21, 22, 18, 19, 20, 21, 22 }));
		palettes.add(new BytePalette("goldfish", new byte[] { 23, 24, 25, 26, 27, 23, 24, 25, 26, 27 }));
		palettes.add(new BytePalette("dreamy", new byte[] { 28, 29, 30, 31, 32, 28, 29, 30, 31, 32 }));
	}
	
	public BytePalette getPalette(String name) {
		for (int i = 0; i < palettes.size(); i++) {
			if (palettes.get(i).getName().equals(name))
				return palettes.get(i);
		}
		return null;
	}

	// load/save world
	public void load(String filename) {
		try {
			FileInputStream fis = new FileInputStream(filename);
			GZIPInputStream gs = new GZIPInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(gs);
			chunks = (Chunk[][][]) in.readObject();
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
			out.writeObject(chunks);
			out.flush();
			out.close();
			fos.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public byte getByteFromName(String string) {
		return library.getType(string).getId();
	}
}
