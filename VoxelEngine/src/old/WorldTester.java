package old;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import java.awt.Panel;

import voxelengine.World;

public class WorldTester extends Applet {
	private static final long serialVersionUID = 1L;
	private static final int worldSize = 20;
	private static final int fractalSize = worldSize * 14;
	private static final byte num = 56;
	private static final int pnlSize = 100;
	
	public void init() {
		// creating world
		System.out.println("Creating world size: "+worldSize);
		Long time = System.currentTimeMillis();
		World world = new World(worldSize);
		time = (System.currentTimeMillis() - time);
		System.out.println("Time: "+time+"ms \n");
		
		// initializing world data
		world.addColorBlockTypes();
		world.addPalettes();
		
		displayColors(world);
		
		// generating mandelbox
//		System.out.println("Generating mandelbox...");
//		int offset = (worldSize * 16 - fractalSize)/2;
//		time = System.currentTimeMillis();
//		world.getGenerator().generateMandelbox("test", fractalSize, 2, 5, 4, 12, 4, offset, offset, offset);
//		time = (System.currentTimeMillis() - time);
//		System.out.println("Time: "+time+"ms \n");
		
		// saving world
//		System.out.println("Saving world...");
//		time = System.currentTimeMillis();
//		world.save("test.wrl");
//		time = (System.currentTimeMillis() - time);
//		System.out.println("Time: "+time+"ms \n");
	}

	private void displayColors(World world) {
		this.setSize(600, 800);
		
		for (byte i = 1; i <= num; i++) {
			Panel pnl = new Panel();
			pnl.setPreferredSize(new Dimension(pnlSize + 8, pnlSize + 32));
			Panel col = new Panel();
			col.setPreferredSize(new Dimension(pnlSize, pnlSize));
			col.setBackground(world.getType(i).getColor());
			pnl.add(col);
			Label lb = new Label();
			lb.setText(world.getType(i).getName() + " " + Integer.toString(i));
			pnl.add(lb);
			add(pnl);
		}
	}
}
