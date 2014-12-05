package still.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import still.data.ClusterTable;

public class ClusterCompareView extends JPanel implements MouseListener, MouseMotionListener
{
	private static final long serialVersionUID = -7661064417345747353L;

	ClusterTable m_ClusterTable = null;
	
	/** The current selected K */
	public int m_iSelectedK = 3;

	/** Sorted*/
	//int m_SortedBySize

	Color bkgndColor 	= Color.WHITE;

	static int[] colorTable = {	
		0xFF258BC1, // blue
		0xFFFE9729, // orange
		0xFF32AB5A, // green
		0xFFDF3D33, // red
		0xFFA67FBE, // purple
		0xFF9D6A5E, // brown
		0xFFE897C7, // pink
		0xFF919090, // grey
		0xFFC7C52B, // gold 
		0xFF05C7D7 // teal
	};
	
	public ClusterCompareView()
	{
		this.m_ChangeListeners = new ArrayList<ChangeListener>();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}

	ArrayList<ChangeListener> m_ChangeListeners = null;
	public void addChangeListener( ChangeListener cl )
	{
		this.m_ChangeListeners.add( cl );
	}
	
	public void setClusterTable(ClusterTable table)
	{
		m_ClusterTable = table;
		repaint();
	}
	
	public void setSelectedK(int iK)
	{
		m_iSelectedK = iK;
	}
	
	public void paintComponent( Graphics g )
	{
		super.paintComponent(g);
		
		Graphics2D g2D = (Graphics2D) g;
		
		// clear screen
		
		g2D.setColor( bkgndColor );
		g2D.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		if (m_ClusterTable == null || m_ClusterTable.m_AllClusterInfoArray == null)
		{// cluster info array is not built yet, so nothing to show.
			return;
		}
		
		int iDataSize = m_ClusterTable.getInputSize();
		int iMaxK = m_ClusterTable.m_iMaxNumClusters;

		
		// gets a sorted list of the cluster IDs decreasing by the number of points in each cluster
		Integer sortedBySize[][] = new Integer[iMaxK][];
		for (int k = 0; k < iMaxK; k++)
		{
			sortedBySize[k] = new Integer[k + 1];
			for (int i = 0; i <= k; i++)
				sortedBySize[k][i] = new Integer(i);
		}
		try
		{
			for (int i = 0; i < iMaxK; i++)
			{
				final int k = i;
				Arrays.sort(sortedBySize[i], new Comparator<Integer>() {
							public int compare(Integer i1, Integer i2)
							{
								int n1 = m_ClusterTable.m_AllClusterInfoArray[k].m_ClusterInfoArray[i1].m_iNumPoints;
								int n2 = m_ClusterTable.m_AllClusterInfoArray[k].m_ClusterInfoArray[i2].m_iNumPoints;
								return n1 > n2 ? -1 : (n1 < n2 ? 1 : (i1 < i2 ? -1 : (i1 > i2 ? 1 : 0)));
							}
						});
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		int iGapX = Math.min(5, this.getWidth() / (2 * iMaxK));
		int iBarW = (this.getWidth() / iMaxK) - iGapX; 
		int iGapY = Math.min(5, this.getHeight() / (2 * iMaxK));
		double dActualHeight = this.getHeight() - iGapY * iMaxK; 
		
		if (m_iSelectedK > 0 && m_iSelectedK <= iMaxK)
		{
			try
			{
				// the number of points shared between all pair of clusters for two differnt Ks
				int pairCompare[][] = new int[iMaxK+1][iMaxK+1];

				for (int pass = 0; pass < 2; pass++)
				{
					int iCompareK = pass == 0 ? m_iSelectedK - 1 : m_iSelectedK + 1;
					if (iCompareK <= 0 || iCompareK > iMaxK)
						continue;
				
					for (int i = 0; i < pairCompare.length; i++)
						Arrays.fill(pairCompare[i], 0);
					
					for (int i = 0; i < iDataSize; i++)
					{
						int i0 = m_ClusterTable.m_AllClusterInfoArray[m_iSelectedK - 1].m_iClusterIDArray[i];
						int i1 = m_ClusterTable.m_AllClusterInfoArray[iCompareK - 1].m_iClusterIDArray[i];
						pairCompare[i0][i1]++;
					}
					
					
					Integer sortedBySelected[] = new Integer[iCompareK]; // sorted based on the elements sharing with the selected K
					for (int i = 0; i < iCompareK; i++)
					{
						sortedBySelected[i] = new Integer(i);
					}
					try
					{
						final int[][] fPairCompare = pairCompare;
						final Integer[][] fSortedBySize = sortedBySize;
						
						Arrays.sort(sortedBySelected, new Comparator<Integer>() {
									public int compare(Integer i1, Integer i2)
									{
										for (int iSel = 0; iSel < m_iSelectedK; iSel++)
										{
											int i = fSortedBySize[m_iSelectedK - 1][iSel];
											if (fPairCompare[i][i1] > fPairCompare[i][i2])
												return -1;
											else if (fPairCompare[i][i1] < fPairCompare[i][i2])
												return 1;
										}
										return 0;
									}
								});
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					
					int iBarX = (iCompareK - 1) * (iBarW + iGapX);
					double dBarY = (iMaxK - iCompareK) * iGapY / 2;
					for (int iCmp = 0; iCmp < iCompareK; iCmp++)
					{
						int iCmpSorted = sortedBySelected[iCmp]; // the sorted index
						for (int iSel = 0; iSel < m_iSelectedK; iSel++)
						{
							int iSelSorted = sortedBySize[m_iSelectedK - 1][iSel]; // the sorted index
							double dBarH = (dActualHeight * pairCompare[iSelSorted][iCmpSorted]) / iDataSize;
							g2D.setColor(new Color(colorTable[iSelSorted % colorTable.length]));
							g2D.fillRect(iBarX, (int)dBarY, iBarW, (int)Math.ceil(dBarH));
							dBarY += dBarH;
						}
						dBarY += iGapY;
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			for (int iK = 1; iK <= iMaxK; iK++)
			{
				if (iK == m_iSelectedK + 1 || iK == m_iSelectedK - 1)
					continue;
	
				int iBarX = (iK - 1) * (iBarW + iGapX);
				double dBarY = (iMaxK - iK) * iGapY / 2;
			
				for (int i = 0; i < iK; i++)
				{
					int iCluster = sortedBySize[iK - 1][i];
					double dBarH = (dActualHeight * m_ClusterTable.m_AllClusterInfoArray[iK - 1].m_ClusterInfoArray[iCluster].m_iNumPoints) / iDataSize;
					if (iK == m_iSelectedK)
						g2D.setColor(new Color(colorTable[iCluster % colorTable.length]));
					else
						g2D.setColor(new Color(240, 240, 240));
						
					g2D.fillRect(iBarX, (int)dBarY, iBarW, (int)Math.ceil(dBarH));
					dBarY += dBarH;
					dBarY += iGapY;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

//	     Graphics2D g2d = (Graphics2D)g;
//	      Color s1 = Color.red;
//	      Color e = Color.green;
//	      GradientPaint gradient = new GradientPaint(10,10,s1,30,30,e,true);
//	      g2d.setPaint(gradient);
//	      g2d.drawRect(100,100,200,120);
//	      Color s2 = Color.yellow;
//	      Color e1 = Color.pink;
//	      GradientPaint gradient1 = new GradientPaint(10,10,s2,30,30,e1,true);
//	      g2d.setPaint(gradient1);
//	      g2d.fillRect(99,99,199,119);
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		m_iSelectedK = ((m_ClusterTable.m_iMaxNumClusters * e.getX()) / this.getWidth()) + 1;
		for( ChangeListener cl : m_ChangeListeners )
		{
			cl.stateChanged( new ChangeEvent(this) );
		}
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
	}	
}
