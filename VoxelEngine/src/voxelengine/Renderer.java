package voxelengine;

import java.awt.Color;
import java.awt.image.MemoryImageSource;
import static java.lang.Math.*;

public class Renderer {
	private World world;
	private Color SkyboxColor = Color.BLACK;
	private double falloff = 8.0;
	private double rate = 0.85;
	private int renderDistance = 8;
	public Camera camera;

	// constructor
	public Renderer(World world, Camera camera) {
		super();
		this.world = world;
		this.camera = camera;
	}

	public MemoryImageSource renderC(int width, int height, int pixelScale, int castScale) {
		int[] mem = new int[width * height];

		// raycast for each pixel
		for (int py = 0; py < height - pixelScale; py += pixelScale) {
			for (int px = 0; px < width - pixelScale; px += pixelScale) {
				if (pixelScale == 1)
					mem[px + py * width] = raycastC(px, py, width, height, castScale);
				else {
					int val = raycastC(px, py, width, height, castScale);
					for (int pys = 0; pys < pixelScale; ++pys) {
						for (int pxs = 0; pxs < pixelScale; ++pxs) {
							mem[px + pxs + (py + pys) * width] = val;
						}
					}
				}
			}
		}

		// return image source
		return new MemoryImageSource(width, height, mem, 0, width);
	}

	private int raycastC(int px, int py, int width, int height, int castScale) {
		double w2 = width / 2;
		double h2 = height / 2;
		double yaw = camera.rotY + ((px - w2) / w2) * Math.PI / 4;
		double pitch = camera.rotX + ((py - h2) / h2) * Math.PI / 4;

		double x1 = camera.x;
		double y1 = camera.y;
		double z1 = camera.z;
		int x2, y2, z2;
		int cx1, cy1, cz1, cx2, cy2, cz2;

		double xs = Math.sin(pitch) * Math.cos(yaw) / castScale;
		double ys = Math.sin(pitch) * Math.sin(yaw) / castScale;
		double zs = Math.cos(pitch) / castScale;

		while (inBoundsC(x1, y1, z1)) {
			// chunk processing
			cx1 = (int) (x1 / 16.0);
			cy1 = (int) (y1 / 16.0);
			cz1 = (int) (z1 / 16.0);
			if (world.getIsEmpty(cx1, cy1, cz1)) {
				do {
					x1 += xs;
					y1 += ys;
					z1 += zs;
					cx2 = (int) (x1 / 16.0);
					cy2 = (int) (y1 / 16.0);
					cz2 = (int) (z1 / 16.0);
				} while (cx1 == cx2 && cy1 == cy2 && cz1 == cz2);
				continue;
			}

			// block processing
			Block block = world.getBlock(x1, y1, z1);
			if (block != null) {
				Color c = block.getType().getColor();
				c = CalculateColor(c, camera.x, x1, camera.y, y1, camera.z, z1);
				return c.getRGB();
			}

			x2 = (int) x1;
			y2 = (int) y1;
			z2 = (int) z1;
			do {
				x1 += xs;
				y1 += ys;
				z1 += zs;
			} while ((int) x1 == x2 && (int) y1 == y2 && (int) z1 == z2);
		}

		return SkyboxColor.getRGB();
	}

	private boolean inBoundsC(double x, double y, double z) {
		int b = world.getSize() * 16;
		int dx = ((int) Math.abs(camera.x - x)) / 16;
		int dy = ((int) Math.abs(camera.y - y)) / 16;
		int dz = ((int) Math.abs(camera.z - z)) / 16;
		return x >= 0 && x < b && y >= 0 && y < b && z >= 0 && z < b && dx < renderDistance && dy < renderDistance && dz < renderDistance;
	}

	public MemoryImageSource renderE(int width, int height, int pixelScale, int castScale) {
		int[] mem = new int[width * height];
		double yaw = camera.rotY;
		double pitch = camera.rotX;
		double[][] ref = new double[][] { { sin(pitch) * cos(yaw), sin(pitch) * sin(yaw), -cos(pitch) }, { -sin(yaw), cos(yaw), 0 }, // equal to cos(yaw + PI/2), sin(yaw + PI/2), 0
				{ cos(pitch) * cos(yaw), cos(pitch) * sin(yaw), 2 * sin(pitch) } // cross product of the two vectors above
		};

		// raycast for each pixel
		for (int py = 0; py < height - pixelScale; py += pixelScale) {
			for (int px = 0; px < width - pixelScale; px += pixelScale) {
				if (pixelScale == 1)
					mem[px + py * width] = raycastE(px, py, width, height, castScale, ref);
				else {
					int val = raycastE(px, py, width, height, castScale, ref);
					for (int pys = 0; pys < pixelScale; ++pys) {
						for (int pxs = 0; pxs < pixelScale; ++pxs) {
							mem[px + pxs + (py + pys) * width] = val;
						}
					}
				}
			}
		}

		// return image source
		return new MemoryImageSource(width, height, mem, 0, width);
	}

	private int raycastE(int px, int py, int width, int height, int castScale, double[][] ref) {
		double w2 = width / 2;
		double h2 = height / 2;
		double fovH = camera.fovH / 2;
		double fovV = camera.fovV / 2;
		double yawr = ((px - w2) / w2) * fovH;
		double pitchr = ((py - h2) / h2) * fovV; // correction because view window isn't 1:1

		double x1 = camera.x;
		double y1 = camera.y;
		double z1 = camera.z;
		int x2, y2, z2;
		int cx1, cy1, cz1, cx2, cy2, cz2;

		double[] ray = new double[] { cos(pitchr) * cos(yawr), cos(pitchr) * sin(yawr), -sin(pitchr) };
		ray = new double[] { ray[0] * ref[0][0] + ray[1] * ref[1][0] + ray[2] * ref[2][0], ray[0] * ref[0][1] + ray[1] * ref[1][1] + ray[2] * ref[2][1], ray[0] * ref[0][2] + ray[1] * ref[1][2] + ray[2] * ref[2][2], };
		double xs = ray[0] / castScale;
		double ys = ray[1] / castScale;
		double zs = ray[2] / castScale;

		while (inBoundsC(x1, y1, z1)) {
			// chunk processing
			cx1 = (int) (x1 / 16.0);
			cy1 = (int) (y1 / 16.0);
			cz1 = (int) (z1 / 16.0);
			if (world.getIsEmpty(cx1, cy1, cz1)) {
				do {
					x1 += xs;
					y1 += ys;
					z1 += zs;
					cx2 = (int) (x1 / 16.0);
					cy2 = (int) (y1 / 16.0);
					cz2 = (int) (z1 / 16.0);
				} while (cx1 == cx2 && cy1 == cy2 && cz1 == cz2);
				continue;
			}

			// block processing
			Block block = world.getBlock(x1, y1, z1);
			if (block != null) {
				Color c = block.getType().getColor();
				c = CalculateColor(c, camera.x, x1, camera.y, y1, camera.z, z1);
				return c.getRGB();
			}

			x2 = (int) x1;
			y2 = (int) y1;
			z2 = (int) z1;
			do {
				x1 += xs;
				y1 += ys;
				z1 += zs;
			} while ((int) x1 == x2 && (int) y1 == y2 && (int) z1 == z2);
		}

		return SkyboxColor.getRGB();
	}

	public MemoryImageSource renderF(int width, int height, int pixelScale, int castScale) {
		int[] mem = new int[width * height];
		double yaw = camera.rotY;
		double pitch = camera.rotX;
		double[][] ref = new double[][] { { sin(pitch) * cos(yaw), sin(pitch) * sin(yaw), -cos(pitch) }, { -sin(yaw), cos(yaw), 0 }, // equal to cos(yaw + PI/2), sin(yaw + PI/2), 0
				{ cos(pitch) * cos(yaw), cos(pitch) * sin(yaw), 2 * sin(pitch) } // cross product of the two vectors above
		};

		// raycast for each pixel
		for (int py = 0; py < height - pixelScale; py += pixelScale) {
			for (int px = 0; px < width - pixelScale; px += pixelScale) {
				// if (camera.rotX != PI/2) System.out.println("Raycasting "+px+","+py);
				if (pixelScale == 1)
					mem[px + py * width] = raycastF(px, py, width, height, ref);
				else {
					int val = raycastF(px, py, width, height, ref);
					for (int pys = 0; pys < pixelScale; ++pys) {
						for (int pxs = 0; pxs < pixelScale; ++pxs) {
							mem[px + pxs + (py + pys) * width] = val;
						}
					}
				}
			}
		}

		// return image source
		return new MemoryImageSource(width, height, mem, 0, width);
	}

	private int raycastF(int px, int py, int width, int height, double[][] ref) {
		double sv = 0.00000001;
		double w2 = width / 2;
		double h2 = height / 2;
		double fovH = camera.fovH / 2;
		double fovV = camera.fovV / 2;
		double yawr = ((px - w2) / w2) * fovH;
		double pitchr = ((py - h2) / h2) * fovV; // correction because view window isn't 1:1

		double x1 = camera.x;
		double y1 = camera.y;
		double z1 = camera.z;
		int x2, y2, z2;
		int cx1, cy1, cz1, cx2, cy2, cz2;
		double i1, i2, i3;

		double[] ray = new double[] { cos(pitchr) * cos(yawr), cos(pitchr) * sin(yawr), -sin(pitchr) };
		ray = new double[] { ray[0] * ref[0][0] + ray[1] * ref[1][0] + ray[2] * ref[2][0], ray[0] * ref[0][1] + ray[1] * ref[1][1] + ray[2] * ref[2][1], ray[0] * ref[0][2] + ray[1] * ref[1][2] + ray[2] * ref[2][2], };
		double xs = ray[0];
		double ys = ray[1];
		double zs = ray[2];

		while (inBoundsC(x1, y1, z1)) {
			// add/subtract sv if we're on a border exactly
			if (x1 % 1.0 == 0)
				x1 += (xs > 0) ? sv : -sv;
			if (y1 % 1.0 == 0)
				y1 += (ys > 0) ? sv : -sv;
			if (z1 % 1.0 == 0)
				z1 += (zs > 0) ? sv : -sv;

			// chunk processing
			cx1 = (int) (x1 / 16.0);
			cy1 = (int) (y1 / 16.0);
			cz1 = (int) (z1 / 16.0);
			if (world.getIsEmpty(cx1, cy1, cz1)) {
				i1 = (xs == 0) ? 999999999 : (xs > 0) ? abs((16.0 - x1 % 16.0) / xs) : abs((x1 % 16.0) / xs);
				i2 = (ys == 0) ? 999999999 : (ys > 0) ? abs((16.0 - y1 % 16.0) / ys) : abs((y1 % 16.0) / ys);
				i3 = (zs == 0) ? 999999999 : (zs > 0) ? abs((16.0 - z1 % 16.0) / zs) : abs((z1 % 16.0) / zs);
				if ((i1 <= i2) && (i1 <= i3)) {
					// step by i1
					x1 += xs * (i1 + sv);
					y1 += ys * (i1 + sv);
					z1 += zs * (i1 + sv);
				} else if ((i2 <= i1) && (i2 <= i3)) {
					// step by i2
					x1 += xs * (i2 + sv);
					y1 += ys * (i2 + sv);
					z1 += zs * (i2 + sv);
				} else {
					// step by i3
					x1 += xs * (i3 + sv);
					y1 += ys * (i3 + sv);
					z1 += zs * (i3 + sv);
				}
				continue;
			}

			// block processing
			Block block = world.getBlock(x1, y1, z1);
			if (block != null) {
				Color c = block.getType().getColor();
				c = CalculateColor(c, camera.x, x1, camera.y, y1, camera.z, z1);
				return c.getRGB();
			}

			i1 = (xs == 0) ? 999999999 : (xs > 0) ? abs((1.0 - x1 % 1.0) / xs) : abs((x1 % 1.0) / xs);
			i2 = (ys == 0) ? 999999999 : (ys > 0) ? abs((1.0 - y1 % 1.0) / ys) : abs((y1 % 1.0) / ys);
			i3 = (zs == 0) ? 999999999 : (zs > 0) ? abs((1.0 - z1 % 1.0) / zs) : abs((z1 % 1.0) / zs);
			if ((i1 <= i2) && (i1 <= i3)) {
				// step by i1
				x1 += xs * (i1 + sv);
				y1 += ys * (i1 + sv);
				z1 += zs * (i1 + sv);
			} else if ((i2 <= i1) && (i2 <= i3)) {
				// step by i2
				x1 += xs * (i2 + sv);
				y1 += ys * (i2 + sv);
				z1 += zs * (i2 + sv);
			} else {
				// step by i3
				x1 += xs * (i3 + sv);
				y1 += ys * (i3 + sv);
				z1 += zs * (i3 + sv);
			}
		}

		return SkyboxColor.getRGB();
	}

	public MemoryImageSource renderG(int width, int height, int pixelScale, int castScale) {
		int[] mem = new int[width * height];
		double yaw = camera.rotY;
		double pitch = camera.rotX;
		double[][] ref = new double[][] { { sin(pitch) * cos(yaw), sin(pitch) * sin(yaw), -cos(pitch) },
				{ -sin(yaw), cos(yaw), 0 }, // equal to cos(yaw + PI/2), sin(yaw + PI/2), 0
				{ cos(pitch) * cos(yaw), cos(pitch) * sin(yaw), 2 * sin(pitch) } // cross product of the two vectors
																					// above
		};

		// raycast for each pixel
		for (int py = 0; py < height - pixelScale; py += pixelScale) {
			for (int px = 0; px < width - pixelScale; px += pixelScale) {
				if (pixelScale == 1)
					mem[px + py * width] = raycastG(px, py, width, height, castScale, ref);
				else {
					int val = raycastG(px, py, width, height, castScale, ref);
					for (int pys = 0; pys < pixelScale; ++pys) {
						for (int pxs = 0; pxs < pixelScale; ++pxs) {
							mem[px + pxs + (py + pys) * width] = val;
						}
					}
				}
			}
		}

		// return image source
		return new MemoryImageSource(width, height, mem, 0, width);
	}

	private int raycastG(int px, int py, int width, int height, int castScale, double[][] ref) {
		double w2 = width / 2.0;
		double h2 = height / 2.0;

		double x = camera.x;
		double y = camera.y;
		double z = camera.z;

		double fovH = camera.fovH / 2;
		double fovV = camera.fovV / 2;
		double yawr = ((px - w2) / w2) * fovH;
		double pitchr = ((py - h2) / h2) * fovV; // correction because view window isn't 1:1

		double[] ray = new double[] { cos(pitchr) * cos(yawr), cos(pitchr) * sin(yawr), -sin(pitchr) };
		ray = new double[] { ray[0] * ref[0][0] + ray[1] * ref[1][0] + ray[2] * ref[2][0],
				ray[0] * ref[0][1] + ray[1] * ref[1][1] + ray[2] * ref[2][1],
				ray[0] * ref[0][2] + ray[1] * ref[1][2] + ray[2] * ref[2][2], };

		double dx = ray[0] * renderDistance * 16;
		double dy = ray[1] * renderDistance * 16;
		double dz = ray[2] * renderDistance * 16;

		double exy, exz, ezy, ax, ay, az, bx, by, bz;

		int sx, sy, sz, n;

		sx = (int) Math.signum(dx);
		sy = (int) Math.signum(dy);
		sz = (int) Math.signum(dz);

		ax = Math.abs(dx);
		ay = Math.abs(dy);
		az = Math.abs(dz);

		bx = 2 * ax;
		by = 2 * ay;
		bz = 2 * az;

		exy = ay - ax;
		exz = az - ax;
		ezy = ay - az;

		n = (int) (ax + ay + az);

		while (n-- != 0) {
			Block block = world.getBlock(x, y, z);
			if (block != null) {
				Color c = block.getType().getColor();
				c = CalculateColor(c, camera.x, x, camera.y, y, camera.z, z);
				return c.getRGB();
			}

			if (exy < 0) {
				if (exz < 0) {
					x += sx;
					exy += by;
					exz += bz;
				} else {
					z += sz;
					exz -= bx;
					ezy += by;
				}
			} else {
				if (ezy < 0) {
					z += sz;
					exz -= bx;
					ezy += by;
				} else {
					y += sy;
					exy -= bx;
					ezy -= bz;
				}
			}
		}

		return SkyboxColor.getRGB();
	}

	private Color CalculateColor(Color c, double x1, double x2, double y1, double y2, double z1, double z2) {
		double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
		double lightIntensity = falloff / Math.pow(distance, rate);

		// calculate color components
		int red = (int) (c.getRed() * lightIntensity);
		red = (red > 255) ? 255 : red;
		int green = (int) (c.getGreen() * lightIntensity);
		green = (green > 255) ? 255 : green;
		int blue = (int) (c.getBlue() * lightIntensity);
		blue = (blue > 255) ? 255 : blue;

		// return calculated color
		return new Color(red, green, blue);
	}
}