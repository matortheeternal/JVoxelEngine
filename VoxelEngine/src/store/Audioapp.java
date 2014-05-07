package store;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Audioapp extends Applet implements ActionListener {
	private AudioClip sound;
	private Button playSound, loopSound, stopSound;

	public void init() {
		System.out.println(getCodeBase());
		sound = getAudioClip(getCodeBase(), "menu.au");
		playSound = new Button("Play");
		playSound.addActionListener(this);
		add(playSound);

		loopSound = new Button("Loop");
		loopSound.addActionListener(this);
		add(loopSound);

		stopSound = new Button("Stop");
		stopSound.addActionListener(this);
		add(stopSound);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == playSound)
			sound.play();
		else if (e.getSource() == loopSound)
			sound.loop();
		else if (e.getSource() == stopSound)
			sound.stop();
	}
}
