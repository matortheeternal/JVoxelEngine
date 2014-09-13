package voxelengine;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.file.Files;

import javax.swing.Box;
import javax.swing.BoxLayout;

@SuppressWarnings("serial")
public class Menu extends Applet implements ActionListener, ItemListener, Runnable {
	@SuppressWarnings("unused")
	private Button generateWorld, settings, quit, ok, back, generate;
	private TextField size, worldSize, minIt, maxIt, power, zoom, cutoff;
	@SuppressWarnings("unused")
	private Label sizeL, colorL, worldSizeL, minItL, maxItL, powerL, zoomL, resL, pixL, castL, cutoffL;
	private Choice choice, color;
	private AudioClip sound;
	private Image[] slideshow;
	private Panel pnl;
	private int ndx = 0;
	private long time = System.currentTimeMillis();
	private long ttime = 4000;
	private Thread rpThread;
	private static final int bgcount = 32;
	
	public void start() {
		if (rpThread == null) {
		    rpThread = new Thread(this, "repaint");
		    rpThread.start();
		}
    }
	
	public void init() {
		this.setSize(784, 562);
		
		// load slideshow
		slideshow = new Image[bgcount];
		for (int i = 0; i < bgcount; i++) {
			slideshow[i] = getImage(getCodeBase(), Integer.toString(i+1)+".jpg");
		}
		// shuffle slideshow
		for (int i = 0; i < 3 * bgcount; i++) {
			int i1 = i % bgcount;
			int i2 = (int) (Math.random() * bgcount);
			Image s1 = slideshow[i1];
			Image s2 = slideshow[i2];
			slideshow[i1] = s2;
			slideshow[i2] = s1;
		}
		
		// start menu music
		sound = getAudioClip(getCodeBase(), "menu.au");
		sound.loop();
		
		// create menu
		mainMenu();
	}
	
	public void paint(Graphics g) {
		g.drawImage(slideshow[ndx], 0, 0, this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if (src == generateWorld) {
			clearMenu();
			generationMenu();
			this.setSize(785, 562);
			this.setSize(784, 562);
		} else if (src == settings) {
			clearMenu();
			settingsMenu();
			this.setSize(785, 562);
			this.setSize(784, 562);
		} else if (src == quit) {
			clearMenu();
			System.exit(0);
			this.setSize(785, 562);
			this.setSize(784, 562);
		} else if (src == back) {
			clearMenu();
			mainMenu();
			this.setSize(785, 562);
			this.setSize(784, 562);
		} else if (src == generate) {
			sound.stop();
			clearMenu();
			this.setVisible(false);
			sound = getAudioClip(getCodeBase(), "game.au");
			sound.loop();
			SwingInterface.main(new String[]{worldSize.getText(), choice.getSelectedItem(), color.getSelectedItem(), size.getText(), minIt.getText(), maxIt.getText(), power.getText(), zoom.getText(), cutoff.getText()});
		}
	}
	
	public void clearMenu() {
		this.removeAll();
		repaint();
	}
	
	public void clearPanel() {
		pnl.removeAll();
		repaint();
	}
	
	public void generationMenu() {
		pnl = new Panel();
		pnl.setMaximumSize(new Dimension(350, 200));
		pnl.setPreferredSize(new Dimension(350, 200));
		pnl.setBackground(Color.BLACK);
		worldSizeL = new Label("World size: ");
		worldSizeL.setForeground(Color.LIGHT_GRAY);
		worldSizeL.setBackground(Color.BLACK);
		worldSizeL.setMaximumSize(new Dimension(70, 20));
		worldSize = new TextField("6");
		worldSize.setMaximumSize(new Dimension(50, 20));
		choice = new Choice();
		choice.addItem("Menger Sponge");
		choice.addItem("Mandelbulb");
		choice.addItem("Mandelbox");
		choice.addItem("Greek Cross");
		choice.addItem("Octahedron");
		choice.addItem("Load");
		choice.setMaximumSize(new Dimension(140, 40));
		choice.setForeground(Color.LIGHT_GRAY);
		choice.setBackground(Color.BLACK);
		generate = new Button("Generate!");
		generate.setMaximumSize(new Dimension(140, 40));
		generate.setForeground(Color.LIGHT_GRAY);
		generate.setBackground(Color.BLACK);
		back = new Button("Back");
		back.setMaximumSize(new Dimension(140, 40));
		back.setForeground(Color.LIGHT_GRAY);
		back.setBackground(Color.BLACK);
		generate.addActionListener(this);
		back.addActionListener(this);
		choice.addItemListener(this);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(Box.createRigidArea(new Dimension(0,30)));
		add(worldSizeL);
		add(worldSize);
		this.add(Box.createRigidArea(new Dimension(0,20)));
		add(choice);
		this.add(Box.createRigidArea(new Dimension(0,30)));
		add(pnl);
		this.add(Box.createRigidArea(new Dimension(0,50)));
		add(generate);
		this.add(Box.createRigidArea(new Dimension(0,30)));
		add(back);
		
		colorL = new Label("Color: ");
		colorL.setForeground(Color.LIGHT_GRAY);
		colorL.setBackground(Color.BLACK);
		colorL.setPreferredSize(new Dimension(50, 20));
		color = new Choice();
		color.setPreferredSize(new Dimension(120, 20));
		sizeL = new Label("Size: ");
		sizeL.setForeground(Color.LIGHT_GRAY);
		sizeL.setBackground(Color.BLACK);
		sizeL.setPreferredSize(new Dimension(40, 20));
		size = new TextField("81");
		size.setPreferredSize(new Dimension(50, 20));
		minItL = new Label("Minimum Iterations: ");
		minItL.setForeground(Color.LIGHT_GRAY);
		minItL.setBackground(Color.BLACK);
		minItL.setPreferredSize(new Dimension(120, 20));
		minIt = new TextField("4");
		minIt.setPreferredSize(new Dimension(100, 20));
		maxItL = new Label("Maximum Iterations: ");
		maxItL.setForeground(Color.LIGHT_GRAY);
		maxItL.setBackground(Color.BLACK);
		maxItL.setPreferredSize(new Dimension(120, 20));
		maxIt = new TextField("16");
		maxIt.setPreferredSize(new Dimension(100, 20));
		powerL = new Label("Power: ");
		powerL.setForeground(Color.LIGHT_GRAY);
		powerL.setBackground(Color.BLACK);
		powerL.setPreferredSize(new Dimension(50, 20));
		power = new TextField("8");
		power.setPreferredSize(new Dimension(100, 20));
		zoomL = new Label("Zoom: ");
		zoomL.setForeground(Color.LIGHT_GRAY);
		zoomL.setBackground(Color.BLACK);
		zoomL.setPreferredSize(new Dimension(50, 20));
		zoom = new TextField("4");
		zoom.setPreferredSize(new Dimension(50, 20));
		cutoffL = new Label("Cutoff: ");
		cutoffL.setForeground(Color.LIGHT_GRAY);
		cutoffL.setBackground(Color.BLACK);
		cutoffL.setPreferredSize(new Dimension(50, 20));
		cutoff = new TextField("5");
		cutoff.setPreferredSize(new Dimension(50, 20));
		
		pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		
		pnl.add(colorL, BorderLayout.CENTER);
		pnl.add(color, BorderLayout.CENTER);
		pnl.add(Box.createRigidArea(new Dimension(10,0)));
		pnl.add(sizeL, BorderLayout.CENTER);
		pnl.add(size, BorderLayout.CENTER);
		pnl.add(powerL, BorderLayout.CENTER);
		pnl.add(power, BorderLayout.CENTER);
		pnl.add(Box.createRigidArea(new Dimension(40,0)));
		pnl.add(zoomL, BorderLayout.CENTER);
		pnl.add(zoom, BorderLayout.CENTER);
		pnl.add(minItL, BorderLayout.CENTER);
		pnl.add(minIt, BorderLayout.CENTER);
		pnl.add(Box.createRigidArea(new Dimension(80,0)));
		pnl.add(maxItL, BorderLayout.CENTER);
		pnl.add(maxIt, BorderLayout.CENTER);
		pnl.add(Box.createRigidArea(new Dimension(120,0)));
		pnl.add(cutoffL, BorderLayout.CENTER);
		pnl.add(cutoff, BorderLayout.CENTER);
		
		mengerMenu();
	}
	
	public void mengerMenu() {
		addColors();
		size.setText("81");
		power.setEnabled(false);
		zoom.setEnabled(false);
		minIt.setEnabled(false);
		maxIt.setEnabled(false);
		cutoff.setEnabled(false);
	}

	public void crossMenu() {
		addColors();
		powerL.setText("Scale: ");
		power.setText("3");
		size.setText("63");
		power.setEnabled(true);
		zoom.setEnabled(false);
		minIt.setEnabled(false);
		maxIt.setEnabled(false);
		cutoff.setEnabled(false);
	}
	
	public void octahedronMenu() {
		addColors();
		powerL.setText("Scale: ");
		power.setText("7");
		size.setText("63");
		power.setEnabled(true);
		zoom.setEnabled(false);
		minIt.setEnabled(false);
		maxIt.setEnabled(false);
		cutoff.setEnabled(false);
	}
	
	public void bulbMenu() {
		addPalettes();
		powerL.setText("Power: ");
		size.setText("81");
		power.setEnabled(true);
		power.setText("8");
		zoom.setEnabled(false);
		minIt.setEnabled(true);
		maxIt.setEnabled(true);
		cutoff.setEnabled(true);
		cutoff.setText("1024");
	}
	
	public void boxMenu() {
		addPalettes();
		powerL.setText("Scale: ");
		size.setText("81");
		power.setText("1.5");
		power.setEnabled(true);
		zoom.setEnabled(true);
		minIt.setEnabled(true);
		maxIt.setEnabled(true);
		cutoff.setEnabled(true);
		cutoff.setText("5");
	}
	
	private void addPalettes() {
		color.setEnabled(true);
		colorL.setText("Palette: ");
		color.removeAll();
		color.addItem("test");
		color.addItem("Glory");
		color.addItem("Boutique");
		color.addItem("Goldfish");
		color.addItem("Dreamy");
		color.addItem("Technological");
		color.addItem("Murderous");
		color.addItem("Golddigger");
		color.addItem("Undergrowth");
		color.addItem("Feeling Blue");
		color.addItem("Oranje");
		color.addItem("Tropical Sea");
		color.addItem("Motherboard");
		color.addItem("Fire in the Night");
		color.addItem("Sulfur");
		color.addItem("Alien");
		color.addItem("Fruit Shake");
		color.addItem("Frosted Cactus");
		color.addItem("Borg");
	}

	private void addColors() {
		color.setEnabled(true);
		colorL.setText("Color: ");
		color.removeAll();
		color.addItem("red");
		color.addItem("green");
		color.addItem("blue");
		color.addItem("yellow");
		color.addItem("orange");
		color.addItem("magenta");
		color.addItem("pink");
		color.addItem("cyan");
		color.addItem("black");
		color.addItem("white");
		color.addItem("gray");
		color.addItem("darkGray");
	}
	
	public void loadMenu() {
		color.setEnabled(true);
		power.setEnabled(false);
		color.removeAll();
		color.addItem("test.wrl");
		color.addItem("10-Mandelbulb-Fire in the Night-256.wrl");
		color.addItem("37-Menger Sponge-red-2187.wrl");
		color.addItem("35-Mandelbox-Borg-2000.wrl");
		zoom.setEnabled(false);
		minIt.setEnabled(false);
		maxIt.setEnabled(false);
		cutoff.setEnabled(false);
	}
	
	public void settingsMenu() {
		back = new Button("Back");
		back.setMaximumSize(new Dimension(140, 40));
		back.setForeground(Color.LIGHT_GRAY);
		back.setBackground(Color.BLACK);
		back.addActionListener(this);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(Box.createRigidArea(new Dimension(0,250)));
		add(back, BorderLayout.CENTER);
	}
	
	public void mainMenu() {
		generateWorld = new Button("Generate World");
		generateWorld.setMaximumSize(new Dimension(140, 40));
		generateWorld.setForeground(Color.LIGHT_GRAY);
		generateWorld.setBackground(Color.BLACK);
		settings = new Button("Settings");
		settings.setMaximumSize(new Dimension(140, 40));
		settings.setForeground(Color.LIGHT_GRAY);
		settings.setBackground(Color.BLACK);
		quit = new Button("Quit");
		quit.setMaximumSize(new Dimension(140, 40));
		quit.setForeground(Color.LIGHT_GRAY);
		quit.setBackground(Color.BLACK);
		generateWorld.addActionListener(this);
		settings.addActionListener(this);
		quit.addActionListener(this);
		
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		this.add(Box.createRigidArea(new Dimension(0,170)));
		add(generateWorld, BorderLayout.CENTER);
		this.add(Box.createRigidArea(new Dimension(0,50)));
		add(settings, BorderLayout.CENTER);
		this.add(Box.createRigidArea(new Dimension(0,50)));
		add(quit, BorderLayout.CENTER);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getSource() == choice) {
			String sel = choice.getSelectedItem();
			if (sel.equals("Menger Sponge")) {
				mengerMenu();
			} else if (sel.equals("Mandelbulb")) {
				bulbMenu();
			} else if (sel.equals("Mandelbox")) {
				boxMenu();
			} else if (sel.equals("Greek Cross")) {
				crossMenu();
			} else if (sel.equals("Octahedron")) {
				octahedronMenu();
			} else if (sel.equals("Load")) {
				loadMenu();
			}
		}
	}

	@Override
	public void run() {
		while (rpThread != null) {
			if (System.currentTimeMillis() - time >= ttime) {
				time = System.currentTimeMillis();
				ndx = (ndx + 1) % bgcount;
			}
		    try {
		    	Thread.sleep(500);
		    } catch (InterruptedException e) {
		    }
		    paint(this.getGraphics());
		}
	}
}
