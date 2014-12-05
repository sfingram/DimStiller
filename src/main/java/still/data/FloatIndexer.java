package still.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FloatIndexer {

	public double val = 0;
	public int idx = 0;
	public boolean is_neg = false;
	
	public FloatIndexer( double val, int idx ) {
		
		this.val = val;
		this.idx = idx;
	}
	
	public static FloatIndexer[] sortEVals( double[] evals ) {
		
		FloatIndexer[] internal = new FloatIndexer[evals.length];
		
		int k  = 0;
		for( double ev : evals ) {
			
			internal[k] = new FloatIndexer( Math.abs(ev), k );
			if( ev < 0.) {
				
				internal[k].is_neg = true;
			}
			k++;
		}
		
		Arrays.sort( 	internal, 
						new Comparator<FloatIndexer>() { 
								public int compare(FloatIndexer o1, FloatIndexer o2) {
									if( o1.val < o2.val ) {
										return 1;
									}
									if( o1.val > o2.val ) {				
										return -1;
									}
									
									return 0;
								}
						}  );
		k = 0;
		FloatIndexer[] ret = new FloatIndexer[internal.length];
		for( FloatIndexer fi : internal ) {
			
			if( fi.is_neg ) {
				
				ret[k] = new FloatIndexer( -fi.val, fi.idx );
			}
			else {
				
				ret[k] = new FloatIndexer( fi.val, fi.idx );
			}
			k++;
		}
		
		return ret;
	}
	
	public static int[] sortFloats( double[] vals, ArrayList<Integer> valids ) {
		
		FloatIndexer[] internal = new FloatIndexer[valids.size()];
		
		int k  = 0;
		for( int i : valids ) {
			
			internal[k] = new FloatIndexer( vals[i], i );
			k++;
		}
		
		Arrays.sort( 	internal, 
						new Comparator<FloatIndexer>() { 
								public int compare(FloatIndexer o1, FloatIndexer o2) {
									if( o1.val < o2.val ) {
										return -1;
									}
									if( o1.val > o2.val ) {				
										return 1;
									}
									
									return 0;
								}
						}  );
		k = 0;
		int[] ret = new int[valids.size()];
		for( FloatIndexer fi : internal ) {
			
			ret[k] = fi.idx;
			k++;
		}
		
		return ret;
	}
	
	public static int[] sortFloats( double[] vals ) {
		
		FloatIndexer[] internal = new FloatIndexer[vals.length];
		int k = 0;
		for( double v : vals ) {
			
			internal[k] = new FloatIndexer( v, k );
			k++;
		}
		
		Arrays.sort( 	internal, 
						new Comparator<FloatIndexer>() { 
								public int compare(FloatIndexer o1, FloatIndexer o2) {
									if( o1.val < o2.val ) {
										return -1;
									}
									if( o1.val > o2.val ) {				
										return 1;
									}
									
									return 0;
								}
						}  );
		k = 0;
		int[] ret = new int[ vals.length];
		for( FloatIndexer fi : internal ) {
			
			ret[k] = fi.idx;
			k++;
		}
		
		return ret;
	}
	
}
