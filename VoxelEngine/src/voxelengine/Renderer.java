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
		int[] mem = new int[width*height];
		
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
		double w2 = width/2;
		double h2 = height/2;
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
			cx1 = (int) (x1/16.0);
			cy1 = (int) (y1/16.0);
			cz1 = (int) (z1/16.0);
			if (world.getIsEmpty(cx1, cy1, cz1)) {
				do {
					x1 += xs;
					y1 += ys;
					z1 += zs;
					cx2 = (int) (x1/16.0);
					cy2 = (int) (y1/16.0);
					cz2 = (int) (z1/16.0);
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
		int[] mem = new int[width*height];
		double yaw = camera.rotY;
		double pitch = camera.rotX;
		double[][] ref = new double[][] {
				{sin(pitch)*cos(yaw), sin(pitch)*sin(yaw), -cos(pitch)},
				{-sin(yaw), cos(yaw), 0}, // equal to cos(yaw + PI/2), sin(yaw + PI/2), 0
				{cos(pitch)*cos(yaw), cos(pitch)*sin(yaw), 2*sin(pitch)} // cross product of the two vectors above
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
		double w2 = width/2;
		double h2 = height/2;
		double fov = camera.fov/2;
		double yawr = ((px - w2) / w2) * fov;
		double pitchr = ((py - h2) / h2) * fov * 0.5; // correction because view window isn't 1:1
		
		double x1 = camera.x;
		double y1 = camera.y;
		double z1 = camera.z;
		int x2, y2, z2;
		int cx1, cy1, cz1, cx2, cy2, cz2;
		
		double[] ray = new double[]{ cos(pitchr)*cos(yawr), cos(pitchr)*sin(yawr), -sin(pitchr) };
		ray = new double[] {
			ray[0] * ref[0][0] + ray[1] * ref[1][0] + ray[2] * ref[2][0],
		    ray[0] * ref[0][1] + ray[1] * ref[1][1] + ray[2] * ref[2][1],
		    ray[0] * ref[0][2] + ray[1] * ref[1][2] + ray[2] * ref[2][2],
		};
		double xs = ray[0]/castScale;
		double ys = ray[1]/castScale;
		double zs = ray[2]/castScale;
		
		while (inBoundsC(x1, y1, z1)) {
			// chunk processing
			cx1 = (int) (x1/16.0);
			cy1 = (int) (y1/16.0);
			cz1 = (int) (z1/16.0);
			if (world.getIsEmpty(cx1, cy1, cz1)) {
				do {
					x1 += xs;
					y1 += ys;
					z1 += zs;
					cx2 = (int) (x1/16.0);
					cy2 = (int) (y1/16.0);
					cz2 = (int) (z1/16.0);
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

	private Color CalculateColor(Color c, double x1, double x2, double y1, double y2, double z1, double z2) {
		double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));
		double lightIntensity = falloff/Math.pow(distance, rate);
		
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
