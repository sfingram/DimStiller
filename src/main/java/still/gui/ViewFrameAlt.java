package still.gui;

import java.awt.BorderLayout;
import java.awt.Frame;

public class ViewFrameAlt extends Frame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6499684254311619192L;
	static int openFrameCount = 1;
	static final int xOffset = 30, yOffset = 30;
	
	public ViewFrameAlt(String s) {
		
		super();
		setTitle(s);
		setResizable(true);
		setLocation(300 + xOffset*openFrameCount, yOffset*openFrameCount);
		ViewFrameAlt.openFrameCount++;
		setSize(500,500);
		this.setLayout( new BorderLayout(5,5) );
		this.setVisible(false);
	}
}
