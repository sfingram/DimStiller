
package still.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Shows a 3D histogram as a height map image.
 * @author hyounesy
 *
 */
public class HeightMapPanel extends JPanel implements ChangeListener
{
	private static final long serialVersionUID = -1801832641310853977L;
	double m_XAxis[] 	= null;
	double m_YAxis[] 	= null;
	double m_Values[][] = null;
	double m_dMaxValue  = Double.MIN_VALUE;
	double m_dMinValue  = Double.MAX_VALUE;
	ArrayList<ChangeListener> m_ChangeListeners = null;
	
	ImagePanel 		m_ImagePanel 	= null;
	BufferedImage	m_Image 		= null;

	/**
	 * @param xaxis   1D array of numerical coordinates for the x axis
	 * @param yaxis   1D array of numerical coordinates for the y axis
	 * @param values  2D array of height values
	 */
	public HeightMapPanel(double xaxis[], double yaxis[], double values[][])
	{
		this.m_ChangeListeners = new ArrayList<ChangeListener>();
		this.setLayout(new BorderLayout(0,0));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		m_ImagePanel = new ImagePanel(null);
		m_ImagePanel.addChangeListener(this);
		this.add(m_ImagePanel, BorderLayout.CENTER);
		setValues(xaxis, yaxis, values);
	}

	void setValues(double xaxis[], double yaxis[], double values[][])
	{
		m_XAxis = xaxis;
		m_YAxis = yaxis;
		m_Values = values;
		updateImage();
	}
	
	void updateImage()
	{
		if (m_XAxis == null || m_YAxis == null || m_Values == null)
			return;
		
		// find the range of values, and use to normalize
//			m_dMaxValue  = Double.MIN_VALUE;
//			m_dMinValue  = Double.MAX_VALUE;
//			for (int i = 0; i < m_Values.length; i++)
//			{
//				for (int j = 0; j < m_Values[i].length; j++)
//				{
//					m_dMaxValue  = Math.max(m_dMaxValue, m_Values[i][j]);
//					m_dMinValue  = Math.min(m_dMinValue, m_Values[i][j]);
//				}
//			}
		m_dMaxValue  = 1.0;
		m_dMinValue  = 0.0;
		
		m_Image = new BufferedImage(m_XAxis.length, m_YAxis.length, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < m_XAxis.length; x++)
		{
			for (int y = 0; y < m_YAxis.length; y++)
			{
				double dNormVal = 0;
				if (m_dMaxValue > m_dMinValue)
					dNormVal = (m_Values[y][x] - m_dMinValue) / (m_dMaxValue - m_dMinValue);
				int iNormVal = 255 - Math.min(255, (int)(dNormVal * 255));
				m_Image.setRGB(x, m_YAxis.length - y - 1, (new Color(iNormVal, iNormVal, iNormVal)).getRGB());
			}
		}
		m_ImagePanel.setImage(m_Image);
	}
	
	public void addChangeListener( ChangeListener cl )
	{
		this.m_ChangeListeners.add( cl );
	}
	
	public double m_fX = 0;
	public double m_fY = 0;
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == m_ImagePanel)
		{
			m_fX = 1.0 * ImagePanel.m_iMouseClickX / m_ImagePanel.getWidth();
			m_fY = 1.0 - (1.0 * ImagePanel.m_iMouseClickY / m_ImagePanel.getHeight());
		}
		
		for( ChangeListener cl : m_ChangeListeners )
		{
			cl.stateChanged( new ChangeEvent(this) );
		}
	}
}
	