package still.data;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import still.expression.Expression;

/**
 * 
 * General factory for creating random procedurally generated point sets.
 * http://statlearn.free.fr/doc/Multidimensional%20distribution%20tutorial.htm
 * @author hyounesy
 *
 */
public class ProcTableFactory extends TableFactory
{
	static final int s_iInitDataSize = 100;
	static final int s_iInitDataDim  = 2;
	public static Table procTable( Container container )
	{
		//MemoryTable memTab = ProcTableFactory.createTable(s_iInitDataSize, s_iInitDataDim);
		MemoryTable memTab = ProcTableFactory.createGaussian(s_iInitDataSize, s_iInitDataDim, 2, 2, 2, 0.1, false, 0);
		if( memTab != null )
		{
			memTab.setInputControl( new ProcPanel(memTab) );
		}
		return memTab;
	}
	
	/**
	 * Return a Dims object from a csv file.  Assumes *no* categorical data.
	 * and one descriptor line.  
	 * 
	 * @param csvFilename
	 * @return
	 */
	public static MemoryTable createTable(int iDataSize, int iDim)
	{
        double[][] newTable = new double[iDataSize][iDim];
        String[] colNames = new String[iDim];
		MemoryTable memTab = new MemoryTable(newTable, colNames);
		memTab.setDescriptor("ProceduralTable");
    	for( int d = 0; d < iDim; d++ )
    	{
    		colNames[d] = "D" + Integer.toString(d+1);
    		for (int i = 0; i < iDataSize; i++)
    		{
    			newTable[i][d] = 0;
    		}
    	}
		return memTab;
	}

	/**
	 * Creates a memory table with the data points distributed (gaussian) among arbitrary number of clusters.
	 * @param iDataSize number of data points
	 * @param iDataDim dimensionality of the whole dataset
	 * @param iNumClusters	number of clusters
	 * @param iMinClusterDim minimum dimension of each cluster
	 * @param iMaxClusterDim maximum dimension of each cluster
	 * @param dWidth  gaussian distribution width (the larger the width, the datapoints are spread more)
	 * @param bCorrelated  specifies whehter there is correlation between dimensions
	 * @param iNumOutliers number of additional outliers
	 * @return a MemoryTable with the size [iDataSize+iNumOutliers][iDataDim]
	 */
	public static MemoryTable createGaussian(int iDataSize,
											 int iDataDim,
											 int iNumClusters,
											 int iMinClusterDim,
											 int iMaxClusterDim, 
											 double dWidth, 
											 boolean bCorrelated, 
											 int iNumOutliers
											 ) 
	{
		Random randGen = new Random();
		iMaxClusterDim = Math.min(iMaxClusterDim, iDataDim);
		iMinClusterDim = Math.min(iMinClusterDim, iMaxClusterDim);

		MemoryTable memTab = createTable(iDataSize + iNumOutliers, iDataDim);
		double[][] newTable = memTab.getTable(); 
    	
        double[][] center = new double[iNumClusters][iDataDim]; // cluster centroids
		for (int c = 0; c < iNumClusters; c++)
		{
			for (int d = 0; d < iDataDim; d++)
				center[c][d] = randGen.nextDouble();
		}
		
		// distributes the iDataSize points between iNumClusters clusters, with a gaussion distribution of mean: avgNum = iDataSize/iNumClusters
		double avgNum = 1.0 * iDataSize / iNumClusters; 
		int iNumLeft = iDataSize; // number of left (unassigned) points
		int idata = 0; // index of the current point being initialized

		double  correlationMap[][] = new double[iDataDim][iDataDim]; // correlation between each cluster's dimensions.
		
		for (int c = 0; c < iNumClusters; c++)
		{
			// Randomly pick the number of points in each cluster
			int iNumGen = iNumLeft;
			if (c < iNumClusters - 1)
			{
				// It is a gaussian distribution with the mean=avgNum and width=3  
				iNumGen = (int) (avgNum + randGen.nextGaussian() * avgNum / 3.0);

				// making sure there will be atleast 1 point remaining for each cluster at the end
				iNumGen = Math.min(iNumLeft - (iNumClusters - 1 - c), iNumGen);
			}
			iNumGen = Math.max(1, iNumGen);
			iNumLeft -= iNumGen;

			for (int d = 0; d < iDataDim; d++) // initialize the correlation map with 0.0
				Arrays.fill(correlationMap[d], 0.0);

			int iClusterDim = iMinClusterDim + randGen.nextInt(iMaxClusterDim + 1 - iMinClusterDim); // the true cluster dimensionality

			// randomly choose which dimensions are actual independent dimensions
			int iNumActive = 0;
			while (iNumActive < iClusterDim)
			{
				int d = randGen.nextInt(iDataDim);
				if (correlationMap[d][d] == 0.0)
				{
					correlationMap[d][d] = 1.0;
					iNumActive++;
				}
			}
			
			// create iDim random coeffiecients for correlations
			if (bCorrelated)
			{
				for( int d = 0; d < iDataDim; d++ )
		    	{
					correlationMap[randGen.nextInt(iDataDim)][randGen.nextInt(iDataDim)] = randGen.nextDouble() * 2.0 - 1.0; // random [-1..1]
		    	}
			}
			
			for (int i = 0; i < iNumGen && idata < iDataSize; i++, idata++)
			{
				// gaussian distribution around the centroids for uncorrelated dimensions
				for( int d = 0; d < iDataDim; d++ )
		    	{
					newTable[idata][d] = center[c][d];
					if (correlationMap[d][d] != 0)
						newTable[idata][d] += (2.0 * randGen.nextGaussian() - 1.0) * dWidth; 
		    	}
				
				if (bCorrelated)
				{// add linear correlation
					for( int d1 = 0; d1 < iDataDim; d1++ )
			    	{
						for( int d2 = d1 + 1; d2 < iDataDim; d2++ )
							newTable[idata][d2] += newTable[idata][d1] * correlationMap[d1][d2];
			    	}
				}
			}
		}
		
		// Add random outliers
		for (int i = 0; i < iNumOutliers; i++)
		{
			for( int d = 0; d < iDataDim; d++ )
	    	{
				newTable[i + iDataSize][d] = randGen.nextDouble();
	    	}
		}
		
		return memTab;
	}

	public static void saveTableCSV(MemoryTable memTab, File outputFile )
	{
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			for (int d = 0; d < memTab.columns(); d++)
			{
				if (d > 0)
				{
					bw.write(",");
				}
				bw.write(memTab.getColName(d));
			}
			bw.write("\n");
			
			for (int i = 0; i < memTab.rows(); i++)
			{
				for (int d = 0; d < memTab.columns(); d++)
				{
					if (d > 0)
					{
						bw.write(",");
					}
					bw.write(Double.toString(memTab.getMeasurement(i, d)));
				}
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public static class ProcPanel extends JPanel implements ActionListener
	{
		private static final long serialVersionUID = 2727706842660832284L;
		
		JSpinner 	m_SpinnerDataSize		= null;
		JSpinner 	m_SpinnerDataDim		= null;
		JSpinner 	m_SpinnerNumClusters	= null;
		JSpinner 	m_SpinnerWidth			= null;
		JSpinner 	m_SpinnerMinClusterDim	= null;
		JSpinner 	m_SpinnerMaxClusterDim	= null;
		JSpinner 	m_SpinnerOutliers		= null;
		JButton 	m_ButtonGenerate 		= null;
		JButton 	m_ButtonSave			= null;
		JCheckBox 	m_CheckCorrelation		= null;

		MemoryTable 	m_InternalTable = null;
		static int		m_iNumClusters = 2;
		static int 		m_iMinClusterDim = s_iInitDataDim;
		static int		m_iMaxClusterDim = s_iInitDataDim;
		static double	m_dWidth = 0.1;
		static int 		m_iNumOutliers = 0;
		static boolean	m_bCorrelated = false;
		
		Expression 		m_Exp = null;
		MemoryTable 	m_MemTable = null;
		boolean 		block = false;

		public ProcPanel(MemoryTable intTab ) {
			super();
			m_InternalTable 	= intTab;
			
			this.setBorder(	BorderFactory.createEmptyBorder(10, 10, 10, 10));
			this.setLayout(new BorderLayout(5,5));
			JPanel masterPanel = new JPanel(new GridLayout(9, 2, 5, 5)); // (rows, cols, hgap, vgap)
			this.add(masterPanel, BorderLayout.WEST);
			
			masterPanel.add(new JLabel("Data Dim:"));
			m_SpinnerDataDim    = new JSpinner(new SpinnerNumberModel(intTab.columns(), 1, 100, 1));
			masterPanel.add(m_SpinnerDataDim);
			
			masterPanel.add(new JLabel("Data Size:"));
			m_SpinnerDataSize   = new JSpinner(new SpinnerNumberModel(intTab.rows(), 1, 1000000, 1));
			masterPanel.add(m_SpinnerDataSize);
			
			masterPanel.add(new JLabel("Num Clusters:"));
			m_SpinnerNumClusters   = new JSpinner(new SpinnerNumberModel(m_iNumClusters, 1, intTab.rows(), 1));
			masterPanel.add(m_SpinnerNumClusters);

			masterPanel.add(new JLabel("Min Cluster Dim:"));
			m_SpinnerMinClusterDim   = new JSpinner(new SpinnerNumberModel(m_iMinClusterDim, 1, 100, 1));
			masterPanel.add(m_SpinnerMinClusterDim);

			masterPanel.add(new JLabel("Max Cluster Dim:"));
			m_SpinnerMaxClusterDim   = new JSpinner(new SpinnerNumberModel(m_iMaxClusterDim, 1, 100, 1));
			masterPanel.add(m_SpinnerMaxClusterDim);

			masterPanel.add(new JLabel("Spread (0..1):"));
			m_SpinnerWidth   = new JSpinner(new SpinnerNumberModel(m_dWidth, 0.0, 1.0, 0.01));
			masterPanel.add(m_SpinnerWidth);
			
			m_CheckCorrelation = new JCheckBox("");
			m_CheckCorrelation.setSelected(m_bCorrelated);
			masterPanel.add(new JLabel("Correlated: "));
			masterPanel.add(m_CheckCorrelation);
			
			masterPanel.add(new JLabel("Num Outliers:"));
			m_SpinnerOutliers   = new JSpinner(new SpinnerNumberModel(m_iNumOutliers, 0, 100000, 1));
			masterPanel.add(m_SpinnerOutliers);

			m_ButtonGenerate 	= new JButton( "Generate" );
			m_ButtonGenerate.addActionListener(this);
			masterPanel.add(m_ButtonGenerate);			
			
			m_ButtonSave	= new JButton( "Save" );
			m_ButtonSave.addActionListener(this);
			masterPanel.add(m_ButtonSave);			
			
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
//			if( e.getSource() == m_InternalTable )
//			{
//				// update the gui
//				fileLabel.setText(fname);
//			}
			if( e.getSource() == m_ButtonGenerate )
			{
				int iDataDim  = ((Integer)m_SpinnerDataDim.getValue());
				int iDataSize = ((Integer)m_SpinnerDataSize.getValue());
				m_iNumClusters = ((Integer)m_SpinnerNumClusters.getValue());
				m_iMinClusterDim = ((Integer)m_SpinnerMinClusterDim.getValue());
				m_iMaxClusterDim = ((Integer)m_SpinnerMaxClusterDim.getValue());
				m_dWidth = ((Double)m_SpinnerWidth.getValue());
				m_iNumOutliers = ((Integer)m_SpinnerOutliers.getValue());
				m_bCorrelated = m_CheckCorrelation.isSelected();

				// make a new table					
				m_MemTable = ProcTableFactory.createGaussian(iDataSize, iDataDim, m_iNumClusters, m_iMinClusterDim, m_iMaxClusterDim, m_dWidth, m_bCorrelated, m_iNumOutliers);
				if( m_MemTable != null )
				{
					m_MemTable.setInputControl( new ProcPanel(m_MemTable) );
				}
		    	block = true;
		    	
				for( TableListener tl : m_InternalTable.getTableListeners() ) {
					
					if(tl instanceof Expression) {
						
						m_Exp = (Expression)tl;							
					}
				}
				
				SwingUtilities.invokeLater(new Runnable() {
			        public void run() {
			        	
			        	try {
			        		
			        		while( block ) {
			        			Thread.sleep(1000);
			        		}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
			        	m_Exp.setTable(m_MemTable);
			        	m_InternalTable = m_MemTable;
			        }
			      });
				// inform the expression
				block = false;
			}
			else if( e.getSource() == m_ButtonSave )
			{
				JFileChooser fc = new JFileChooser( );
				int returnVal = fc.showSaveDialog(this);
				if( returnVal == JFileChooser.APPROVE_OPTION )
				{
					saveTableCSV(m_InternalTable, fc.getSelectedFile());
				}
			}

		}
	}
}
