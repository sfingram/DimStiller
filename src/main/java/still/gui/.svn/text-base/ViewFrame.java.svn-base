package still.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

public class ViewFrame extends JInternalFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7358842900604017547L;
	static int openFrameCount = 0;
	static final int xOffset = 30, yOffset = 30;
	
	public ViewFrame(String name) {
		super(name,true,false,true,true);		
		setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
		ViewFrame.openFrameCount++;
		setSize(300,300);
		this.getContentPane().setLayout( new BorderLayout(5,5) );
	}

}
