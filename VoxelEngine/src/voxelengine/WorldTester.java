package voxelengine;

import java.applet.Applet;

public class WorldTester extends Applet {
	private static final long serialVersionUID = 1L;
	private static final int worldSize = 64;
	private static final int fractalSize = worldSize * 14;
	
	public void init() {
		// creating world
		System.out.println("Creating world size: "+worldSize);
		Long time = System.currentTimeMillis();
		World world = new World(worldSize);
		time = ((System.currentTimeMillis() - time)/1000);
		System.out.println("Time: "+time+"s \n");
		
		// initializing world data
		world.addColorBlockTypes();
		world.addPalettes();
		
		// generating mandelbox
		System.out.println("Generating mandelbox...");
		int offset = (worldSize * 16 - fractalSize)/2;
		time = System.currentTimeMillis();
		world.generateMandelbox("test", fractalSize, 2, 5, 4, 12, 4, offset, offset, offset);
		time = ((System.currentTimeMillis() - time)/1000);
		System.out.println("Time: "+time+"s \n");
		
		// saving world
		System.out.println("Saving world...");
		time = System.currentTimeMillis();
		world.save("test.wrl");
		time = ((System.currentTimeMillis() - time)/1000);
		System.out.println("Time: "+time+"s \n");
	}
}
