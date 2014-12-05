package still.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;

public class Binner2D {

	public static int MAX_UNIQUE_VALUES = 100;
	
	boolean use_bins_1 = false;
	boolean use_bins_2 = false;
	Hashtable<Double,Integer> unique_value_hash = null;
	ArrayList<Double> unique_values = null;
	ArrayList<Double[]> bins = null;
	public Binner bin1 = null;
	public Binner bin2 = null;
	public Binner bin3 = null;
	int bin_count_1 = 10;
	int bin_count_2 = 10;
	Table inner_table = null;
	int bin_dimension_1 = -1;
	int bin_dimension_2 = -1;	
	int bin_dimension_3 = -1;	
	double min_val = -1;
	double max_val = -1;
	double partition_size = -1;
	public NumberFormat nf = null;	
	public NumberFormat sf = new DecimalFormat("0.#E0");
	
	public Binner2D( Table t, int dimension_1, int dimension_2 ) {
		
		this( t, dimension_1, dimension_2, -1 );		
	}
	
	public Binner2D( Table t, int dimension_1, int dimension_2, int dimension_3 ) {
				
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);
		
		inner_table = t;
		bin_dimension_1 = dimension_1;
		bin_dimension_2 = dimension_2;
		bin_dimension_3 = dimension_3;
		if( bin_dimension_3 >= 0 && t.getColType(bin_dimension_3) != Table.ColType.ATTRIBUTE ) {
			
			bin_dimension_3 = -1;
		}
		
		bin();		
	}
		
	public void bin( ) {
		
		bin1 = new Binner(inner_table, bin_dimension_1);
		bin2 = new Binner(inner_table, bin_dimension_2);
		if( bin_dimension_3 >= 0 ) {
			
			bin3 = new Binner(inner_table, bin_dimension_3);
		}
		else {
			
			bin3 = null;
		}
		
		bin1.setBinCount(bin_count_1);
		bin2.setBinCount(bin_count_2);
		
		bin1.setUseBins(use_bins_1);
		bin2.setUseBins(use_bins_2);
		if( bin3 != null ) {
			
			bin3.setUseBins( false );
		}
		
		use_bins_1 = bin1.getUseBins();
		use_bins_2 = bin2.getUseBins();		
	}
	
	/**
	 * Set the number of partitions a range is divided into 
	 * 
	 * @param bin_count
	 */
	public void setBinCount( int dim_number, int bin_count ) {
				
		if( dim_number == 1 ) {
			
			this.bin_count_1 = Math.max( 1, bin_count );
		}
		else {
			
			this.bin_count_2 = Math.max( 1, bin_count );
		}
		
		bin();
	}
		
	/**
	 * Set whether to bin by partitioning the range of the dimension or 
	 * to use the unique values
	 * 
	 * @param use_bins
	 */
	public void setUseBins( int dim_number, boolean use_bins ) {
		
		if( dim_number == 1 ) {
			
			this.use_bins_1 = use_bins;
		}
		else {
			
			this.use_bins_2 = use_bins;
		}

		bin();
	}
	
	public String[] getBinStrings( int dim_number ) {
		
		if( dim_number == 1 ) {
			
			return bin1.getBinStrings();
		}
		
		return bin2.getBinStrings();
	}
	
	public int getMax2DBin( ) {
		
		int[][][] bin2ds = get2DBins();
		
		int max_val = -1;
		
		for( int i = 0; i < bin2ds.length; i++ ) {
			for( int j = 0; j < bin2ds[0].length; j++ ) {
				for( int k = 0; k < bin2ds[0][0].length; k++ ) {
			
					max_val = Math.max( max_val, bin2ds[i][j][k] );
				}
			}			
		}
		
		return max_val;
	}
	
	public int[][][] get2DBins( ) {
		
		int bin1pos = -1;
		if( bin1.getUseBins() ) {

			bin1pos = bin1.getBinCount();
		}
		else {
			
			bin1pos = bin1.getUniqueValues().size();
		}
		
		int bin2pos = -1;
		if( bin2.getUseBins() ) {

			bin2pos = bin2.getBinCount();
		}
		else {
			
			bin2pos = bin2.getUniqueValues().size();
		}
		
		int bin3pos = -1;
		
		if( bin3 == null ) {
						
			bin3pos = 1;
		}
		else {
			
			if( bin3.getUseBins() ) {

				bin3pos = bin3.getBinCount();
			}
			else {
				
				bin3pos = bin3.getUniqueValues().size();
			}
		}
		
		int[][][] ret = new int[bin1pos][bin2pos][bin3pos];

		for( int i = 0; i < inner_table.rows(); i++ ) {
			
			if( bin3 == null ) {
				
				ret[ (int) bin1.binNum( inner_table.getMeasurement(i, bin_dimension_1) ) ]
				   [ (int) bin2.binNum( inner_table.getMeasurement(i, bin_dimension_2) ) ][0]++;
			}
			else {
				
				ret[ (int) bin1.binNum( inner_table.getMeasurement(i, bin_dimension_1) ) ]
				   [ (int) bin2.binNum( inner_table.getMeasurement(i, bin_dimension_2) ) ]
				   [ (int) bin3.binNum( inner_table.getMeasurement(i, bin_dimension_3) ) ]++;
			}
		}

		return ret;
	}

}
