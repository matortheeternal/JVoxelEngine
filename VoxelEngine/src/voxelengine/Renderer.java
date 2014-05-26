package voxelengine;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.image.*;
import java.util.concurrent.*;

public class Renderer {
	private World world;
	private Color SkyboxColor = Color.BLACK;
	private double falloff = 4.0;
	private double rate = 0.6;
	private double ambientIntensity = 0.2;
	private double directionalIntensity = 0.3;
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

	int[] mem = new int[0];
	MemoryImageSource fb;

	public MemoryImageSource renderG(int width, int height, int pixelScale, int castScale, boolean doShadows) {
		if (mem.length != width * height) {
			mem = new int[width * height]; // this is very marginally faster than recreating it every frame
		}
		double yaw = camera.rotY;
		double pitch = camera.rotX;
		double[][] ref = new double[][] { { sin(pitch) * cos(yaw), sin(pitch) * sin(yaw), -cos(pitch) },
				{ -sin(yaw), cos(yaw), 0 }, // equal to cos(yaw + PI/2), sin(yaw + PI/2), 0
				{ cos(pitch) * cos(yaw), cos(pitch) * sin(yaw), 2 * sin(pitch) } // cross product of the two vectors
																					// above
		};

		// raycast for each pixel
		ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int py = 0; py < height - pixelScale; py += pixelScale) {
			for (int px = 0; px < width - pixelScale; px += pixelScale) {
				if (pixelScale == 1) {
					service.execute(new PixelWorker(mem, new int[] { px + py * width }, px, py, width, height, ref,
							doShadows));
				} else {
					int[] fbIndices = new int[pixelScale * pixelScale];
					for (int pys = 0; pys < pixelScale; ++pys) {
						for (int pxs = 0; pxs < pixelScale; ++pxs) {
							fbIndices[pys * pixelScale + pxs] = px + pxs + (py + pys) * width;
						}
					}
					service.execute(new PixelWorker(mem, fbIndices, px, py, width, height, ref, doShadows));
				}
			}
		}
		try {
			service.shutdown();
			service.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (fb == null) {
			fb = new MemoryImageSource(width, height, mem, 0, width);
		} else {
			fb.newPixels(); // this is very marginally faster than recreating it
		}

		// return image source
		return fb;
	}

	private RayCastCollision raycastGScreenCoords(int px, int py, int width, int height, double[][] ref) {
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

		return raycastG(x, y, z, ray);
	}

	private RayCastCollision raycastG(double x, double y, double z, double[] ray) {

		double startX = x;
		double startY = y;
		double startZ = z;

		double dx = ray[0] * renderDistance * 16;
		double dy = ray[1] * renderDistance * 16;
		double dz = ray[2] * renderDistance * 16;

		double rayMagnitude = Math.sqrt(dx * dx + dy * dy + dz * dz);

		double ax, ay, az;

		int sx, sy, sz, n;

		double sv = Double.MIN_NORMAL;

		sx = (int) Math.signum(dx);
		sy = (int) Math.signum(dy);
		sz = (int) Math.signum(dz);

		ax = Math.abs(dx) / rayMagnitude;
		ay = Math.abs(dy) / rayMagnitude;
		az = Math.abs(dz) / rayMagnitude;

		ax = ((ax > sv) ? ax : sv);
		ay = ((ay > sv) ? ay : sv);
		az = ((az > sv) ? az : sv);

		double tDeltaX = 1 / ax;
		double tDeltaY = 1 / ay;
		double tDeltaZ = 1 / az;

		double tMaxX = Math.abs((sx == 1) ? (1 - (x % 1.0)) : (x % 1.0)) / ax;
		double tMaxY = Math.abs((sy == 1) ? (1 - (y % 1.0)) : (y % 1.0)) / ay;
		double tMaxZ = Math.abs((sz == 1) ? (1 - (z % 1.0)) : (z % 1.0)) / az;

		n = (int) (Math.abs(dx) + Math.abs(dy) + Math.abs(dz));

		int face = -1;

		while (n-- != 0) {
			if (tMaxX < tMaxY) {
				if (tMaxX < tMaxZ) {
					face = 0;
					x += sx;
					tMaxX += tDeltaX;
				} else {
					face = 2;
					z += sz;
					tMaxZ += tDeltaZ;
				}
			} else {
				if (tMaxY < tMaxZ) {
					face = 1;
					y += sy;
					tMaxY += tDeltaY;
				} else {
					face = 2;
					z += sz;
					tMaxZ += tDeltaZ;
				}
			}
			Block block = world.getBlock(x, y, z);
			if (block != null) {
				double t = 0;
				switch (face) {
				case 0:
					t = tMaxX - tDeltaX - 0.01;
					break;
				case 1:
					t = tMaxY - tDeltaY - 0.01;
					break;
				case 2:
					t = tMaxZ - tDeltaZ - 0.01;
					break;
				}
				return new RayCastCollision(block, face, t * dx / rayMagnitude + startX,
						t * dy / rayMagnitude + startY, t * dz / rayMagnitude + startZ);
			}
		}

		return null;
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

	private Color CalculateColor(Color c, double x1, double x2, double y1, double y2, double z1, double z2, int face,
			double directionalDot) {
		double ray[] = { x1 - x2, y1 - y2, z1 - z2 };
		double distance = Math.sqrt(ray[0] * ray[0] + ray[1] * ray[1] + ray[2] * ray[2]);
		ray[0] /= distance;
		ray[1] /= distance;
		ray[2] /= distance;
		double dot = 0;
		switch (face) {
		case 0:
			dot = Math.abs(ray[0]);
			break;
		case 1:
			dot = Math.abs(ray[1]);
			break;
		case 2:
			dot = Math.abs(ray[2]);
			break;
		}
		double diffuseIntensity = Math.max(falloff / Math.pow(distance, rate), 1.0) * dot;
		double lightIntensity = ambientIntensity + (1 - ambientIntensity - directionalIntensity) * diffuseIntensity;

		lightIntensity += directionalIntensity * directionalDot;

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

	private class PixelWorker implements Runnable {

		private int[] framebuffer;
		private int[] fbIndices;
		private int px;
		private int py;
		private int width;
		private int height;
		private double[][] ref;
		private boolean doShadows;

		public PixelWorker(int[] framebuffer, int[] fbIndices, int px, int py, int width, int height, double[][] ref,
				boolean doShadows) {
			this.framebuffer = framebuffer;
			this.fbIndices = fbIndices;
			this.px = px;
			this.py = py;
			this.width = width;
			this.height = height;
			this.ref = ref;
			this.doShadows = doShadows;
		}

		double[] lightRay = { renderDistance * 8, renderDistance * 2, renderDistance * 4 };

		@Override
		public void run() {
			RayCastCollision result = Renderer.this.raycastGScreenCoords(px, py, width, height, ref);
			Color c;
			double lightRayMagnitude = Math.sqrt(lightRay[0] * lightRay[0] + lightRay[1] * lightRay[1] + lightRay[2]
					* lightRay[2]);
			if (result != null) {
				Block block = result.getCollisionBlock();
				double x = result.getX();
				double y = result.getY();
				double z = result.getZ();
				double lightDot = 0;
				if (doShadows) {
					RayCastCollision lightOcclusion = Renderer.this.raycastG(result.getX(), result.getY(),
							result.getZ(), lightRay);
					if (lightOcclusion == null) {
						switch (result.getFace()) {
						case 0:
							lightDot = Math.abs(lightRay[0] / lightRayMagnitude);
							break;
						case 1:
							lightDot = Math.abs(lightRay[1] / lightRayMagnitude);
							break;
						case 2:
							lightDot = Math.abs(lightRay[2] / lightRayMagnitude);
							break;
						}
					}
				}
				c = Renderer.this.CalculateColor(block.getType().getColor(), camera.x, x, camera.y, y, camera.z, z,
						result.getFace(), lightDot);
			} else {
				c = SkyboxColor;
			}
			synchronized (framebuffer) {
				for (int index : fbIndices) {
					framebuffer[index] = c.getRGB();
				}
			}
		}

	}
}