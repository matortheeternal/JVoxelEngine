package voxelengine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class World {
	private int size;
	private Chunk[][][] chunks;
	private boolean[][][] isEmpty;
	private BlockLibrary library = new BlockLibrary();
	private ArrayList<Palette> palettes = new ArrayList<Palette>();
	
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
	public Block getBlock(int x, int y, int z) {
		int xChunk = x/16;
		int yChunk = y/16;
		int zChunk = z/16;
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		if (chunk != null)
			return chunk.getBlock(x % 16, y % 16, z % 16);
		else
			return null;
	}
	public Block getBlock(double x, double y, double z) {
		int xi = (int) x;
		int yi = (int) y;
		int zi = (int) z;
		return getBlock(xi, yi, zi);
	}
	public void setBlock(String id, int x, int y, int z) {
		int xChunk = x/16;
		int yChunk = y/16;
		int zChunk = z/16;
		Chunk chunk = getChunk(xChunk, yChunk, zChunk);
		BlockType type = getType(id);
		chunk.setBlock(type, x % 16, y % 16, z % 16);
	}
	public void setBlock(String id, double x, double y, double z) {
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
	public BlockType getType(String id) {
		return library.getType(id);
	}
	
	// world generation methods
	public void generateMengerSponge(String id, int d0, int xOffset, int yOffset, int zOffset) {
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
	
	public void generateBox(String id, int d0, int xOffset, int yOffset, int zOffset) {
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
	
	public void generateBensWorld() {
		for (int i = 0; i < 100; ++i) {
			Random r = new Random(i);
			for (int j = 0; j < 100; ++j) {
				setBlock("blue", i, j, 0);
				if ((i + j) % 2 == 0)
					setBlock("red", i, 0, j);
				else
					setBlock("black", i, 0, j);
				setBlock("orange", 0, i, j);
				setBlock("cyan", i, j, 50);
				setBlock("yellow", i, 99, j);
				setBlock("pink", 99, i, j);

				if (i % 4 == 0) {
					int rand = r.nextInt(100);
					if (rand < 10)
						setBlock("green", i, 70, j);
					else if (rand < 50)
						setBlock("gray", i, 70, j);
					else
						setBlock("darkGray", i, 70, j);
				} else if (i % 2 == 0)
					setBlock("black", i, 70, j);
			}
		}
	}
	
	// generate mandelbulb
	public void generateMandelbulb(String name, int d, double power, double cutoff, int xOffset, int yOffset, int zOffset, int itMin, int itMax) {
	    Palette palette = getPalette(name);
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
                        String id = palette.get(iterations - itMin);
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
	    Palette palette = getPalette(name);
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
	                        String id = palette.get(iterations - itMin);
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
	
	// palettes and block types
	public void addColorBlockTypes() {
		library.addType(new BlockType(null, "red", "1", Color.RED));
		library.addType(new BlockType(null, "green", "2", Color.GREEN));
		library.addType(new BlockType(null, "blue", "3", Color.BLUE));
		library.addType(new BlockType(null, "yellow", "4", Color.YELLOW));
		library.addType(new BlockType(null, "orange", "5", Color.ORANGE));
		library.addType(new BlockType(null, "magenta", "6", Color.MAGENTA));
		library.addType(new BlockType(null, "pink", "7", Color.PINK));
		library.addType(new BlockType(null, "cyan", "8", Color.CYAN));
		library.addType(new BlockType(null, "black", "9", Color.BLACK));
		library.addType(new BlockType(null, "white", "A", Color.WHITE));
		library.addType(new BlockType(null, "gray", "B", Color.GRAY));
		library.addType(new BlockType(null, "darkGray", "C", Color.DARK_GRAY));
	}
	
	public void addPalettes() {
		palettes.add(new Palette("test", new String[]{"white", "gray", "darkGray", "black", "red", "orange", "yellow", "green", "cyan", "blue", "magenta", "pink"}));
		palettes.add(new Palette("blackNblue", new String[]{"white", "gray", "darkGray", "black", "blue", "cyan", "white", "gray", "darkGray", "blue", "cyan", "white"}));
	}
	
	public Palette getPalette(String name) {
		for (int i = 0; i < palettes.size(); i++) {
			if (palettes.get(i).getName().equals(name))
				return palettes.get(i);
		}
		return null;
	}
	
	// load/save world
	public void load(String filename) {
		
	}
	public void save(String filename) {
		
	}
}
