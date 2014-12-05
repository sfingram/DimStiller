package still.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.mallardsoft.tuple.Pair;

/**
 * 
 * Describes Maps in the space of the DR framework.
 * 
 * Maps are bipartite graphs where the nodes in the first 
 * independent set represent the input dimensions and
 * the nodes in the second set represent output dimensions.
 * the edges in the graph dictate the relationships between
 * the two sets in terms of which input dimensions
 * are used to compute which output dimensions.
 * 
 * Maps are used to produce Groups from Dims.
 * 
 * Internally they are the B matrix in a bipartite graph
 * where the rows of B represent the input dims
 * and the cols of B represent the output dims
 * 
 * @author sfingram
 *
 */
public class Map implements Serializable {

	public boolean[][] map;
	public boolean[] one_to_one;
	
	public static Map generateDiagonalMap( int size ) {
		
		boolean[][] map = new boolean[size][size];
		for( int i = 0; i < size; i++ ) {
			
			map[i][i] = true;
		}
		
		return new Map(map);
	}
	
	/**
	 * 
	 * Generates a one-to-(one or none) mapping
	 * 
	 * @param cullDims
	 * @return
	 */
	public static Map generateCullMap( boolean[] cullDims ) {
		
		// count the number of true entries
		int numTrue = 0;
		for( int i = 0; i < cullDims.length; i++ ) {
		
			if( cullDims[i] ) {
				
				numTrue++;
			}
		}
		
		boolean[][] map = new boolean[cullDims.length][numTrue];
		numTrue = 0;
		for( int i = 0; i < cullDims.length; i++ ) {
			
			if( cullDims[i] ) {
				
				map[i][numTrue] = true;
				numTrue++;
			}
		}
		
		return new Map(map);		
	}
	
	/**
	 * Generates a many to one where the source 
	 * dimensions are grouped into disjoint sets
	 * 
	 * @param groupDims
	 * @return
	 */
	public static Map generateCovarianceMap( int[] groupDims ) {
		
		// count the maximum of group dims
		int biggestGroup = 0;
		for( int i = 0; i < groupDims.length; i++ ) {
		
			biggestGroup = Math.max(biggestGroup, groupDims[i]);
		}
		
		boolean[][] map = new boolean[groupDims.length][biggestGroup];
		for( int i = 0; i < groupDims.length; i++ ) {
			
			if( groupDims[i] > 0) {
				
				map[i][groupDims[i]-1] = true;
			}
		}
		
		return new Map(map);		
	}
	
	/**
	 * Generates a fully connected map
	 * 
	 * @param adims
	 * @param bdims
	 * @return
	 */
	public static Map fullBipartite( int adims, int bdims ) {
		
		boolean[][] map = new boolean[adims][bdims];
		for( int i = 0; i < adims; i++ ) {
			for( int j = 0; j < bdims; j++ ) {
			
				map[i][j] = true;
			}			
		}
		
		return new Map(map);
	}
	
	/**
	 * Generates a fully connected map except for a small list of dims
	 * 
	 * @return
	 */
	public static Map fullBipartiteExcept( 
			ArrayList<Integer> from_full, 
			ArrayList<Integer> to_full, 
			ArrayList<Integer> from_one, 
			ArrayList<Integer> to_one,
			int total_from,
			int total_to) { 
		
		boolean[][] map = new boolean[total_from][total_to];
		for( int i : from_full ) {
			for( int j : to_full ) {
				map[i][j] = true;
			}
		}
		
		Iterator<Integer> one_to_one_t = to_one.iterator();
		Iterator<Integer> one_to_one_f = from_one.iterator();
		while( one_to_one_f.hasNext() ) {
			Integer oto_t = one_to_one_t.next();
			Integer oto_f = one_to_one_f.next();
			map[oto_f][oto_t]=true;			
		}
		
		return new Map(map);
	}
	
	/**
	 * Generates a fully connected map except for a small list of dims
	 * 
	 * @return
	 */
	public static Map fullBipartiteAppend( 
			ArrayList<Integer> numeric, 
			ArrayList<Integer> non_numeric, 
			int total_from,
			int outdims) { 
		
		boolean[][] map = new boolean[total_from][total_from+outdims];
		for( int i = 0; i < total_from; i++ ) {
			
			map[i][i] = true;
		}

		for( int i = total_from; i < total_from+outdims; i++ ) {
			
			for( int j : numeric ) {
				
				map[j][i] = true;
			}
		}
		
		return new Map(map);
	}

	public Map( boolean[][] map ) {
	
		this.map = map;
		this.one_to_one = new boolean[map[0].length];
		for( int j = 0; j < map[0].length; j++ ) {
			int count = 0;
			for( int i = 0; i < map.length; i++ ) {
				
				if( this.map[i][j] ) {
					
					count++;
				}
			}
			if( count == 1 ) {
				
				this.one_to_one[j] = true;
			}
			else {
				
				this.one_to_one[j] = false;
			}
		}
	}
	
	public int rows() {
		return map.length;		
	}
	
	public int columns() {
		return map[0].length;		
	}
	
	/**
	 * Returns the input columns associated with the output column of an operator
	 * 
	 * @param col
	 * @return
	 */
	public ArrayList<Integer> getColumnSamples( int col ) {

		ArrayList<Integer> returnList = new ArrayList<Integer>();
		
		for( int j = 0; j < map.length; j++ ) {
			
			if( map[j][col] ) {
				
				returnList.add( j );
			}
		}
		return returnList;
	}
	
	public ArrayList<Pair<Integer,Integer>> getSubColumnMap(int row) {
		
		ArrayList<Pair<Integer,Integer>> returnList = new ArrayList<Pair<Integer,Integer>>();
		
		for( int i = 0; i < map[row].length; i++ ) {
			if( map[row][i] ) {

				int subcolCount = 0;
				for( int j = 0; j < row; j++ ) {
					if( map[j][i] ) {
						
						subcolCount++;
					}
				}
				returnList.add(Pair.from(i, subcolCount+1));
			}
		}
		
		return returnList;
	}

}
