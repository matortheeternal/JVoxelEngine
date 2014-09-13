package voxelengine;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

@SuppressWarnings("serial")
public class SwingInterface extends JPanel {
	private static Keyboard keyboard = new Keyboard();
	private Image src = null;
	private World world;
	private String worldName = "";
	private Renderer renderer;
	private Camera camera = new Camera(0, 0, 0, 0, 0, 0, Math.PI / 2, Math.PI / 6);
	public double speed = 1.0;
	private static String[] arguments;

	private static final int X_SIZE = 800;
	private static final int Y_SIZE = 600;
	private static final int X_BIG = 1600;
	private static final int Y_BIG = 1200;
	private static final int csz = 64;
	private static int pixelScale = 4;
	private static int castScale = 4;

	private static boolean doShadows = false;

	public SwingInterface() {
		new Worker().execute();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (src != null)
			g.drawImage(src, 0, 0, this);
	}

	private class Worker extends SwingWorker<Void, Image> {

		protected void process(List<Image> chunks) {
			for (Image bufferedImage : chunks) {
				src = bufferedImage;
				repaint();
			}
		}

		protected Void doInBackground() throws Exception {
			set_up();
			int frames = 0;
			long time = System.currentTimeMillis();

			long lastFrameNanos = System.nanoTime();

			while (true) {
				frames++;
				if (frames == 30) {
//					world.manageChunks((int) (camera.x / csz), (int) (camera.y / csz), (int) (camera.z / csz), false);
					world.decompressAllChunks((int) (camera.x / csz), (int) (camera.y / csz), (int) (camera.z / csz));
					double t = System.currentTimeMillis() - time;
					double fps = 30 / (t / 1000);
					System.out.println("(" + ((int) camera.x) + "," + ((int) camera.y) + "," + ((int) camera.z) + ") "
							+ ((double) Math.round(fps * 100) / 100) + "fps");
					frames = 0;
					time = System.currentTimeMillis();
				}
				process_input((System.nanoTime() - lastFrameNanos) / 1000000000.0);
				lastFrameNanos = System.nanoTime();
				Image img = createImage(renderer.renderG(X_SIZE, Y_SIZE, pixelScale, castScale, doShadows));
				BufferedImage buffer = new BufferedImage(X_SIZE, Y_SIZE, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = buffer.createGraphics();
				g2.drawImage(img, 0, 0, null);
				g2.dispose();
				publish(buffer);
			}
		}

		private void set_up() {
			int worldSize = Integer.parseInt(arguments[0]);
			int objectSize = Integer.parseInt(arguments[3]);
			int offset = (worldSize * csz - objectSize) / 2;
			world = new World(worldSize);
			world.addColorBlockTypes();
			world.addPalettes();
			long time = System.currentTimeMillis();
			if (arguments[1].equals("Menger Sponge")) {
				System.out.println("Generating Menger Sponge...");
				Byte b = (byte) world.getByteFromName(arguments[2]);
//				world.getGenerator().generateMengerSponge(b, objectSize, offset, offset, offset);
				world.getGenerator().generateMengerChunks(b, objectSize, offset, offset, offset);
			} else if (arguments[1].equals("Mandelbulb")) {
				System.out.println("Generating Mandelbulb...");
				world.getGenerator().generateMandelbulb(arguments[2], objectSize,
						Double.parseDouble(arguments[6]), Double.parseDouble(arguments[8]), offset, offset, offset,
						Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]));
			} else if (arguments[1].equals("Mandelbox")) {
				System.out.println("Generating Mandelbox...");
//				world.getGenerator().generateMandelbox(arguments[2], objectSize,
//						Double.parseDouble(arguments[6]), Double.parseDouble(arguments[8]),
//						Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]),
//						Double.parseDouble(arguments[7]), offset, offset, offset);
				world.getGenerator().generateMandelboxChunks(arguments[2], objectSize,
					Double.parseDouble(arguments[6]), Double.parseDouble(arguments[8]),
					Integer.parseInt(arguments[4]), Integer.parseInt(arguments[5]),
					Double.parseDouble(arguments[7]), offset, offset, offset);
			} else if (arguments[1].equals("Greek Cross")) {
				System.out.println("Generating Greek Cross Fractal...");
				Byte b = (byte) world.getByteFromName(arguments[2]);
				world.getGenerator().generateCross(b, objectSize, offset, offset, offset,
						Integer.parseInt(arguments[6]), 0);
//				System.out.println(world.getGenerator().getCounter());
			} else if (arguments[1].equals("Octahedron")) {
				System.out.println("Generating Octahedron Fractal...");
				Byte b = (byte) world.getByteFromName(arguments[2]);
				world.getGenerator().generateOctahedron(b, objectSize, offset, offset, offset,
						Integer.parseInt(arguments[6]));
//				System.out.println(world.getGenerator().getCounter());
			} else if (arguments[1].equals("Load")) {
				System.out.println("Loading an existing world...");
				world.load(arguments[2]);
			}
			System.out.println((Math.round((System.currentTimeMillis() - time) / 10) / 100.0)
					+ " seconds spent generating world.");
			
//			world.setdsize(8);
			world.setdmax(4);
			world.initializeDecompression();
			world.decompressAllChunks((int) (camera.x / csz), (int) (camera.y / csz), (int) (camera.z / csz));
			
//			System.out.println("Compressing generated chunks...");
//			time = System.currentTimeMillis();
//			world.compressAll();
//			System.out.println((Math.round((System.currentTimeMillis() - time) / 10) / 100.0)
//					+ " seconds spent compressing world.");
			
			if (!arguments[1].equals("Load")) {
				time = System.currentTimeMillis();
				worldName = arguments[0] + "-" + arguments[1] + "-" + arguments[2] + "-" + arguments[3];
				world.save(worldName + ".wrl");
				System.out.println((Math.round((System.currentTimeMillis() - time) / 10) / 100.0)
						+ " seconds spent saving world.");
			}
			
			camera.x = (double) (offset - 2.0);
			camera.y = (double) (offset - 2.0);
			camera.z = (double) (offset - 2.0);
			camera.rotY = 0;
			camera.rotX = Math.PI / 2;
			
			renderer = new Renderer(world, camera);
			System.out.println("World set up.  Now rendering...");
		}

		private void process_input(double timestep) {
			
			if (keyboard.isKeyDown('A')) {
				camera.x += speed * Math.cos(camera.rotY - Math.PI / 2);
				camera.y += speed * Math.sin(camera.rotY - Math.PI / 2);
			}
			if (keyboard.isKeyDown('D')) {
				camera.x += speed * Math.cos(camera.rotY + Math.PI / 2);
				camera.y += speed * Math.sin(camera.rotY + Math.PI / 2);
			}
			if (keyboard.isKeyDown('W')) {
				camera.x += speed * Math.cos(camera.rotY);
				camera.y += speed * Math.sin(camera.rotY);
			}
			if (keyboard.isKeyDown('S')) {
				camera.x += speed * Math.cos(camera.rotY + Math.PI);
				camera.y += speed * Math.sin(camera.rotY + Math.PI);
			}
			if (keyboard.isKeyDown('Q')) {
				camera.z -= speed;
			}
			if (keyboard.isKeyDown('E')) {
				camera.z += speed;
			}
			if (keyboard.isKeyDown('J')) {
				camera.rotY -= Math.PI / 32;
			}
			if (keyboard.isKeyDown('L')) {
				camera.rotY += Math.PI / 32;
			}
			if (keyboard.isKeyDown('I')) {
				camera.rotX += Math.PI / 32;
			}
			if (keyboard.isKeyDown('K')) {
				camera.rotX -= Math.PI / 32;
			}
			if (keyboard.isKeyDown('=')) {
				pixelScale++;
			}
			if (keyboard.isKeyDown('-')) {
				pixelScale--;
			}
			if (keyboard.isKeyDown('Z')) {
				speed *= 0.90;
			}
			if (keyboard.isKeyDown('X')) {
				speed *= 1.11;
			}
			if (keyboard.isKeyDown('C')) {
				System.out.println("Taking screenshot...");
				try {
				    // retrieve image
					Renderer r = new Renderer(world, camera);
					Image scr = createImage(r.renderG(X_BIG, Y_BIG, 1, castScale, true));
					BufferedImage bi = new BufferedImage(X_BIG, Y_BIG, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g3 = bi.createGraphics();
					g3.drawImage(scr, 0, 0, null);
					g3.dispose();
				    File outputfile = new File(worldName + "-"+Integer.toString((int) (Math.random()*1000))+".png");
				    ImageIO.write(bi, "png", outputfile);
				    r = null;
				    scr = null;
				    bi = null;
				    g3 = null;
				} catch (IOException e) {
				    // nothing to do
				}
			}
			if (keyboard.isKeyDown('P')) { // P is for pretty shadows
				doShadows = true;
			} else {
				doShadows = false;
			}
			camera.rotY = camera.rotY % (Math.PI * 2);
			camera.rotX = Math.max(Math.min(camera.rotX, Math.PI), 0);
			pixelScale = Math.max(Math.min(pixelScale, 10), 1);
			castScale = Math.max(Math.min(castScale, 50), 1);
		}
	}

	public static void main(String[] args) {
		arguments = args;
		JFrame jf = new JFrame();
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.addKeyListener(keyboard);
		jf.getContentPane().add(new SwingInterface(), BorderLayout.CENTER);
		jf.setSize(X_SIZE, Y_SIZE);
		jf.setVisible(true);
	}
}
