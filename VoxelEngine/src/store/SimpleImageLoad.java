package store;

import java.awt.*;
import java.applet.*;
import java.net.*;

public class SimpleImageLoad extends Applet {
	Image img;
	URL coach;

	public void init() {
		try {
			coach = new URL("http://www.cs.sbcc.cc.ca.us/~rhd/");
		} catch (MalformedURLException e) {
			showStatus("Exception: " + e.toString());
		}

		img = getImage(coach, "coach2c.gif");
	}

	public void paint(Graphics g) {
		g.drawImage(img, 0, 0, this);
	}
}
