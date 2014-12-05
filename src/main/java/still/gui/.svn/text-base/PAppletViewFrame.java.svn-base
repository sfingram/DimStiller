package still.gui;

import java.awt.Graphics;

import processing.core.PApplet;

public class PAppletViewFrame extends ViewFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20468039754356861L;

	PApplet procApp = null;
	
	public PAppletViewFrame( String s, PApplet pa ) {
		
		super(s);
		
		procApp = pa;
		this.add(procApp,"Center");
		
		procApp.init();		
	}
	
	public void paintComponent( Graphics g ) {
		
		super.paintComponent(g);
		procApp.redraw();
	}
}
