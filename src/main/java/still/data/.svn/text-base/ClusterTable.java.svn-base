package still.data;

import java.util.Arrays;
import java.util.Random;

public class ClusterTable
{
	public Table m_InputTable	= null;

	public class ClusterInfo
	{
		public boolean 	m_bSelected = true;
		public int 		m_iFirstIndex = -1;
		public int 		m_iNumPoints = 0;
		public double 	m_dCentroid[] = null;
		
		ClusterInfo()
		{
		}
		
		ClusterInfo(int iDim)
		{
			setDim(iDim);
		}
		void setDim (int iDim)
		{
			m_dCentroid = new double[iDim];
		}
		ClusterInfo(ClusterInfo rhs)
		{
			m_bSelected = rhs.m_bSelected;
			m_iFirstIndex = rhs.m_iFirstIndex;
			m_iNumPoints = rhs.m_iNumPoints;
			m_dCentroid = rhs.m_dCentroid.clone();
		}
	}
	
	// keeps the clustering info for each K (number of clusters)
	public class AllClusterInfo
	{
		AllClusterInfo(int iDataSize, int iDim, int iNumClusters)
		{
			m_ClusterInfoArray = new ClusterInfo[iNumClusters + 1]; // last one for outliers
			for (int i = 0; i < m_ClusterInfoArray.length; i++)
			{
				m_ClusterInfoArray[i] = new ClusterInfo(iDim);
			}
			m_QualityMeasuresArray = new double[QualityMeasure.values().length];
			m_iClusterIDArray = new int[iDataSize];
			Arrays.fill(m_iClusterIDArray, 0);
		}
		
		public ClusterInfo[]	m_ClusterInfoArray		= null;
		public double[] 	  	m_QualityMeasuresArray	= null;
		public int[] 			m_iClusterIDArray 		= null; // cache the cluster id for each point
	}
	
	public AllClusterInfo[] m_AllClusterInfoArray		= null;
	
	/** Different clustering methods */
	public enum Method
	{ // http://en.wikipedia.org/wiki/Cluster_analysis#Types_of_clustering
		KMEANS,
		HIERARCHICAL,
		PROJECTED
	}
	
	/** Different distance metrics (similarity measures) which can be used to calculate the distances between data points */
	public enum DistanceMetric { // check http://en.wikipedia.org/wiki/Cluster_analysis#Distance_measure for more
		MANHATTAN_DISTANCE,
		EUCLIDEAN_DISTANCE,
		EUCLIDEAN_SQUARED_DISTANCE
//		PEARSON_CORRELATION,	// Use the Pearson Linear  Correlation (PLC) coefficient to cluster together genes or samples with similar behavior; genes or samples with opposite behavior are assigned to different clusters.
//		PEARSON_SQUARED,		// Use the squared Pearson Correlation coefficient to cluster together genes with similar or opposite behaviors (i.e. genes that are highly correlated and those that are highly anti-correlated are clustered together).
//		CHEBYCHEV,			// Use Chebychev distance to cluster together genes that do not show dramatic expression differences in any samples; genes with a large expression difference in at least one sample are assigned to different clusters.
//		SPEARMAN			// Use Spearman Correlation to cluster together genes whose expression profiles have similar shapes or show similar general trends (e.g. increasing expression with time), but whose expression levels may be very different.
	}
	
	
	/*
		* Intra- and Inter-cluster distance: high intra, low inter
		* Jagota suggests a measure that emphasizes cluster tightness or homogeneity. The lower = clusters are tighter = better
		* Entropy [Sha48] (the lower, the better)
		* F-measure [LA99] (the higher, the better)
	    * Gap statistic
	    * Log likelihood
	 */
	public enum QualityMeasure
	{
		INTRA_CLUSTER_DIST,
		SUM_SQUARED_ERROR
	}
	
	/** Clustering method. */
	public Method 			m_Method = Method.KMEANS;
	
	/** Current distance metric used to calculate the distances between data points. */
	public DistanceMetric 	m_Metric = DistanceMetric.EUCLIDEAN_DISTANCE;
	
	/** Array of cluster info. size: [iNumClusters] */
	public ClusterInfo 		m_ClusterInfoArray[] = null;
  
	/** Array of cluster IDs for each data point. size: [iDataSize] */
	public int 				m_iClusterIDArray[] = null;
  
	/** Array of data point indices, sorted by their cluster IDs. size: [iDataSize] */
  	public int 				m_iSortedIndexArray[] = null; 
	
	/** Number of clusters. */
	public int 				m_iNumClusters = 1;
	
	/** Random number generator (used to randomly pick the initial cluster centroids). */
	Random 					m_RandomGenerator = new Random();
	
	public QualityMeasure m_Quality = QualityMeasure.INTRA_CLUSTER_DIST;

	/** maxium number of clusters to precalculate in calculateAll() */
	public int		m_iMaxNumClusters 		= 15;

	/** automatically calculates all, whenever data changes */
	public boolean	m_bAutoCalculateAll	= false;
	
	/** automatically selects the K which produces best quality */
	public boolean	m_bSelectBestK	= false;   
	
	/** Number of times to repeat calculateAll */
	public int		m_iRepeatCalculateAll = 0;
	
	/** maximum distance between points to consider them neighbors */
	public double 	m_dMaxOutlierDist  = 0.5;
	/** minimum number of neighbors between points to keep them as non-outliers */
	public int 	m_iMinOutlierNeighbors = 0;
	/** whether to filter the outlier points*/
	public boolean	m_bFilterOutliers = false;
	
	
	/** maximum distance between points: updated in calculateNeighbors whenever input data changes.*/
	public double	m_dMaxPointDist    = 0;
	/** number of neighbors per points, updated in calculateNeighbors */
	int		m_iNumNeighborsArray[] = null;
	/** maximum number of neighbors: updated in calculateNeighbors whenever input data changes. */
	public int		m_iMaxNumNeighbors = 0;
	/** number of points that have at least certain number of neighbors, updated in calculateNeighbors */
	public int		m_iNumNeighborhoodPointsArray[] = null;
	/** maximum number of points in a neighborhood: updated in calculateNeighbors whenever input data changes. */
	int		m_iMaxNumNeighborPoints = 0;
	
	
	
	/**
	 * Simple Constructor 
	 */
	public ClusterTable(Table newInput)
	{
		m_InputTable = newInput;
	}

	public void setTable(Table newInput)
	{
		m_InputTable = newInput;
	}

	public int getInputSize()
	{
		return m_InputTable.rows();
	}
	
	public int getInputDim()
	{
		return m_InputTable.columns();
	}
	
	public double[] getInputPoint( int point_idx )
	{
		return m_InputTable.getPoint(point_idx);
	}
	
	public double getInputMeasurement( int point_idx, int dim )
	{
		return m_InputTable.getMeasurement(point_idx, dim);
	}
	
	public void setNumClusters(int K)
	{
		m_iNumClusters = K;
	}

	public int getBestK(QualityMeasure measure)
	{
		int k = 1;
		if (m_AllClusterInfoArray != null)
		{
			double dBestVal = m_AllClusterInfoArray[0].m_QualityMeasuresArray[measure.ordinal()];
			for (int i = 1; i < m_AllClusterInfoArray.length; i++)
			{//TODO: for some quality measures, best value is the max value
				if (m_AllClusterInfoArray[i].m_QualityMeasuresArray[measure.ordinal()] < dBestVal)
				{
					dBestVal = m_AllClusterInfoArray[i].m_QualityMeasuresArray[measure.ordinal()];
					k = i + 1;
				}
			}
		}
		return k;
	}
	
	public void updateClusters()
	{
		m_AllClusterInfoArray = null;
		
		if (m_bFilterOutliers)
		{
			calculateNeighbors(m_dMaxOutlierDist);
		}
		
		if (m_bAutoCalculateAll)
		{
			for (int i = 0; i < m_iRepeatCalculateAll + 1; i++)
			{
				calculateAll(m_iMaxNumClusters);
			}
		}
		
		if (m_bSelectBestK)
		{
			m_iNumClusters = getBestK(m_Quality);
		}
	}
	
	double getDist(double[] p1, double[] p2, DistanceMetric metric)
	{
		double fDist = 0;
		for (int d = 0; d < p1.length; d++)
		{
			double fDimDist = p1[d] - p2[d]; // dintance for dimension d
			if (metric == DistanceMetric.MANHATTAN_DISTANCE)
				fDimDist = java.lang.Math.abs(fDimDist);
			if (metric == DistanceMetric.EUCLIDEAN_DISTANCE || metric == DistanceMetric.EUCLIDEAN_SQUARED_DISTANCE)
				fDimDist *= fDimDist; 
			fDist += fDimDist;
		}
		
		if (metric == DistanceMetric.EUCLIDEAN_DISTANCE)
		{
			fDist = Math.sqrt(fDist);
		}
		
		return fDist;
	}
	
	public boolean isOutlier(int iPointIndex)
	{
		try
		{
			if (m_bFilterOutliers && m_iNumNeighborsArray != null)
				return m_iNumNeighborsArray[iPointIndex] < m_iMinOutlierNeighbors;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
		
	public void runKMeans()
	{
		int iDataDim = getInputDim();
	    int iDataSize = getInputSize();
		if (m_iNumClusters <= 0)
		{
			System.out.print("Too few clusters. Use initClusterCentroids to specify the number of clusters.\n");
			return;
		}
		if (iDataSize <= 0)
		{
			System.out.print("No data points to cluster.\n");
			return;
		}
		if (iDataDim <= 0)
		{
			System.out.print("DataDim == 0. Nothing to do.\n");
			return;
		}
	    
		m_ClusterInfoArray = new ClusterInfo[m_iNumClusters + 1]; // last one for outliers
	    for (int c = 0; c < m_ClusterInfoArray.length; c++)
	    	m_ClusterInfoArray[c] = new ClusterInfo(iDataDim);
	    

	    if (m_iClusterIDArray == null || m_iClusterIDArray.length != iDataSize)
	    	m_iClusterIDArray = new int[iDataSize];
		Arrays.fill(m_iClusterIDArray, -1);
		
	    if (m_iSortedIndexArray == null || m_iSortedIndexArray.length != iDataSize)
	    	m_iSortedIndexArray = new int[iDataSize];
		Arrays.fill(m_iSortedIndexArray, -1);
				

	    if (m_AllClusterInfoArray != null && m_AllClusterInfoArray.length >= m_iNumClusters)
	    { // get the centroids from the precalculated list
			for (int c = 0; c < m_iNumClusters; c++)
			{
				m_ClusterInfoArray[c] = new ClusterInfo(m_AllClusterInfoArray[m_iNumClusters - 1].m_ClusterInfoArray[c]);
			}
			m_ClusterInfoArray[m_iNumClusters] = new ClusterInfo(iDataDim);
	    }
	    else
	    {
			// initialize the centroids randomly
			for (int c = 0; c < m_iNumClusters; c++)
			{
				int randIndex = m_RandomGenerator.nextInt(iDataSize);
				for (int d = 0; d < iDataDim; d++)
	               m_ClusterInfoArray[c].m_dCentroid[d] = getInputMeasurement(randIndex, d);
			}
	    }

	    final int MAX_ITERATIONS = 100;
	    int iIterations = 0;
		boolean bClustersChanged = true;
	    while(bClustersChanged && iIterations++ < MAX_ITERATIONS)
	    {
	    	bClustersChanged = false;
	    	for (int idata = 0; idata < iDataSize; idata++)
	    	{
	    		if (isOutlier(idata))
	    		{
	    			m_iClusterIDArray[idata] = m_iNumClusters;
	    			continue;
	    		}

	    		double fMinDist = Double.MAX_VALUE; // minimum distance from the point to cluster centroids
	    		int iCluster = -1; // the cluster to which the point belongs

				// calculate the distance from the centroids
	    		for (int c = 0; c < m_iNumClusters; c++)
	    		{
		    		double fDist = getDist(getInputPoint(idata), m_ClusterInfoArray[c].m_dCentroid, m_Metric);
					if (fDist < fMinDist)
					{
						iCluster = c;
						fMinDist = fDist;
					}
	    		}
	    		
	    		if (m_iClusterIDArray[idata] != iCluster)
	    		{
	    			m_iClusterIDArray[idata] = iCluster;
	    			bClustersChanged = true;
	    		}
	    	}
	    	
	    	// calculate the new centroids
	    	if (bClustersChanged)
	    	{
	    		// initialize
	    		for (int c = 0; c < m_iNumClusters; c++)
	    		{
	    			m_ClusterInfoArray[c].m_iNumPoints = 0;
	    			for (int d = 0; d < iDataDim; d++)
		    		{
		    			 m_ClusterInfoArray[c].m_dCentroid[d] = 0;
		    		}
	    		}
	    		
	    		// sum
	    		for( int idata = 0; idata < iDataSize; idata++ )
	    		{
	    			int iCluster = m_iClusterIDArray[idata];
	    			m_ClusterInfoArray[iCluster].m_iNumPoints++;
	    			if (!isOutlier(idata))
	    			{
		    			for (int d = 0; d < iDataDim; d++)
			    		{
			    			 m_ClusterInfoArray[iCluster].m_dCentroid[d] += getInputMeasurement(idata, d);
			    		}
	    			}
	    		}
	    		
	    		// calculate new centroid
	    		for (int c = 0; c < m_iNumClusters; c++)
	    		{
	    			if (m_ClusterInfoArray[c].m_iNumPoints != 0)
	    			{
		    			for (int d = 0; d < iDataDim; d++)
			    		{
			    			 m_ClusterInfoArray[c].m_dCentroid[d] /= m_ClusterInfoArray[c].m_iNumPoints;
			    		}
	    			}
	    			else
	    			{// change the cluster centroid
	    				System.out.format("WARNING: Cluster %d has 0 members. Picking a new random centroid.\n", c);
	    				int randIndex = m_RandomGenerator.nextInt(iDataSize);
	    				for (int d = 0; d < iDataDim; d++)
	    				{
	    	               m_ClusterInfoArray[c].m_dCentroid[d] = getInputMeasurement(randIndex, d);
	    				}
	    				bClustersChanged = true;
	    			}
	    		}
	    	}
	    }
	    
	    int iCumSum = 0;
	    for (int c = 0; c < m_iNumClusters; c++)
	    {
	    	m_ClusterInfoArray[c].m_iFirstIndex = iCumSum;
	    	iCumSum += m_ClusterInfoArray[c].m_iNumPoints;
	    	m_ClusterInfoArray[c].m_iNumPoints = 0; // temporarily setting it to 0 to be used as a counter index
	    }
	    
//			m_ClusterIDs = d_Clusters;
		for( int idata = 0; idata < iDataSize; idata++ )
		{
			int c = m_iClusterIDArray[idata];
			if (c < m_iNumClusters)
			{
				m_iSortedIndexArray[m_ClusterInfoArray[c].m_iFirstIndex + m_ClusterInfoArray[c].m_iNumPoints] = idata;
				m_ClusterInfoArray[c].m_iNumPoints++;
			}
			//setMeasurement(idata, m_iClusterCol, c);
		}

	}
	
	/**
	 * Pre calculates the cluster info for a range of num clusters
	 * @param iMaxNumClusters
	 */
	public void calculateAll(int iMaxNumClusters)
	{
		int iDataDim = getInputDim();
	    int iDataSize = getInputSize();
		int iCurrentNumClusters = m_iNumClusters;
		
		AllClusterInfo oldAllClusterInfoArray[] = m_AllClusterInfoArray;
		m_AllClusterInfoArray = null;
		
		//TODO: Don't recreate this array if already in good state. 
		AllClusterInfo newAllClusterInfoArray[] = new AllClusterInfo[iMaxNumClusters];
		
		for (int k = 0; k < iMaxNumClusters; k++)
		{
			m_iNumClusters = k + 1;
			runKMeans();
			newAllClusterInfoArray[k] = new AllClusterInfo(iDataSize, iDataDim, m_ClusterInfoArray.length);
			for (int c = 0; c < m_ClusterInfoArray.length; c++)
			{
				newAllClusterInfoArray[k].m_ClusterInfoArray[c] = new ClusterInfo(m_ClusterInfoArray[c]);
			}
			System.arraycopy(m_iClusterIDArray, 0, newAllClusterInfoArray[k].m_iClusterIDArray, 0, m_iClusterIDArray.length);
			
			// calculate the clustering quality measures (e.g. inter-cluster distances)
			for (int iM = 0; iM < QualityMeasure.values().length; iM++)
  		{
				newAllClusterInfoArray[k].m_QualityMeasuresArray[iM] = 0;
  		}
			
			for (int idata = 0; idata < iDataSize; idata++)
			{// calculate the quality measures
				
	    		if (isOutlier(idata))
	    		{
	    			continue;
	    		}

	    		double fEuclDist = getDist(getInputPoint(idata), m_ClusterInfoArray[m_iClusterIDArray[idata]].m_dCentroid, DistanceMetric.EUCLIDEAN_DISTANCE);
	    		newAllClusterInfoArray[k].m_QualityMeasuresArray[QualityMeasure.INTRA_CLUSTER_DIST.ordinal()] +=
	    			fEuclDist / m_ClusterInfoArray[m_iClusterIDArray[idata]].m_iNumPoints;
	    		
	    		newAllClusterInfoArray[k].m_QualityMeasuresArray[QualityMeasure.SUM_SQUARED_ERROR.ordinal()] += (fEuclDist * fEuclDist) * m_iNumClusters;
			}
			
			if (oldAllClusterInfoArray != null && k < oldAllClusterInfoArray.length)
			{// use the old clustering, if the new one has a worst score.
				if (oldAllClusterInfoArray[k].m_QualityMeasuresArray[QualityMeasure.SUM_SQUARED_ERROR.ordinal()] <
					newAllClusterInfoArray[k].m_QualityMeasuresArray[QualityMeasure.SUM_SQUARED_ERROR.ordinal()])
				{
					newAllClusterInfoArray[k] = oldAllClusterInfoArray[k]; 
				}
			}
		}
		m_AllClusterInfoArray = newAllClusterInfoArray;
		m_iNumClusters = iCurrentNumClusters;
		runKMeans();
	}
	
	/**
	 * Calculates the neighborhood information.
	 * Updates: m_iNumNeighborsArray (number of neighbors for each point),
	 * 			m_dMaxPointDist (max distance between points),
	 * 			m_iMaxNumNeighbors (max number of neighbors for a point).
	 * @param dMaxNeighborDist   maximum distance between points to consider them as neighbors.
	 */
  public void calculateNeighbors(double dMaxNeighborDist)
  {
	    m_dMaxPointDist   = 0;
	    m_iMaxNumNeighbors = 0;
	    m_iMaxNumNeighborPoints = 0;
	    
	    int iDataSize = getInputSize();
	    m_iNumNeighborsArray = new int[iDataSize];
		Arrays.fill(m_iNumNeighborsArray, 0);
		// bruteforce find the number of neighbors less than a certain dist
		for (int i1 = 0; i1 < iDataSize; i1++)
		{
			double p1[] = getInputPoint(i1);
			for (int i2 = i1; i2 < iDataSize; i2++)
			{
				double p2[] = getInputPoint(i2);
				double dDist = getDist(p1, p2, m_Metric);
				if (dDist < dMaxNeighborDist)
				{
					m_iNumNeighborsArray[i1]++;
					m_iNumNeighborsArray[i2]++;
				}
				m_dMaxPointDist = Math.max(m_dMaxPointDist, dDist);
			}
			m_iMaxNumNeighbors = Math.max(m_iMaxNumNeighbors, m_iNumNeighborsArray[i1]);
		}
		
		m_iNumNeighborhoodPointsArray = new int[m_iMaxNumNeighbors + 1];
		Arrays.fill(m_iNumNeighborhoodPointsArray, 0);
		for (int i = 0; i < iDataSize; i++)
		{
			m_iNumNeighborhoodPointsArray[m_iNumNeighborsArray[i]]++;
		}
		//for(int i = 1; i < m_iNumNeighborhoodPointsArray.length; i++)
		//{
		//	m_iNumNeighborhoodPointsArray[i] += m_iNumNeighborhoodPointsArray[i - 1]; // accumulate sum
		//}
		for (int i = 0; i < m_iNumNeighborhoodPointsArray.length; i++)
		{
			m_iMaxNumNeighborPoints = Math.max(m_iMaxNumNeighborPoints, m_iNumNeighborhoodPointsArray[i]);
		}
		
		m_iMinOutlierNeighbors = Math.max(0, Math.min(m_iMinOutlierNeighbors, m_iMaxNumNeighbors));
  }
  
	static final int NEIGHBOR_DIST_STEPS = 300;
	public double m_NeighborhoodValues[][] = new double[NEIGHBOR_DIST_STEPS + 1][NEIGHBOR_DIST_STEPS];
	public double m_NeighborhoodXAxis[] = new double[NEIGHBOR_DIST_STEPS];
	public double m_NeighborhoodYAxis[] = new double[NEIGHBOR_DIST_STEPS + 1];

	public void calculateAllNeighborhoods()
	{
	  	int iDataSize = getInputSize();
	
	  	calculateNeighbors(0);
	  	double dDist = 0;
	
	  	//double xaxis[] = new double[NEIGHBOR_DIST_STEPS];
	  	//double yaxis[] = new double[NEIGHBOR_DIST_STEPS];
	  	int iMaxAllNumNeighborPoints = 0;
	  	for (int i = 0; i < m_NeighborhoodValues.length; i++)
	  	{
	  		Arrays.fill(m_NeighborhoodValues[i], 0);
	  	}
	  	for (int i = 0; i < NEIGHBOR_DIST_STEPS; i++)
	  	{
	  		//Arrays.fill(m_NeighborhoodValues[i], 0);
	  		for (int j = 0; j < m_iNumNeighborhoodPointsArray.length; j++)
	  		{
				int j2 = Math.min(j, NEIGHBOR_DIST_STEPS - 1);
	  			if (iDataSize > NEIGHBOR_DIST_STEPS)
	  			{
	  				j2 = Math.min(j*NEIGHBOR_DIST_STEPS/iDataSize, NEIGHBOR_DIST_STEPS - 1);
	  			}
	  			m_NeighborhoodValues[j2][i] += 1.0 * m_iNumNeighborhoodPointsArray[j]  /Math.max(1.0, m_iMaxNumNeighborPoints);
	  		}
	  		
	  		iMaxAllNumNeighborPoints = Math.max(iMaxAllNumNeighborPoints, m_iMaxNumNeighborPoints);
	  		dDist += m_dMaxPointDist / NEIGHBOR_DIST_STEPS;
	      	calculateNeighbors(dDist);
	  	}
	  	
//	  	for (int i = 0; i < NEIGHBOR_DIST_STEPS; i++)
//	  	{
//	  		for (int j = 0; j < NEIGHBOR_DIST_STEPS; j++)
//	  		{
//	  			m_NeighborhoodValues[i][j] /= iMaxAllNumNeighborPoints;
//	  		}
//	  	}
	}
}