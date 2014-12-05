package still.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

/**
 * 
 * Handles binning numeric dimensions into equipartitions
 * or returns the set of unique values of the dimension.
 * 
 * @author sfingram
 *
 */
public class Binner {

	
	public static int MAX_UNIQUE_VALUES = 100;
	
	boolean use_bins = false;
	public Hashtable<Double,Integer> unique_value_hash = null;
	ArrayList<Double> unique_values = null;
	ArrayList<Double[]> bins = null;
	int bin_count = 10;
	Table inner_table = null;
	int bin_dimension = -1;
	double min_val = -1;
	double max_val = -1;
	double partition_size = -1;
	public NumberFormat nf = null;	
	public NumberFormat sf = new DecimalFormat("0.#E0");
	
	public Binner( Table t, int dimension ) {
		
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);
		
		inner_table = t;
		bin_dimension = dimension;
		bin();
		
	}
	
	/**
	 * Get a list of the unique values of the dimension.  Null if use_bins == true.
	 * 
	 * @return
	 */
	public ArrayList<Double> getUniqueValues () {
		
		if( use_bins ) {
			
			return null;
		}
		
		return unique_values;
	}
	
	/**
	 * Get ranges [a,b) of the partitions.  Null if use_bins == false.
	 * @return
	 */
	public ArrayList<Double[]> getBins() {

		if( !use_bins ) {
			
			return null;
		}
		
		return bins;
	}
	
	public void bin( ) {
		
		// empty table case
		if( inner_table.rows() < 1 ) {
			
			return;
		}
		
		if( use_bins && ( inner_table.getColType(bin_dimension) != Table.ColType.NUMERIC  && 
				inner_table.getColType(bin_dimension) != Table.ColType.ATTRIBUTE) ) {
			
			use_bins = false;
		}
		
		if( use_bins ) {
			
			// get the range of the dimension
			
			min_val = inner_table.getMeasurement(0, bin_dimension);
			max_val = min_val;
			for( int i = 1; i < inner_table.rows(); i++ ) {
			
				min_val = Math.min( min_val, inner_table.getMeasurement(i, bin_dimension));
				max_val = Math.max( max_val, inner_table.getMeasurement(i, bin_dimension));
			}
			
			double range = max_val-min_val;
			partition_size = range / ((double)bin_count);
			
			// construct partition
			bins = new ArrayList<Double[]>();
			for( int i = 0; i < bin_count; i++ ) {
				
				Double[] partition = new Double[2];
				partition[0] = min_val + partition_size * i;
				partition[1] = min_val + partition_size * (i+1);
				bins.add(partition);
			}			
		}
		else {
			
			if ( inner_table.getColType(bin_dimension) != Table.ColType.NUMERIC && 
					inner_table.getColType(bin_dimension) != Table.ColType.ATTRIBUTE ) {
				
				// count the number of unique values
				
				int k = 0;
				this.unique_value_hash = new Hashtable<Double,Integer>(); 
				for( int i = 0; i < inner_table.getCategories(bin_dimension).length; i++ ) {
					
					unique_value_hash.put((double)i, i);
				}
							
				// create unique value list
				unique_values = new ArrayList<Double>();
				for( Double d : unique_value_hash.keySet() ) {
					
					unique_values.add( d );
				}
				
				Collections.sort(unique_values);
			}
			else {
				
				// count the number of unique values
				
				int k = 0;
				this.unique_value_hash = new Hashtable<Double,Integer>(); 
				for( int i = 0; i < inner_table.rows(); i++ ) {
					
					double val = inner_table.getMeasurement(i, bin_dimension);
					if( !unique_value_hash.containsKey( val ) ) {
						
						unique_value_hash.put(val, k);
						k++;
						
						// if greater than threshold, then re-bin
						if( k > MAX_UNIQUE_VALUES ) {
							
							setUseBins( !use_bins );
							return;
						}
					}				
				}
							
				// create unique value list
				unique_values = new ArrayList<Double>();
				for( Double d : unique_value_hash.keySet() ) {
					
					unique_values.add( d );
				}
				Collections.sort(unique_values);
			}
		}
	}
	
	/**
	 * 
	 * Return the partition or unique value number
	 * 
	 * @param val
	 * @return
	 */
	public double binNum( double val ) {
		
		if( use_bins ) {
					
			return Math.min(this.bin_count-1, Math.floor( (val-min_val)/partition_size ));
		}
		
		return unique_values.indexOf(val);//unique_value_hash.get(val);		
	}
	
	/**
	 * Set the number of partitions a range is divided into 
	 * 
	 * @param bin_count
	 */
	public void setBinCount( int bin_count ) {
				
		this.bin_count = Math.max( 1, bin_count );
		bin();
	}
	
	/**
	 * Set the number of partitions a range is divided into 
	 * 
	 * @param bin_count
	 */
	public int getBinCount( ) {

		if( !getUseBins() ) {
		
			return unique_value_hash.size();
		}
		
		return bin_count;
	}
	
	/**
	 * Set whether to bin by partitioning the range of the dimension or 
	 * to use the unique values
	 * 
	 * @param use_bins
	 */
	public void setUseBins( boolean use_bins ) {
		
		this.use_bins = use_bins;
		bin();
	}
	
	public boolean getUseBins( ) {
		
		return use_bins;
	}
	
	public String[] getBinStrings() {
		
		String[] ret = null;
		
		if( getUseBins() ) {
			
			ArrayList<Double[]> bins = getBins();
			ret = new String[bins.size()];
			for( int i = 0; i < bins.size(); i++ ) {
				
				double num1 = bins.get(i)[0].doubleValue();
				double num2 = bins.get(i)[1].doubleValue();
				String numstr1 = null;
				String numstr2 = null;
				if( Math.abs(num1) > 100.) {
					
					numstr1 = sf.format( num1 );
				}
				else {
					
					numstr1 = nf.format( num1 );
				}
				if( Math.abs(num2) > 100.) {
					
					numstr2 = sf.format( num2 );
				}
				else {
					
					numstr2 = nf.format( num2 );
				}
				ret[i] = "(" + numstr1 + "," + numstr2 + ")";
			}
		}
		else {
			
			if( inner_table.getColType( bin_dimension ) == Table.ColType.NUMERIC || 
					inner_table.getColType( bin_dimension ) == Table.ColType.ATTRIBUTE ) {
				
				ArrayList<Double> uvals = getUniqueValues();
				if( inner_table.getColType( bin_dimension ) == Table.ColType.NUMERIC ) 
					Collections.sort(uvals);
				ret = new String[uvals.size()];
				for( int i = 0; i < uvals.size(); i++ ) {
					
					double num = uvals.get(i);
					String numstr = null;
					if( Math.abs(num) > 100.) {
						
						numstr = sf.format( num );
					}
					else {
						
						numstr = nf.format( num );
					}
					ret[i] = "" + numstr;
				}
			}
			else {
				
				return inner_table.getCategories( bin_dimension );
			}
		}
		
		return ret;
	}
	
}
