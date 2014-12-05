package still.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ImagePanel extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = -2193781979309251268L;
	Image m_Image = null;
	ArrayList<ChangeListener> m_ChangeListeners = null;
	int m_iMouseMoveX = 0;
	int m_iMouseMoveY = 0;
	static int m_iMouseClickX = 0;
	static int m_iMouseClickY = 0;
	
	public ImagePanel(Image inputImage)
	{
		this.m_ChangeListeners = new ArrayList<ChangeListener>();
		m_Image = inputImage;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void setImage(Image inputImage)
	{
		m_Image = inputImage;
	}
	
	public void paint(Graphics g)
	{
		if (m_Image != null)
		{
			g.drawImage(m_Image, 0, 0, getWidth(), getHeight(), this);

			g.setColor(Color.RED);
			g.drawLine(m_iMouseMoveX, 0, m_iMouseMoveX, getHeight());
			g.drawLine(0, m_iMouseMoveY, getWidth(), m_iMouseMoveY);

			g.setColor(Color.YELLOW);
			g.drawLine(m_iMouseClickX, 0, m_iMouseClickX, getHeight());
			g.drawLine(0, m_iMouseClickY, getWidth(), m_iMouseClickY);
		}
	}
	
	public void addChangeListener( ChangeListener cl )
	{
		this.m_ChangeListeners.add( cl );
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		m_iMouseClickX = m_iMouseMoveX;
		m_iMouseClickY = m_iMouseMoveY;
		
		for( ChangeListener cl : m_ChangeListeners )
		{
			cl.stateChanged( new ChangeEvent(this) );
		}
		// update the view
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		mouseMoved(e);
	}

	@Override
	public void mouseExited(MouseEvent e)
	{
		mouseMoved(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		mouseMoved(e);
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		m_iMouseMoveX = e.getX();
		m_iMouseMoveY = e.getY();
		repaint();
	}		
}
