/*
  TODO auto update the SPLOM when clusterOP changes.
  TODO auto-generate Expression: params(num points, datadim, min-max, num clusters, clusters dim, distribution, ...)
  TODO output cluster centroids
  TODO precompute centroids for a K range
  TODO computer inter-cluster distance
*/

package still.operators;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.*;


import still.data.*;
import still.gui.ClusterCompareView;
import still.gui.EnumUtils;
import still.gui.HeightMapPanel;
import still.gui.OperatorView;
import still.gui.ScreePlot;

public class ClusterOp extends Operator implements Serializable
{
	private static final long serialVersionUID = 7425288340100347866L;

	static final String 	s_sClusterColName = "_cluster_";
  	/** Index of the cluster column in the output table. */
	public int 				m_iClusterCol 	= -1;  
	/** Specifies whether the table has a cluster column already. */
	boolean 				m_bHasClusterCol = false;
	/** [N][1] values representing the additional cluster id column */
	double 					m_dClusterColArray[][] = null;

	ClusterTable 			m_ClusterTable 	= null;

	
	/**
	 * Simple Constructor 
	 */
	public ClusterOp(Table newInput, boolean isActive)
	{
		super(newInput);
		m_ClusterTable = new ClusterTable(newInput);
				
		this.isActive = isActive;
		if (isActive ) {
			
			updateMap();
			updateFunction();
			//runKMeans();
			isLazy  		= true;
			setView( new ClusterView( this ) );		
		}
	}

	/**
	 * Constructor: Parameterized Operator creation
	 * @param newInput
	 * @param isActive
	 * @param paramString
	 */
	public ClusterOp( Table newInput, boolean isActive, String paramString ) {
		
		super(newInput);
		m_ClusterTable = new ClusterTable(newInput);
		
		// extract parameters
		
		String[] params = paramString.split(",");
//		colorByCol = Integer.parseInt(params[0]);
//		cullColorBy = Boolean.parseBoolean(params[1]);
		
		// handle if the parameters are inappropriate
//		if (Operator.getNonAttributeDims(newInput) <= colorByCol ) {
//			colorByCol = 0;	// reset column to zero
//		}
		m_ClusterTable.setNumClusters(Integer.parseInt(params[0]));
		
		this.isActive = isActive;
		if (isActive ) {
			
			updateMap();
			updateFunction();
			//runKMeans();
			isLazy  		= true;
			setView( new ClusterView( this ) );		
		}
	}
	
	/**
	 * @return String menu name of the cluser operator 
	 */
	public static String getMenuName()
	{
		return "Attrib:Cluster";
	}
	
	public String toString()
	{
		return "[Attrib:Cluster]";
	}

	public int rows()
	{
		if (m_ClusterTable.m_ClusterInfoArray == null)
		{
			return input.rows();
		}
		//TODO: replace this with a filter operator instead
		int iSelectedPoints = 0;
		for (int c = 0; c  < m_ClusterTable.m_iNumClusters; c++)
		{
			if (m_ClusterTable.m_ClusterInfoArray[c].m_bSelected)
			{
				iSelectedPoints += m_ClusterTable.m_ClusterInfoArray[c].m_iNumPoints;
			}
		}
		return iSelectedPoints;
	}

	/** 
	 * Sets the value of an attribute of a data point in the table.
	 * @param point_idx  index of the data point (i.e. row index).
	 * @param dim        index of the attribute dimension (i.e. column index).
	 * @param value      value to be set.
	 */
	public void setMeasurement( int point_idx, int dim, double value )
	{
		if (m_bHasClusterCol || (!m_bHasClusterCol && dim < map.columns()-1 ) )
		{
			input.setMeasurement(point_idx, dim, value);
		}
		else
		{
			m_dClusterColArray[point_idx][0] = value;
		}
	}
	
	/**
	 * Returns the type of a table column (NUMERIC, ORDINAL, CATEGORICAL, ATTRIBUTE)
	 * @param dim   index of the column
	 */
	public ColType getColType( int dim ) {
		
		if (m_bHasClusterCol || (!m_bHasClusterCol && dim < map.columns()-1 ) ) {			
			for( int i : map.getColumnSamples(dim) ) {
				if (input.getColType(i) == ColType.CATEGORICAL) {
					return ColType.CATEGORICAL;
				}
				if (input.getColType(i) == ColType.NUMERIC ) {
					
					return ColType.NUMERIC;
				}
				if (input.getColType(i) == ColType.ORDINAL ) {
					
					return ColType.ORDINAL;
				}
				if (input.getColType(i) == ColType.ATTRIBUTE ) {
					
					return ColType.ATTRIBUTE;
				}
			}
		}
//		return ColType.ATTRIBUTE;
		return ColType.NUMERIC;
	}

	/**
	 * Gets called when an operator is activated.
	 */
	public void activate()
	{
		isActive = true;
		updateMap();
		updateFunction();
		isLazy  		= true;
		setView( new ClusterView( this ) );		
	}
	
	/**
	 * 
	 */
	public String getSaveString( ) {
		
		String saveString = "";
		
//		saveString += colorByCol;
//		saveString += ",";
//		saveString += cullColorBy;
		saveString += m_ClusterTable.m_iNumClusters;
		return saveString;
	}

	/**
	 * Returns the string name of a column.
	 */
	public String getColName( int dim )
	{
		if (m_bHasClusterCol || (!m_bHasClusterCol && dim < map.columns()-1 )) {
		
			ArrayList<Integer> colsamp = map.getColumnSamples(dim); 
			if (colsamp.size() > 1 ) {
				
				return (this.toString() + dim);
			}
			else if (colsamp.size() == 1 ){
				
				return input.getColName(colsamp.get(0));
			}
		}
		return s_sClusterColName;
	}


	/**
	 * Function to filter the data using their cluster id. 
	 * @author hyounesy
	 */
	
	public class ClusterFunction extends AppendFunction implements Function, Serializable
	{
		private static final long serialVersionUID = -7422000613556512121L;
		ClusterOp 			m_ClusterOp = null;
		public ClusterFunction(ClusterOp op)
		{
			super(op.input, op.m_dClusterColArray, op.map);
			m_ClusterOp  = op;
		}

		@Override
		public double compute(int row, int col) 
		{
			boolean bAllSelected = m_ClusterOp.input.rows() == m_ClusterOp.rows();
			if (bAllSelected || m_ClusterOp.m_ClusterTable.m_ClusterInfoArray == null)
			{
				return super.compute(row, col);
			}

			int iSelectedPoints = 0;
			int iRow = -1;
			for (int c = 0; c  < m_ClusterOp.m_ClusterTable.m_iNumClusters; c++)
			{
				if (m_ClusterOp.m_ClusterTable.m_ClusterInfoArray[c].m_bSelected)
				{
					if (row < iSelectedPoints + m_ClusterOp.m_ClusterTable.m_ClusterInfoArray[c].m_iNumPoints)
					{
						int iIndex = m_ClusterOp.m_ClusterTable.m_ClusterInfoArray[c].m_iFirstIndex + row - iSelectedPoints;
						iRow = m_ClusterOp.m_ClusterTable.m_iSortedIndexArray[iIndex];
						break;
					}
					iSelectedPoints += m_ClusterOp.m_ClusterTable.m_ClusterInfoArray[c].m_iNumPoints;
				}
			}
			if (iRow < 0)
			{
				iRow = 0;
			}
			return super.compute(iRow, col);
		}

//		public double[] invert( Map map, int row, int col, double value ) ...
	}
	
	
	@Override
	public void updateFunction()
	{
		if (!m_bHasClusterCol ) {
			//function = new AppendFunction(input, m_dClusterColArray, map);
			function = new ClusterFunction(this);
		}
		else {
			function = new IdentityFunction( input );
		}
		runKMeans();
	}
	
	public void updateMap()
	{
		// handle the color operator
		m_bHasClusterCol = false;
		for( int i = 0; i < input.columns(); i++ ) {
			
			if (input.getColName(i).equalsIgnoreCase(s_sClusterColName)) //	&&	input.getColType(i) == ColType.ATTRIBUTE ) {
			{
				m_bHasClusterCol = true;
				m_iClusterCol 	= i;
			}
		}
		
		if (!m_bHasClusterCol) {
			m_iClusterCol 	= input.columns();
			m_dClusterColArray = new double[input.rows()][1];
			map 		= Map.generateDiagonalMap(input.columns()+1);
		}
		else
		{
			map 		= Map.generateDiagonalMap(input.columns());
		}
		m_ClusterTable.setTable(input);
		m_ClusterTable.updateClusters();
	}
	

	public void runKMeans()
	{
	    m_ClusterTable.runKMeans();

	    int iDataSize = input.rows();
		for( int idata = 0; idata < iDataSize; idata++ )
		{
			int c = m_ClusterTable.m_iClusterIDArray[idata];
			setMeasurement(idata, m_iClusterCol, c);
		}
	}

	
	/**
	 * The GUI view class for the cluster operator.
	 *  
	 * @author hyounesy
	 *
	 */
	public class ClusterView extends OperatorView implements ChangeListener, ListSelectionListener
	{
		private static final long serialVersionUID = 2918564590423897769L;
		
		JTabbedPane	m_MasterPanel 			= null;
		JSlider 	m_NumClustersSlider 	= null;
		JSlider     m_OutlierDistSlider		= null;
	    JList 		m_ClusterSelectionList	= null;
	    ScreePlot 	m_ClusterScreePlot		= null;
	    ScreePlot 	m_NeighborsScreePlot	= null;
		JButton 	m_CalculateButton		= null;
		JSpinner 	m_SpinnerRepeatCalcAll 	= null;
		JCheckBox	m_AutoCalculateAllCheck	= null; // automatically calculates all, whenever data changes
		JCheckBox	m_SelectBestKCheck		= null; // automatically selects the K which produces best quality  
		JCheckBox	m_FilterOutliersCheck	= null;  
		JButton 	m_CalcNeighborhoodButton= null;
		ClusterCompareView m_CCView 		= null;
		HeightMapPanel m_HeightMapPanel 	= null;
		
		//JScrollPane jsp = null;

		/**
		 *  actually calculate the color column values based on gui settings
		 */
		public void populateClusterColumn()
		{
			((ClusterOp)operator).runKMeans();
		}
		
		void buildNumClustersSlider()
		{
			// K-slider
			Hashtable<Integer,JLabel> labelTableSize = new Hashtable<Integer,JLabel>();
			for( int i = 1; i <= 15; i++) {				
				labelTableSize.put(new Integer(i),new JLabel(""+i));
			}
			
			m_NumClustersSlider = new JSlider( 	JSlider.HORIZONTAL, 1, ((ClusterOp)operator).m_ClusterTable.m_iMaxNumClusters, 1);
			m_NumClustersSlider.setName("kSlider");
			m_NumClustersSlider.setMajorTickSpacing( 2 );
			m_NumClustersSlider.setMinorTickSpacing( 1 );
			m_NumClustersSlider.setLabelTable(labelTableSize);
			m_NumClustersSlider.setPaintLabels(true);	
			m_NumClustersSlider.setValue(((ClusterOp)operator).m_ClusterTable.m_iNumClusters);
			m_NumClustersSlider.addChangeListener(this);
			
			
			labelTableSize = new Hashtable<Integer,JLabel>();
			for( int i = 0; i <= 10; i+= 5) {				
				labelTableSize.put(new Integer(i*10),new JLabel("%"+i*10));
			}
			m_OutlierDistSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
			m_OutlierDistSlider.setName("kSlider");
			m_OutlierDistSlider.setMajorTickSpacing( 10 );
			m_OutlierDistSlider.setMinorTickSpacing( 1 );
			m_OutlierDistSlider.setLabelTable(labelTableSize);
			m_OutlierDistSlider.setPaintLabels(true);	
			m_OutlierDistSlider.setValue((int)(m_ClusterTable.m_dMaxOutlierDist * 100 / m_ClusterTable.m_dMaxPointDist));
			m_OutlierDistSlider.addChangeListener(this);
		}

		// Update the JList for cluster selection
		public void buildClusterSelectionList()
		{
			ClusterOp op = (ClusterOp)operator;
			// populate the cluster list
			String clusterListLabel[] = new String[op.m_ClusterTable.m_iNumClusters];
			int iSelectedClusters[] = new int[op.m_ClusterTable.m_iNumClusters];
			int iSelected = 0;
			for (int c = 0; c < op.m_ClusterTable.m_iNumClusters; c++)
			{
				clusterListLabel[c] = Integer.toString(c + 1);
				if (op.m_ClusterTable.m_ClusterInfoArray != null && op.m_ClusterTable.m_ClusterInfoArray[c].m_bSelected)
				{
					iSelectedClusters[iSelected] = c;
					iSelected++;
				}
			}

			m_ClusterSelectionList = new JList(clusterListLabel);
		    m_ClusterSelectionList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		    m_ClusterSelectionList.setVisibleRowCount(-1);
		    m_ClusterSelectionList.setName("clusterList");
			if (iSelected > 0)
			{
				iSelectedClusters = Arrays.copyOf(iSelectedClusters, iSelected);
	        	m_ClusterSelectionList.setSelectedIndices(iSelectedClusters);
			}
			m_ClusterSelectionList.addListSelectionListener(this);
		}
		
		public void buildClusterScreePlot()
		{
			ClusterOp op = (ClusterOp)operator;
			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			for(int i = 0; i < op.m_ClusterTable.m_iMaxNumClusters; i++)
			{
				double fVal = 0;
				if (op.m_ClusterTable.m_AllClusterInfoArray != null && i < op.m_ClusterTable.m_AllClusterInfoArray.length)
				{
					fVal = op.m_ClusterTable.m_AllClusterInfoArray[i].m_QualityMeasuresArray[op.m_ClusterTable.m_Quality.ordinal()];
				}
				univarQuant.add(new Double(fVal));
				univarNames.add(Integer.toString(i));
			}
			
			m_ClusterScreePlot = new ScreePlot(univarQuant, univarNames, false, 
											new Comparator<Double>() {public int compare(Double o1, Double o2) {return 0;}}, // no need to sort
											null);
			m_ClusterScreePlot.cutoff = op.m_ClusterTable.m_iNumClusters - 1;
			m_ClusterScreePlot.useDimensionNames = false;
			m_ClusterScreePlot.isCutoffLeft = true;
			//scree.addLogStateCheckbox( jcb );
			m_ClusterScreePlot.addChangeListener( this );
		}

		public void buildNeighborsScreePlot()
		{
			ClusterOp op = (ClusterOp)operator;
			ArrayList<Double> univarQuant = new ArrayList<Double>();
			ArrayList<String> univarNames = new ArrayList<String>();
			
			if (op.m_ClusterTable.m_iNumNeighborhoodPointsArray != null)
			{
				for(int i = 0; i < op.m_ClusterTable.m_iNumNeighborhoodPointsArray.length; i++)
				{
					univarQuant.add(new Double(op.m_ClusterTable.m_iNumNeighborhoodPointsArray[i]));
					univarNames.add(Integer.toString(i));
				}
				for (int i = op.m_ClusterTable.m_iNumNeighborhoodPointsArray.length; i < op.input.rows(); i++)
				{
					univarQuant.add(new Double(0));
					univarNames.add(Integer.toString(i));
				}
			}
			univarQuant.add(new Double(0));
			univarNames.add(Integer.toString(0));
			
			m_NeighborsScreePlot = new ScreePlot(univarQuant, univarNames, false, 
											new Comparator<Double>() {public int compare(Double o1, Double o2) {return 0;}}, // no need to sort
											null);
			m_NeighborsScreePlot.cutoff = Math.max(0, Math.min(op.m_ClusterTable.m_iMinOutlierNeighbors - 1, op.m_ClusterTable.m_iMaxNumNeighbors));
			m_NeighborsScreePlot.useDimensionNames = false;
			m_NeighborsScreePlot.isCutoffLeft = true;
			m_NeighborsScreePlot.addChangeListener( this );
		}

		/**
		 *  Construct and populate the gui
		 */
		public void buildGui()
		{
			int iSelectedTabIndex = 0;
			if (m_MasterPanel != null)
			{
				iSelectedTabIndex = m_MasterPanel.getSelectedIndex();
				this.removeAll();
				m_MasterPanel.removeAll();
			}
			m_MasterPanel 			= new JTabbedPane();// new GridLayout(1,3,0,0));
			this.setLayout(new BorderLayout(5,5));
			this.add(m_MasterPanel, BorderLayout.CENTER);
			this.setBorder(	BorderFactory.createEmptyBorder(10, 10, 10, 10));

			ClusterOp op = (ClusterOp)operator;
			
			m_CalcNeighborhoodButton = new JButton("Calc Neighbor");
			m_CalcNeighborhoodButton.addActionListener(this);

			
			buildNumClustersSlider();

			//------------------------
			//------ measure Tab
			//------------------------
			JPanel measurePanel = new JPanel(new BorderLayout(5, 5));
			JPanel clusterScreePanel = new JPanel(new BorderLayout(5, 5));
			measurePanel.add(clusterScreePanel, BorderLayout.CENTER);
			buildClusterScreePlot();
			clusterScreePanel.add(m_ClusterScreePlot, BorderLayout.CENTER);
			
			JPanel typePanel		= new JPanel( new GridLayout(1,2,0,0) );
			clusterScreePanel.add(typePanel, BorderLayout.NORTH);
			JComboBox methodBox  = EnumUtils.getComboBox(ClusterTable.Method.values(), op.m_ClusterTable.m_Method, "methodBox", this);
			JComboBox metricBox  = EnumUtils.getComboBox(ClusterTable.DistanceMetric.values(), op.m_ClusterTable.m_Metric, "metricBox", this);
			typePanel.add(methodBox);
			typePanel.add(metricBox);
						
			JPanel calcAllPanel = new JPanel (new GridLayout(2, 3, 0, 0));
			clusterScreePanel.add(calcAllPanel, BorderLayout.SOUTH);

			m_SpinnerRepeatCalcAll   = new JSpinner(new SpinnerNumberModel(op.m_ClusterTable.m_iRepeatCalculateAll, 0, 100, 1));
			calcAllPanel.add(m_SpinnerRepeatCalcAll);
			m_SpinnerRepeatCalcAll.addChangeListener(this);

			m_AutoCalculateAllCheck	= new JCheckBox("Auto CalcAll");
			m_AutoCalculateAllCheck.setActionCommand("autoCalculateAll");
			m_AutoCalculateAllCheck.setSelected(op.m_ClusterTable.m_bAutoCalculateAll);
			m_AutoCalculateAllCheck.addActionListener(this);
			calcAllPanel.add(m_AutoCalculateAllCheck);

			m_CalculateButton		= new JButton("Calculate All");
			m_CalculateButton.addActionListener(this);
			calcAllPanel.add(m_CalculateButton);

			JComboBox qualityBox = EnumUtils.getComboBox(ClusterTable.QualityMeasure.values(), op.m_ClusterTable.m_Quality, "qualityBox", this);
			calcAllPanel.add(qualityBox);

			m_SelectBestKCheck	= new JCheckBox("Auto Best K");
			m_SelectBestKCheck.setActionCommand("bestK");
			m_SelectBestKCheck.setSelected(op.m_ClusterTable.m_bSelectBestK);
			m_SelectBestKCheck.addActionListener(this);
			calcAllPanel.add(m_SelectBestKCheck);

			//JPanel kPanel			= new JPanel( new BorderLayout(5,5) );
			//typePanel.add(kPanel);
			//kPanel.add(m_NumClustersSlider,"Center");
			//kPanel.add(new JLabel("Num Clusters"), BorderLayout.WEST);

//			buildClusterSelectionList();
//			JScrollPane clusterListPane = new JScrollPane(m_ClusterSelectionList);
//			typePanel.add(clusterListPane);
			
			
			//------------------------
			//------ outlier Tab
			//------------------------
			JPanel outlierPanel = new JPanel(new BorderLayout(5, 5));
			JPanel neighborScreePanel = new JPanel(new BorderLayout(5, 5));
			//outlierPanel.add(neighborScreePanel, BorderLayout.CENTER);
			buildNeighborsScreePlot();
			neighborScreePanel.add(m_NeighborsScreePlot, BorderLayout.CENTER);
			//neighborScreePanel.add(m_OutlierDistSlider, BorderLayout.NORTH);
			m_FilterOutliersCheck = new JCheckBox("Filter Outliers");
			m_FilterOutliersCheck.setSelected(op.m_ClusterTable.m_bFilterOutliers);
			m_FilterOutliersCheck.addActionListener(this);
			outlierPanel.add(m_FilterOutliersCheck, BorderLayout.SOUTH);
			// heightmap
			m_HeightMapPanel = new HeightMapPanel(m_ClusterTable.m_NeighborhoodXAxis,
												  m_ClusterTable.m_NeighborhoodYAxis,
												  m_ClusterTable.m_NeighborhoodValues);
			outlierPanel.add(m_HeightMapPanel, BorderLayout.CENTER);
			outlierPanel.add(m_CalcNeighborhoodButton, BorderLayout.NORTH);
			m_HeightMapPanel.addChangeListener(this);

			//------------------------
			//------ compare Tab
			//------------------------
			JPanel comparePanel = new JPanel(new BorderLayout(5, 5));
			m_CCView = new ClusterCompareView();
			if (op.m_ClusterTable != null)
			{
				m_CCView.setClusterTable(op.m_ClusterTable);
				m_CCView.setSelectedK(op.m_ClusterTable.m_iNumClusters);
			}
			m_CCView.addChangeListener(this);
			comparePanel.add(m_CCView, BorderLayout.CENTER);
			

			BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < 200; i++)
				for (int j = 0; j < 200; j++)
				{
					image.setRGB(i, j, i + (j << 8));
				}
			//calcAllPanel.add(new ImagePanel(image));
			
			m_MasterPanel.addTab("Measures", null, measurePanel, "Uses cluster quality measures");
			m_MasterPanel.addTab("Compare", null, comparePanel, "Compares data membership between clusters");
			m_MasterPanel.addTab("Outliers", null, outlierPanel, "Uses density to filter outliers");
	        m_MasterPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	        m_MasterPanel.setSelectedIndex(iSelectedTabIndex);
			

		}
		
		public ClusterView(Operator o)
		{
			super(o);
			buildGui();
			populateClusterColumn();
		}
		
		void calculateAll()
		{
			ClusterOp op = (ClusterOp)operator;
			for (int i = 0; i < op.m_ClusterTable.m_iRepeatCalculateAll + 1; i++)
			{
				op.m_ClusterTable.calculateAll(op.m_ClusterTable.m_iMaxNumClusters);
			}
			if (op.m_ClusterTable.m_bSelectBestK)
			{
				int iBestNumClusters = op.m_ClusterTable.getBestK(op.m_ClusterTable.m_Quality);
				if (iBestNumClusters != op.m_ClusterTable.m_iNumClusters)
				{
					op.m_ClusterTable.m_iNumClusters = iBestNumClusters;
				}
			}
		}
		
		public void actionPerformed(ActionEvent e)
		{

			ClusterOp op = (ClusterOp)operator;
			boolean bRecalculate = false;
			boolean bBuildGui = false;

			// the underlying operator has changed
			if( e.getSource() instanceof Operator )
			{
				bBuildGui = true;
			}

			if (e.getSource() instanceof JComboBox )
			{
				JComboBox cb = (JComboBox)e.getSource();
				if (cb.getName().equalsIgnoreCase("methodBox"))
				{
					op.m_ClusterTable.m_Method = ClusterTable.Method.values()[cb.getSelectedIndex()];
					bRecalculate = true;
					bBuildGui = true;
				}
				else if (cb.getName().equalsIgnoreCase("metricBox"))
				{
					op.m_ClusterTable.m_Metric = ClusterTable.DistanceMetric.values()[cb.getSelectedIndex()];
					bRecalculate = true;
					bBuildGui = true;
				}
				else if (cb.getName().equalsIgnoreCase("qualityBox"))
				{
					op.m_ClusterTable.m_Quality = ClusterTable.QualityMeasure.values()[cb.getSelectedIndex()];
					bBuildGui = true;
				}
			}
			if (e.getSource() == m_CalculateButton)
			{
				calculateAll();
				bRecalculate = true;
				bBuildGui = true;
			}
			if (e.getSource() == m_CalcNeighborhoodButton)
			{
				op.m_ClusterTable.calculateAllNeighborhoods();
				bBuildGui = true;
			}
			else if( e.getSource() == m_AutoCalculateAllCheck)
			{
				op.m_ClusterTable.m_bAutoCalculateAll = m_AutoCalculateAllCheck.isSelected();
			}
			else if( e.getSource() == m_SelectBestKCheck)
			{
				op.m_ClusterTable.m_bSelectBestK = m_SelectBestKCheck.isSelected();
			}
			else if( e.getSource() == m_FilterOutliersCheck)
			{
				op.m_ClusterTable.m_bFilterOutliers = m_FilterOutliersCheck.isSelected();
			}
			
			if (bRecalculate)
			{
		    	populateClusterColumn( );
				operator.tableChanged( new TableEvent(operator, TableEvent.TableEventType.TABLE_CHANGED ), true);
			}
			if (bBuildGui)
			{
				buildGui();
				this.validate();
				this.repaint();
			}
				
			/*
			// the underlying operator has changed
			if (e.getSource() instanceof Operator ) {

				buildGui();
				populateClusterColumn( );
				operator.tableChanged( new TableEvent( operator, TableEvent.TableEventType.ATTRIBUTE_CHANGED, s_sClusterColName, null, false) );
				this.validate();
				this.repaint();
			}
			*/
		}
		
		@Override
		public void stateChanged(ChangeEvent e)
		{
			ClusterOp op = (ClusterOp)operator;
			boolean bClustersChanged = false;
			if (e.getSource() == m_NumClustersSlider) {
			    if (!m_NumClustersSlider.getValueIsAdjusting()) {
			    	op.m_ClusterTable.m_iNumClusters = (int)m_NumClustersSlider.getValue();
			    	bClustersChanged = true;
			    }
			}
			if (e.getSource() == m_OutlierDistSlider) {
			    if (!m_OutlierDistSlider.getValueIsAdjusting()) {
			    	op.m_ClusterTable.m_dMaxOutlierDist = ((int)m_OutlierDistSlider.getValue() * 0.01) * op.m_ClusterTable.m_dMaxPointDist;
			    	if (op.m_ClusterTable.m_bFilterOutliers)
			    	{
			    		op.m_ClusterTable.calculateNeighbors(op.m_ClusterTable.m_dMaxOutlierDist);
				    	bClustersChanged = true;
			    	}
			    }
			}
			else if( e.getSource() == m_ClusterScreePlot)
			{
		    	op.m_ClusterTable.m_iNumClusters = m_ClusterScreePlot.cutoff+1;
		    	bClustersChanged = true;
			}
			else if (e.getSource() == m_CCView)
			{
				op.m_ClusterTable.m_iNumClusters = m_CCView.m_iSelectedK;
				bClustersChanged = true;
			}
			else if( e.getSource() == m_NeighborsScreePlot)
			{
		    	if (op.m_ClusterTable.m_bFilterOutliers)
		    	{
			    	op.m_ClusterTable.m_iMinOutlierNeighbors = m_NeighborsScreePlot.cutoff+1;
			    	updateMap();
					updateFunction();
			    	bClustersChanged = true;
		    	}
			}
			else if (e.getSource() == m_HeightMapPanel)
			{
		    	if (op.m_ClusterTable.m_bFilterOutliers)
		    	{
			    	op.m_ClusterTable.m_dMaxOutlierDist = (m_HeightMapPanel.m_fX * op.m_ClusterTable.m_dMaxPointDist);
		    		op.m_ClusterTable.m_iMinOutlierNeighbors = (int)(m_HeightMapPanel.m_fY * op.m_ClusterTable.m_iMaxNumNeighbors);
		    		updateMap();
		    		updateFunction();
		    		op.m_ClusterTable.calculateNeighbors(op.m_ClusterTable.m_dMaxOutlierDist);
		    		bClustersChanged = true;
		    	}
			}
			else if (e.getSource() == m_SpinnerRepeatCalcAll)
			{
				op.m_ClusterTable.m_iRepeatCalculateAll = ((Integer)m_SpinnerRepeatCalcAll.getValue());
			}
			
	    	if (bClustersChanged)
	    	{
		    	populateClusterColumn( );
				buildGui();
				operator.tableChanged( new TableEvent(operator, TableEvent.TableEventType.TABLE_CHANGED ), true);
				this.validate();
				this.repaint();
	    	}
		}
		
		public void valueChanged(ListSelectionEvent e)
		{
			ClusterOp op = (ClusterOp)operator;
	        if (e.getSource() instanceof JList)
	        {
	        	JList list = (JList) e.getSource();
	        	if (list.getName() == "clusterList")
	        	{
	        		for (int c = 0; c < op.m_ClusterTable.m_iNumClusters; c++)
	        		{
	        			op.m_ClusterTable.m_ClusterInfoArray[c].m_bSelected = list.isSelectedIndex(c);
	        		}
					operator.tableChanged( new TableEvent(operator, TableEvent.TableEventType.TABLE_CHANGED ), true);
					this.validate();
					this.repaint();
	        	}
	        }
		}
		
	}
}