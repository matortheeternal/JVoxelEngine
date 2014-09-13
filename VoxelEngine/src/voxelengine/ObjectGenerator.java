package voxelengine;

import static java.lang.Math.*;

import java.text.DecimalFormat;

public class ObjectGenerator {
	private int csz;
	private int recursionCounter = 0;
	private int recursionGoal = 0;
	private long messageDelay = 500;
	private long time = 0;

	private World world;

	public ObjectGenerator(World world, int chunkSize) {
		this.world = world;
		this.csz = chunkSize;
	}

	public int getCounter() {
		return recursionCounter;
	}

	/***************************************************************
	 ********************* Basic Shape Methods *********************
	 ***************************************************************/

	public void generateCube(byte id, int d0, int xo, int yo, int zo) {
		Chunk c = new Chunk(csz);

		for (int x = xo; (x < csz) && (x < xo + d0); x++) {
			for (int y = yo; (y < csz) && (y < yo + d0); y++) {
				for (int z = zo; (z < csz) && (z < zo + d0); z++) {
					c.setBlock(id, x, y, z);
				}
			}
		}

		world.setChunk(1, 1, 1, c);
	}

	public void generateBox(byte id, int d0, int xOffset, int yOffset, int zOffset) {
		for (int x = 0; x < d0; x++) {
			for (int y = 0; y < d0; y++) {
				world.setBlock(id, x + xOffset, y + yOffset, zOffset);
				world.setBlock(id, x + xOffset, y + yOffset, (d0 - 1) + zOffset);
			}
		}
		for (int y = 0; y < d0; y++) {
			for (int z = 0; z < d0; z++) {
				world.setBlock(id, xOffset, y + yOffset, z + zOffset);
				world.setBlock(id, (d0 - 1) + xOffset, y + yOffset, z + zOffset);
			}
		}
		for (int x = 0; x < d0; x++) {
			for (int z = 0; z < d0; z++) {
				world.setBlock(id, x + xOffset, yOffset, z + zOffset);
				world.setBlock(id, x + xOffset, (d0 - 1) + yOffset, z + zOffset);
			}
		}
	}

	/***************************************************************
	 ******************** Menger Sponge Methods ********************
	 ***************************************************************/

	public void generateMengerSponge(byte id, int d0, int xOffset, int yOffset, int zOffset) {
		// set recursion goal
		if (recursionGoal == 0) {
			recursionGoal = getMengerGoal(d0);
			// System.out.println("Menger sponge goal: "+recursionGoal);
			time = System.currentTimeMillis();
		}

		// increment recursion counter
		recursionCounter++;

		// print recursion message once every messageDelay seconds
		if (System.currentTimeMillis() - time > messageDelay) {
			double recursionProgress = (double) 100 * (recursionCounter) / (recursionGoal);
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println(df.format(recursionProgress) + "%");
			time = System.currentTimeMillis();
		}

		// place block and return if dimension = 1
		if (d0 == 1) {
			world.setBlock(id, xOffset, yOffset, zOffset);
			return;
		}

		// 1/3 of dimension
		int d1 = d0 / 3;

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
						generateMengerSponge(id, d1, x * d1 + xOffset, y * d1 + yOffset, z * d1 + zOffset);
					else {
						world.setBlock(id, x + xOffset, y + yOffset, z + zOffset);
					}
				}
			}
		}
	}

	private int getMengerGoal(int d0) {
		int volume = 0;
		int it = 0;
		while (d0 >= 3) {
			d0 /= 3;
			it++;
		}

		while (it >= 0) {
			it--;
			volume += pow(20, it);
		}

		return volume;
	}

	// menger sponge chunk-based generation
	public void generateMengerChunks(byte id, int d, int x1, int y1, int z1) {
		// calculate the coordinates for the starting chunks
		int xs = x1 / csz;
		int ys = y1 / csz;
		int zs = z1 / csz;

		// calculate the coordinates for the ending chunks
		int xe = (x1 + d) / csz;
		int ye = (y1 + d) / csz;
		int ze = (z1 + d) / csz;

		// generate each chunk and set it in the world
		Long time = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#.##");
		int totalChunks = (xe - xs + 1)*(ye - ys + 1)*(ze - zs + 1);
		for (int x = xs; x <= xe; x++)
			for (int y = ys; y <= ye; y++)
				for (int z = zs; z <= ze; z++) {
					world.setChunk(x, y, z, generateMengerChunk(id, d, x1, y1, z1, x - xs, y - ys, z - zs));
					if ((System.currentTimeMillis() - time) > messageDelay) {
						int currentChunks = (ze - zs + 1)*(ye - ys + 1)*(x - xs) + (ze - zs + 1)*(y - ys) + (z - zs);
						double progress = (double) 100 * currentChunks/totalChunks;
						System.out.println(df.format(progress)+"%");
						time = System.currentTimeMillis();
					}
				}
	}

	public Chunk generateFilledChunk(byte id) {
		Chunk c = new Chunk(csz);
		for (int x = 0; x < csz; x++)
			for (int y = 0; y < csz; y++)
				for (int z = 0; z < csz; z++) {
					c.setBlock(id, x, y, z);
				}
		return c;
	}

	// generate a single chunk that's part of a larger Menger Sponge
	public Chunk generateMengerChunk(byte id, int d, int x1, int y1, int z1, int xc, int yc, int zc) {
		// prepare the chunk and the BlockType
		Chunk c = new Chunk(csz);
		
		// here we calculate the upper limit in each Cartesian direction of the object
		// that we're generating. this is important for us to not attempt to generate
		// blocks outside of the object's actual dimensions
		int x2 = x1 + d;
		int y2 = y1 + d;
		int z2 = z1 + d;

		// we loop over the entire chunk, calculating Cartesian values relative to the
		// origin of the fractal we're generating. we also calculate the real Cartesian
		// values for each point to make sure we're in the range of the fractal we're
		// generating.
		for (int x = 0; x < csz; x++) {
			int xRelative = xc * csz + x - (x1 % csz);
			int xReal = xRelative + x1;
			if ((xReal < x1) || (xReal > x2))
				continue;
			for (int y = 0; y < csz; y++) {
				int yRelative = yc * csz + y - (y1 % csz);
				int yReal = yRelative + y1;
				if ((yReal < y1) || (yReal > y2))
					continue;
				for (int z = 0; z < csz; z++) {
					int zRelative = zc * csz + z - (z1 % csz);
					int zReal = zRelative + z1;
				    if ((zReal < z1) || (zReal > z2))
				    	continue;
					// if the location is part of the Menger Sponge set, we place a block
					if (mengerTest(d, xRelative, yRelative, zRelative)) {
//						System.out.println("Set block at (" + xRelative + ", " + yRelative + ", " + zRelative + ")");
						c.setBlock(id, x, y, z);
					}
				}
			}
		}

		return c;
	}

	// recursively test if a location in Cartesian coordinates is a part of the
	// Menger Sponge set
	public boolean mengerTest(int d, int x, int y, int z) {
		int d3 = d / 3;

		// test if x, y, or z is in the d/3 to 2d/3 range
		boolean xOut = ((x >= d3) && (x < 2 * d3));
		boolean yOut = ((y >= d3) && (y < 2 * d3));
		boolean zOut = ((z >= d3) && (z < 2 * d3));

		// if two Cartesian values are out of range, return false
		// else, if d >= 9 recurse with d/3 and modulused Cartesian values
		// else, return true
		if ((xOut && yOut) || (yOut && zOut) || (xOut && zOut)) {
			return false;
		} else if (d >= 9) {
			return mengerTest(d3, x % d3, y % d3, z % d3);
		} else {
			return true;
		}
	}

	// public void generateMengerSpongeChunks(byte id, int d0, int xo, int yo, int zo) {
	// int xlim = (xo + d0) / csz;
	// int ylim = (yo + d0) / csz;
	// int zlim = (zo + d0) / csz;
	// int xs = xo / csz;
	// int ys = yo / csz;
	// int zs = zo / csz;
	// int xm = d0;
	// int ym = d0;
	// int zm = d0;
	//
	// int ixo = xo % csz;
	// for (int i = xs; i <= xlim; i++) {
	// int iyo = yo % csz;
	// ym = d0;
	// zm = d0;
	// for (int j = ys; j <= ylim; j++) {
	// int izo = zo % csz;
	// zm = d0;
	// for (int k = zs; k <= zlim; k++) {
	// Chunk c = generateMengerChunk(id, d0, ixo, iyo, izo, (i - xs) * csz, (j - ys) * csz,
	// (k - zs) * csz, xm, ym, zm);
	// world.setChunk(i, j, k, c);
	// izo = 0;
	// zm -= (csz - izo);
	// }
	// ym -= (csz - iyo);
	// iyo = 0;
	// }
	// xm -= (csz - ixo);
	// ixo = 0;
	// }
	// }
	//
	// public Chunk generateMengerChunk(byte id, int d0, int xo, int yo, int zo, int cxo, int cyo, int czo, int xm,
	// int ym, int zm) {
	// Chunk c = new Chunk(csz);
	// for (int x = 0; (x < 64 - xo) && (x < xm); x++)
	// for (int y = 0; (y < 64 - yo) && (y < ym); y++)
	// for (int z = 0; (z < 64 - zo) && (z < zm); z++) {
	// boolean b = mengerTest(d0, x + cxo, y + cyo, z + czo);
	// if (b)
	// c.setBlock(id, x + xo, y + yo, z + zo);
	// }
	//
	// return c;
	// }
	//
	// public boolean mengerTest(int d0, int x, int y, int z) {
	// boolean bx = ((x >= d0 / 3) && (x < 2 * d0 / 3));
	// boolean by = ((y >= d0 / 3) && (y < 2 * d0 / 3));
	// boolean bz = ((z >= d0 / 3) && (z < 2 * d0 / 3));
	//
	// // recursion
	// if (d0 > 3) {
	// int d1 = d0 / 3;
	// if ((bx) && (by || bz))
	// return mengerTest(d1, x % d1, y % d1, z % d1);
	// else if (by && bz)
	// return mengerTest(d1, x % d1, y % d1, z % d1);
	//
	// return false;
	// }
	//
	// if ((bx) && (by || bz))
	// return true;
	// else if (by && bz)
	// return true;
	//
	// return false;
	// }

	/***************************************************************
	 ********************* Mandelbulb Methods *********************
	 ***************************************************************/

	// generate mandelbulb
	public void generateMandelbulb(String name, int d, double power, double cutoff, int xOffset, int yOffset, int zOffset, int itMin, int itMax) {
		// prepare palette
		BytePalette palette = world.getPalette(name);
		if (itMax - itMin > palette.getSize()) {
			System.out.println("Remapping palette from " + palette.getSize() + " to " + (itMax - itMin));
			palette = palette.remap(itMax - itMin);
		}

		// iterate for all x,y,z
		for (int x = 0; x < d; x++) {
			double xc = tf(x, d);
			for (int y = 0; y < d; y++) {
				// print message once every messageDelay seconds
				if (System.currentTimeMillis() - time > messageDelay) {
					double progress = (double) 100 * (d * d * x) / (pow(d, 3));
					DecimalFormat df = new DecimalFormat("#.##");
					System.out.println(df.format(progress) + "%");
					time = System.currentTimeMillis();
				}
				double yc = tf(y, d);
				for (int z = 0; z < d; z++) {
					double zc = tf(z, d);

					int iterations = -1;
					double[] C = new double[] { xc, yc, zc };
					double[] Z = new double[] { 0, 0, 0 };

					// iterate over vectors
					while ((mag(Z) <= cutoff) && (iterations < itMax)) {
						Z = add(formula(Z, power), C);
						iterations++;
					}

					// place block if cutoff reached in itMin -> itMax range
					if ((iterations >= itMin) && (iterations < itMax)) {
						byte id = palette.get(iterations - itMin);
						// System.out.println("Placing block "+id+" at ("+(xOffset + x)+","+(yOffset + y)+","+(zOffset + z)+")");
						world.setBlock(id, xOffset + x, yOffset + y, zOffset + z);
					}
				}
			}
		}
	}

	public void generateMandelbulbChunks(String name, int d, double power, double cutoff, int itMin, int itMax, int xo, int yo, int zo) {
		int xlim = (xo + d) / csz;
		int ylim = (yo + d) / csz;
		int zlim = (zo + d) / csz;
		int xs = xo / csz;
		int ys = yo / csz;
		int zs = zo / csz;
		int xm = d;
		int ym = d;
		int zm = d;

		int ixo = xo % csz;
		for (int i = xs; i <= xlim; i++) {
			int iyo = yo % csz;
			ym = d;
			zm = d;
			for (int j = ys; j <= ylim; j++) {
				int izo = zo % csz;
				zm = d;
				for (int k = zs; k <= zlim; k++) {
					Chunk c = generateMandelbulbChunk(name, d, power, cutoff, itMin, itMax, ixo, iyo, izo, (i - xs) * csz, (j - ys) * csz, (k - zs) * csz, xm, ym, zm);
					world.setChunk(i, j, k, c);
					zm -= csz - izo;
					izo = 0;
				}
				ym -= csz - iyo;
				iyo = 0;
			}
			xm -= csz - ixo;
			ixo = 0;
		}
	}

	public Chunk generateMandelbulbChunk(String name, int d, double power, double cutoff, int itMin, int itMax, int xo, int yo, int zo, int cxo, int cyo, int czo, int xm, int ym, int zm) {

		Chunk chunk = new Chunk(64);
		BytePalette palette = world.getPalette(name);

		for (int x = 0; (x < 64 - xo) && (x < xm); x++) {
			double xc = (2.0 * (x + cxo)) / (d - 1) - 1.0;
			// double xc = tf(x + cxo, d);
			for (int y = 0; (y < 64 - yo) && (y < ym); y++) {
				double yc = (2.0 * (y + cyo)) / (d - 1) - 1.0;
				for (int z = 0; (z < 64 - zo) && (z < zm); z++) {
					double zc = (2.0 * (z + czo)) / (d - 1) - 1.0;

					int iterations = -1;
					double[] C = new double[] { xc, yc, zc };
					double[] Z = new double[] { 0, 0, 0 };

					// iterate over vectors
					while ((mag(Z) <= cutoff) && (iterations < itMax)) {
						Z = add(formula(Z, power), C);
						iterations++;
					}

					// place block if cutoff reached in itMin -> itMax range
					if ((iterations >= itMin) && (iterations < itMax)) {
						byte id = palette.get(iterations - itMin);
						chunk.setBlock(id, xo + x, yo + y, zo + z);
					}
				}
			}
		}

		return chunk;
	}

	private double tf(int v, int d) {
		return (2.0 * v) / ((double) d) - 1.0;
	}

	private double mag(double[] vec) {
		return sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]);
	}

	private double[] add(double[] vec1, double[] vec2) {
		return new double[] { vec1[0] + vec2[0], vec1[1] + vec2[1], vec1[2] + vec2[2] };
	}

	// mandelbulb vector growth formula
	// if current vector magnitude > 1 and t is not too close to a multiple of (pi/2)
	// this will give a vector of greater magnitude than the vector put in
	// n is the rate at which vector magnitude grows.
	private double[] formula(double[] vec, double n) {
		double t = theta(vec);
		double p = phi(vec);
		double k = pow(mag(vec), n);
		return new double[] { k * sin(n * t) * cos(n * p), k * sin(n * t) * sin(n * p), k * cos(n * t) };
	}

	// theta vector value (arccos)
	private double theta(double[] vec) {
		return (acos(vec[2] / (mag(vec) + 0.000000001)));
	}

	// phi vector value (arctan)
	private double phi(double[] vec) {
		return (atan(vec[1] / (vec[0] + 0.000000001)));
	}

	/***************************************************************
	 ********************** Mandelbox Methods **********************
	 ***************************************************************/

	// generate mandelbox
	public void generateMandelbox(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, 
			int xOffset, int yOffset, int zOffset) {
		// prepare palette
		BytePalette palette = world.getPalette(name);
		if (itMax - itMin > palette.getSize()) {
			System.out.println("Remapping palette from " + palette.getSize() + " to " + (itMax - itMin));
			palette = palette.remap(itMax - itMin);
		}

		// iterate for all x,y,z
		double tfsub = tfmult / 2;
		for (int x = 0; x < d; x++) {
			double xc = (tfmult * x) / (d - 1) - tfsub;
			for (int y = 0; y < d; y++) {
				// print message once every messageDelay seconds
				if (System.currentTimeMillis() - time > messageDelay) {
					double progress = (double) 100 * (d * d * x) / (pow(d, 3));
					DecimalFormat df = new DecimalFormat("#.##");
					System.out.println(df.format(progress) + "%");
					time = System.currentTimeMillis();
				}
				double yc = (tfmult * y) / (d - 1) - tfsub;
				for (int z = 0; z < d; z++) {
					double zc = (tfmult * z) / (d - 1) - tfsub;

					int iterations = -1;
					double[] C = new double[] { xc, yc, zc };
					double[] Z = new double[] { 0, 0, 0 };

					while ((mag(Z) < cutoff) && (iterations < itMax)) {
						Z = add(mult(scale, boxformula(Z)), C);
						iterations++;
					}

					if ((iterations >= itMin) && (iterations < itMax)) {
						// place block if cutoff reached in itMin -> itMax range
						if ((iterations >= itMin) && (iterations < itMax)) {
							byte id = palette.get(iterations - itMin);
							// System.out.println("Placing block "+id+" at ("+(xOffset + x)+","+(yOffset + y)+","+(zOffset + z)+")");
							world.setBlock(id, xOffset + x, yOffset + y, zOffset + z);
						}
					}
				}
			}
		}
	}
	
	public void generateMandelboxChunks(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, 
			int x1, int y1, int z1) {
		// calculate the coordinates for the starting chunks
		int xs = x1 / csz;
		int ys = y1 / csz;
		int zs = z1 / csz;

		// calculate the coordinates for the ending chunks
		int xe = (x1 + d) / csz;
		int ye = (y1 + d) / csz;
		int ze = (z1 + d) / csz;

		// generate each chunk and set it in the world
		Long time = System.currentTimeMillis();
		DecimalFormat df = new DecimalFormat("#.##");
		int totalChunks = (xe - xs + 1)*(ye - ys + 1)*(ze - zs + 1);
		for (int x = xs; x <= xe; x++)
			for (int y = ys; y <= ye; y++)
				for (int z = zs; z <= ze; z++) {
					world.setChunk(x, y, z, generateMandelboxChunk(name, d, scale, cutoff, itMin, itMax, tfmult, 
							x1, y1, z1, x - xs, y - ys, z - zs));
					if ((System.currentTimeMillis() - time) > messageDelay) {
						int currentChunks = (ze - zs + 1)*(ye - ys + 1)*(x - xs) + (ze - zs + 1)*(y - ys) + (z - zs);
						double progress = (double) 100 * currentChunks/totalChunks;
						System.out.println(df.format(progress)+"%");
						time = System.currentTimeMillis();
					}
				}
	}

	// generate a single chunk that's part of a larger Menger Sponge
	public Chunk generateMandelboxChunk(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, 
			int x1, int y1, int z1, int xc, int yc, int zc) {
		// prepare the chunk and the palette
		BytePalette p = world.getPalette(name);
		Chunk c = new Chunk(csz);
		double tfsub = tfmult / 2;
		
		// here we calculate the upper limit in each Cartesian direction of the object
		// that we're generating. this is important for us to not attempt to generate
		// blocks outside of the object's actual dimensions
		int x2 = x1 + d;
		int y2 = y1 + d;
		int z2 = z1 + d;

		// we loop over the entire chunk, calculating Cartesian values relative to the
		// origin of the fractal we're generating. we also calculate the real Cartesian
		// values for each point to make sure we're in the range of the fractal we're
		// generating.
		for (int x = 0; x < csz; x++) {
			int xRelative = xc * csz + x - (x1 % csz);
			int xReal = xRelative + x1;
			if ((xReal < x1) || (xReal > x2))
				continue;
			double xtf = (tfmult * (xRelative)) / (d - 1) - tfsub;
			for (int y = 0; y < csz; y++) {
				int yRelative = yc * csz + y - (y1 % csz);
				int yReal = yRelative + y1;
				if ((yReal < y1) || (yReal > y2))
					continue;
				double ytf = (tfmult * (yRelative)) / (d - 1) - tfsub;
				for (int z = 0; z < csz; z++) {
					int zRelative = zc * csz + z - (z1 % csz);
					int zReal = zRelative + z1;
				    if ((zReal < z1) || (zReal > z2))
				    	continue;
					double ztf = (tfmult * (zRelative)) / (d - 1) - tfsub;
					
					int iterations = -1;
					double[] C = new double[] { xtf, ytf, ztf };
					double[] Z = new double[] { 0, 0, 0 };

					while ((mag(Z) < cutoff) && (iterations < itMax)) {
						Z = add(mult(scale, boxformula(Z)), C);
						iterations++;
					}

					if ((iterations >= itMin) && (iterations < itMax)) {
						// place block if cutoff reached in itMin -> itMax range
						if ((iterations >= itMin) && (iterations < itMax)) {
							byte id = p.get(iterations - itMin);
							c.setBlock(id, x, y, z);
						}
					}
				}
			}
		}

		return c;
	}

//	public void generateMandelboxChunks(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, int xo, int yo, int zo) {
//		int xlim = (xo + d) / csz;
//		int ylim = (yo + d) / csz;
//		int zlim = (zo + d) / csz;
//		int xs = xo / csz;
//		int ys = yo / csz;
//		int zs = zo / csz;
//		int xm = d;
//		int ym = d;
//		int zm = d;
//
//		int ixo = xo % csz;
//		for (int i = xs; i <= xlim; i++) {
//			int iyo = yo % csz;
//			ym = d;
//			zm = d;
//			for (int j = ys; j <= ylim; j++) {
//				int izo = zo % csz;
//				zm = d;
//				for (int k = zs; k <= zlim; k++) {
//					Chunk c = generateMandelboxChunk(name, d, scale, cutoff, itMin, itMax, tfmult, ixo, iyo, izo, (i - xs) * csz, (j - ys) * csz, (k - zs) * csz, xm, ym, zm);
//					world.setChunk(i, j, k, c);
//					zm -= csz - izo;
//					izo = 0;
//				}
//				ym -= csz - iyo;
//				iyo = 0;
//			}
//			xm -= csz - ixo;
//			ixo = 0;
//		}
//	}
//
//	public Chunk generateMandelboxChunk(String name, int d, double scale, double cutoff, int itMin, int itMax, double tfmult, int xo, int yo, int zo, int cxo, int cyo, int czo, int xm, int ym, int zm) {
//
//		Chunk chunk = new Chunk(64);
//		double tfsub = tfmult / 2;
//		BytePalette palette = world.getPalette(name);
//
//		for (int x = 0; (x < 64 - xo) && (x < xm); x++) {
//			double xc = (tfmult * (x + cxo)) / (d - 1) - tfsub;
//			for (int y = 0; (y < 64 - yo) && (y < ym); y++) {
//				double yc = (tfmult * (y + cyo)) / (d - 1) - tfsub;
//				for (int z = 0; (z < 64 - zo) && (z < zm); z++) {
//					double zc = (tfmult * (z + czo)) / (d - 1) - tfsub;
//
//					int iterations = -1;
//					double[] C = new double[] { xc, yc, zc };
//					double[] Z = new double[] { 0, 0, 0 };
//
//					while ((mag(Z) < cutoff) && (iterations < itMax)) {
//						Z = add(mult(scale, boxformula(Z)), C);
//						iterations++;
//					}
//
//					if ((iterations >= itMin) && (iterations < itMax)) {
//						// place block if cutoff reached in itMin -> itMax range
//						if ((iterations >= itMin) && (iterations < itMax)) {
//							byte id = palette.get(iterations - itMin);
//							chunk.setBlock(id, x + xo, y + yo, z + zo);
//						}
//					}
//				}
//			}
//		}
//
//		return chunk;
//	}

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

		double[] output = new double[] { x, y, z };
		double m = mag(output);

		if (m < 0.5)
			output = mult(4, output);
		else if (m < 1)
			output = mult(1 / (m * m), output);

		return output;
	}

	// multiplies a constant and a vector together
	private double[] mult(double c, double[] vec) {
		double[] output = new double[] { c * vec[0], c * vec[1], c * vec[2] };

		return output;
	}

	/***************************************************************
	 ********************* Greek Cross Methods *********************
	 ***************************************************************/

	// greek cross fractal
	public void generateCross(byte id, int d0, int xOffset, int yOffset, int zOffset, int scale, int mode) {
		// set recursion goal
		if (recursionGoal == 0) {
			recursionGoal = getCrossRecursions(d0, scale);
			// System.out.println("Greek Cross recursion goal: "+recursionGoal);
			time = System.currentTimeMillis();
		}

		// increment recursion counter
		recursionCounter++;

		// print recursion message once every messageDelay seconds
		if (System.currentTimeMillis() - time > messageDelay) {
			double recursionProgress = (double) 100 * (recursionCounter) / (recursionGoal);
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println(df.format(recursionProgress) + "%");
			time = System.currentTimeMillis();
		}

		int d1 = (d0 - 1) / 2;
		if ((floor(d1) - d1 != 0) || (d1 < scale))
			return;
		// make cross
		if (mode != 1) {
			for (int x = 0; x < d0; x++) {
				if (x == d1)
					continue;
				world.setBlock(id, x + xOffset, d1 + yOffset, d1 + zOffset);
			}
		}
		if (mode != 2) {
			for (int y = 0; y < d0; y++) {
				if (y == d1)
					continue;
				world.setBlock(id, d1 + xOffset, y + yOffset, d1 + zOffset);
			}
		}
		if (mode != 3) {
			for (int z = 0; z < d0; z++) {
				if (z == d1)
					continue;
				world.setBlock(id, d1 + xOffset, d1 + yOffset, z + zOffset);
			}
		}
		d1++;
		// recursion close
		generateCross(id, d1 - 1, 0 + xOffset, d1 / 2 + yOffset, d1 / 2 + zOffset, scale, 1);
		generateCross(id, d1 - 1, d1 / 2 + xOffset, 0 + yOffset, d1 / 2 + zOffset, scale, 2);
		generateCross(id, d1 - 1, d1 / 2 + xOffset, d1 / 2 + yOffset, 0 + zOffset, scale, 3);
		// recursion far
		generateCross(id, d1 - 1, d1 + xOffset, d1 / 2 + yOffset, d1 / 2 + zOffset, scale, 1);
		generateCross(id, d1 - 1, d1 / 2 + xOffset, d1 + yOffset, d1 / 2 + zOffset, scale, 2);
		generateCross(id, d1 - 1, d1 / 2 + xOffset, d1 / 2 + yOffset, d1 + zOffset, scale, 3);
	}

	private int getCrossRecursions(int d0, int scale) {
		int recursions = 0;
		int depth = 0;
		while (d0 > scale) {
			d0 /= 2;
			depth++;
		}
		while (depth >= 0) {
			recursions += pow(6, depth);
			depth--;
		}

		return recursions;
	}

	/***************************************************************
	 ********************* Octahedron Methods *********************
	 ***************************************************************/

	public void generateOctahedron(byte id, int d0, int xOffset, int yOffset, int zOffset, int oscale) {
		// set recursion goal
		if (recursionGoal == 0) {
			recursionGoal = getCrossRecursions(d0, oscale);
			// System.out.println("Greek Cross recursion goal: "+recursionGoal);
			time = System.currentTimeMillis();
		}

		// increment recursion counter
		recursionCounter++;

		// print recursion message once every messageDelay seconds
		if (System.currentTimeMillis() - time > messageDelay) {
			double recursionProgress = (double) 100 * (recursionCounter) / (recursionGoal);
			DecimalFormat df = new DecimalFormat("#.##");
			System.out.println(df.format(recursionProgress) + "%");
			time = System.currentTimeMillis();
		}

		int d1 = (d0 - 1) / 2;
		int d2 = (d1 - 1) / 2;
		// create octahedron when minimum scale reached
		if (d1 < oscale) {
			int width = 0;
			for (int y = 0; y < d0; y++) {
				for (int z = 0; z < d0; z++) {
					for (int x = 0; x < d0; x++) {
						if (abs(z - d1) + abs(x - d1) <= width) {
							world.setBlock(id, x + xOffset, y + yOffset, z + zOffset);
						}
					}
				}
				if (y > d1 - 1)
					width--;
				else
					width++;
			}
		} else { // recursion
			generateOctahedron(id, d1, d2 + 1 + xOffset, d1 + 1 + yOffset, d2 + 1 + zOffset, oscale); // top octahedron
			generateOctahedron(id, d1, d2 + 1 + xOffset, yOffset, d2 + 1 + zOffset, oscale); // bottom octahedron
			generateOctahedron(id, d1, xOffset, d2 + 1 + yOffset, d2 + 1 + zOffset, oscale); // close right octahedron
			generateOctahedron(id, d1, d2 + 1 + xOffset, d2 + 1 + yOffset, zOffset, oscale); // close left octahedron
			generateOctahedron(id, d1, d2 + 1 + xOffset, d2 + 1 + yOffset, d1 + 1 + zOffset, oscale); // far right octahedron
			generateOctahedron(id, d1, d1 + 1 + xOffset, d2 + 1 + yOffset, d2 + 1 + zOffset, oscale); // far left octahedron
		}
	}
}
