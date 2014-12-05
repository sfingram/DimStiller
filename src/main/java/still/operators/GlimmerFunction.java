package still.operators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import javax.swing.JFrame;

import org.jblas.DoubleMatrix;

import processing.core.PApplet;
import still.data.Function;
import still.data.Group;
import still.data.Map;
import still.data.Table;

public class GlimmerFunction implements Function, Serializable{

	
	static int V_SET_SIZE = 14;
	static int S_SET_SIZE = 10;			// number of randomly chosen neighbors
	static int  MAX_ITERATION =50000;	// maximum number of iterations
	static int  COSCLEN	= 51;			// length of cosc filter
	static float EPS = 1.e-4f;			// termination threshold
	static int  MIN_SET_SIZE = 1000;	// recursion termination condition
	static int  DEC_FACTOR = 8;			// decimation factor
	static float cosc[] = {0.f,  -0.00020937301404f,      -0.00083238644375f,      -0.00187445134867f,      -0.003352219513758f,     -0.005284158713234f,     -0.007680040381756f,     -0.010530536243981f,     -0.013798126870435f,     -0.017410416484704f,     -0.021256733995966f,     -0.025188599234624f,     -0.029024272810166f,     -0.032557220569071f,     -0.035567944643756f,     -0.037838297355557f,     -0.039167132882787f,     -0.039385989227318f,     -0.038373445436298f,     -0.036066871845685f,     -0.032470479106137f,     -0.027658859359265f,     -0.02177557557417f,      -0.015026761314847f,     -0.007670107630023f,     0.f,      0.007670107630023f,      0.015026761314847f,      0.02177557557417f,       0.027658859359265f,      0.032470479106137f,      0.036066871845685f,      0.038373445436298f,      0.039385989227318f,      0.039167132882787f,      0.037838297355557f,      0.035567944643756f,      0.032557220569071f,      0.029024272810166f,      0.025188599234624f,      0.021256733995966f,      0.017410416484704f,      0.013798126870435f,      0.010530536243981f,      0.007680040381756f,      0.005284158713234f,      0.003352219513758f,      0.00187445134867f,       0.00083238644375f,       0.00020937301404f,       0.f};

	public int cur_embedding_dim=0;
	public JFrame foo = null;
	public TestApplet myapp = null;
	
	public class TestApplet extends PApplet {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1504310272559416173L;
		public GlimmerFunction func = null;
		public float[] buf = null;
		
		public TestApplet(GlimmerFunction func, float[] buf) {
			
			super();
			this.buf = buf;
			this.func = func;
			
		}
		
		public void setup() {
			
			size(	500, 
					500);
	    	 // prevent thread from starving everything else
	         noLoop();
		}
		
		public void draw() {
			
			if( buf != null && cur_embedding_dim == 2) {
			
				background(0);
				
				this.stroke(255);
				this.fill(255);
				
				for( int i = 0; i < this.func.input.rows(); i++ ) {
					
//					System.out.println("i = " + i);
					this.rect(100+buf[i*2]*2000, 100+buf[i*2+1]*2000,3,3);
				}
			}			
		}
		
	}
	
	
	public class IndexType {
		
		public int index;		// index of the other point
		public float highd;	// high dimensional distance
		public float lowd;		// low dimensional distance
	}
	
	public class DistComp implements Comparator<IndexType> {

		@Override
		public int compare(IndexType da, IndexType db) {
			if(da.highd == db.highd)
				return 0;
			return (da.highd - db.highd)<0.f?-1:1;
		}
		
	}
	
	public class IdxComp implements Comparator<IndexType> {

		@Override
		public int compare(IndexType da, IndexType db) {
			return (int)(da.index - db.index);
		}
		
	}
	
	/**
	 * 
	 * @param idx_set - the set of indices
	 * @param size - the size at which we are operating
	 * @param iteration - the current iteration
	 * @param stop_iteration - total number of iterations since changing levels
	 * @param sstress - the current set of sparse stress iterations
	 * @return if the current index set satisfies the sparse stress cuttoff criterion
	 */
	public boolean terminate( 	IndexType[] idx_set, 
								int size, 
								int iteration, 
								int stop_iteration, 
								float[] sstress ) {
		
		float numer = 0.f; // sq diff of dists
		float denom = 0.f; // sq dists
		float temp  = 0.f;

		if( iteration > MAX_ITERATION ) {

			return true;
		}

		// compute sparse stress
		for( int i = 0; i < size; i++ ) {

			for( int j = 0; j < (V_SET_SIZE+S_SET_SIZE); j++ ) {

				temp	= (idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].highd==1000.f)?0.f:(idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].highd - idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].lowd);
//				System.out.print(","+temp);
				numer	+= temp*temp;
				denom	+= (idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].highd==1000.f)?0.f:(idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].highd * idx_set[i*(V_SET_SIZE+S_SET_SIZE) + j].highd);
			}
		}
		sstress[ iteration ] = numer / denom;

//		System.out.println("\n---------\n");
//		if( this.cur_embedding_dim == 2 ) {
//			System.out.println(numer);
//			return false;
//		}
		
		// convolve the signal
		float signal = 0.f;
		if( iteration - stop_iteration > COSCLEN ) {

			for( int i = 0; i < COSCLEN; i++ ) {

				signal += sstress[ (iteration - COSCLEN)+i ] * cosc[ i ];
			}

//			System.out.println("  CUTOFF" + iteration + ","+stop_iteration + " = " + Math.abs(signal));
			
			if( Math.abs( signal ) < EPS ) {

				//stop_iteration = iteration;
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Compute Chalmers' an iteration of force directed simulation 
	 * on subset of size 'ssize' holding fixedsize fixed
	 * @param ssize - the size of the moving point set
	 * @param fixedsize - the size of the fixed point set
	 * @param iteration - the current iteration
	 * @param stop_iteration - total number of iterations since changing levels
	 */
	public void force_directed(	IndexType[] g_idx, 
								int ssize, 
								int fixedsize, 
								int iteration, 
								int stop_iteration, 
								boolean g_interpolating, 
								int n_embedding_dims,
								int n_original_dims,
								float[] g_data,
								float[] g_embed,
								float[] g_vel,
								float[] g_force) {
	
		// initialize index sets
		if( iteration == stop_iteration ) {
	
			for( int i = 0; i < ssize; i++ ) {
	
				for( int j = 0; j < V_SET_SIZE; j++ ) {
	
					g_idx[i*(V_SET_SIZE+S_SET_SIZE) + j ].index = myRandom.nextInt( g_interpolating?fixedsize:ssize);
				}
			}
		}
	
		// perform the force simulation iteration
		float []dir_vec		= new float[n_embedding_dims];
		float []relvel_vec	= new float[n_embedding_dims];
		float diff			= 0.f;
		float norm			= 0.f;
		float lo			= 0.f;
		float hi			= 0.f;
		
		// compute new forces for each point
		for( int i = fixedsize; i < ssize; i++ ) {
	
			for( int j = 0; j < V_SET_SIZE+S_SET_SIZE; j++ ) {
	
				// update the S set with random entries
				if( j >= V_SET_SIZE ) {
					g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index = 
						myRandom.nextInt(g_interpolating?fixedsize:ssize);
				}
	
				// calculate high dimensional distances
				int idx = g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index;
				hi = 0.f;
				for( int k = 0; k < n_original_dims; k++ ) {
			  
					norm = (g_data[idx*n_original_dims+k] - g_data[i*n_original_dims+k]);
					hi += norm*norm;
				}
				g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd=(float)Math.sqrt(hi);
			}
	
			
			// sort index set by index
			Arrays.sort(g_idx, i*(V_SET_SIZE+S_SET_SIZE), (i+1)*(V_SET_SIZE+S_SET_SIZE), new IdxComp());

				
			// mark duplicates (with 1000)
			for( int j = 1; j < V_SET_SIZE+S_SET_SIZE; j++ ) {
	
				if( g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index==g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j-1].index )
					g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd=1000.f;
			}
	
//			System.out.print("Before: ");
//			for( int j = 0; j < V_SET_SIZE+S_SET_SIZE; j++ ) {
//				System.out.print(" (" + g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index + ", " + g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd + ")" );
//			}
//			System.out.println( );
			// sort index set by distance
			Arrays.sort(g_idx, i*(V_SET_SIZE+S_SET_SIZE), (i+1)*(V_SET_SIZE+S_SET_SIZE), new DistComp());
			
//			System.out.print("After: ");
//			for( int j = 0; j < V_SET_SIZE+S_SET_SIZE; j++ ) {
//				System.out.print(" (" + g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index + ", " + g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd + ")" );
//			}
//			System.out.println( );
	
			// move the point
			for( int j = 0; j < (V_SET_SIZE+S_SET_SIZE); j++ ) {
	
				// get a reference to the other point in the index set
				int idx = g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].index;
				norm = 0.f;
				for( int k = 0; k < n_embedding_dims; k++ ) {
	
					// calculate the direction vector
					dir_vec[k] =  g_embed[idx*n_embedding_dims+k] - g_embed[i*n_embedding_dims+k];
					norm += dir_vec[k]*dir_vec[k];
				}
				norm = (float)Math.sqrt( norm );
				g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].lowd = norm;
				if( norm > 1.e-6 && g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd!=1000.f ) {		// check for zero norm or mark
	
					// normalize direction vector
					for( int k = 0; k < n_embedding_dims; k++ ) {
	
						dir_vec[k] /= norm;
					}
	
					// calculate relative velocity
					for( int k = 0; k < n_embedding_dims; k++ ) {
						relvel_vec[k] = g_vel[idx*n_embedding_dims+k] - g_vel[i*n_embedding_dims+k];
					}
	
					// calculate difference between lo and hi distances
					lo = g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].lowd;	
					hi = g_idx[i*(V_SET_SIZE+S_SET_SIZE)+j].highd;
					diff = (lo - hi) * SPRINGFORCE;					
					// compute damping value
					norm = 0.f;
					for( int k = 0; k < n_embedding_dims; k++ ) {
						
						norm += dir_vec[k]*relvel_vec[k];
					}
					diff += norm*DAMPING;
					
					// accumulate the force
					for( int k = 0; k < n_embedding_dims; k++ ) {
						
						g_force[i*n_embedding_dims+k] += dir_vec[k]*diff;
					}
				}
			}
	
			// scale the force by the size factor
			for( int k = 0; k < n_embedding_dims; k++ ) {
				
				g_force[i*n_embedding_dims+k] *= SIZE_FACTOR;
			}
		}
	
//		System.out.println();
		
		// compute new velocities for each point with Euler integration
		for( int i = fixedsize; i < ssize; i++ ) {
	
			for( int k = 0; k < n_embedding_dims; k++ ) {
			
				float foo = g_vel[i*n_embedding_dims+k];
				float bar = foo + g_force[i*n_embedding_dims+k]*DELTATIME;
				float baz = bar * FREENESS;
				g_vel[i*n_embedding_dims+k] = (float)Math.max( Math.min(baz, 2.0 ), -2.0 );
			}
		}
	
		// compute new positions for each point with Euler integration
		for( int i = fixedsize; i < ssize; i++ ) {
			for( int k = 0; k < n_embedding_dims; k++ ) {
			
				g_embed[i*n_embedding_dims+k] += g_vel[i*n_embedding_dims+k]*DELTATIME;
//				System.out.print(" "+g_embed[i*n_embedding_dims+k]);
			}
//			System.out.println();
		}
	
	}
	
	/*
		init embedding to a random initialization in (-1,1) x (-1,1)
	*/
	void init_embedding( float[] embedding, int N, int n_embedding_dims ) {
		
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < n_embedding_dims; j++ ) {
				embedding[i*(n_embedding_dims)+j]=((float)(myRandom.nextInt(10000))/10000.f)-0.5f;
			}
		}
	}
	
	/*
		computes the input level hierarchy and size
	*/
	int fill_level_count( int input, int[] h, int levels ) {
	
		h[levels]=input;
		if( input <= MIN_SET_SIZE )
			return levels + 1;
		return fill_level_count( input / DEC_FACTOR, h, levels + 1 );
	}
	/*
		FORCE CONSTANTS
	*/
	static float SIZE_FACTOR = (1.f / ((float)(V_SET_SIZE+S_SET_SIZE)));
	static float DAMPING = (0.3f);
	static float SPRINGFORCE = (0.7f);
	static float DELTATIME = (0.3f);
	static float FREENESS = (0.85f);
	
	Random myRandom = null;
	private DoubleMatrix newCoords	= null;
	private int numericDims = 0;
	private Table input = null;
	ArrayList<Integer> nonNumericIdxs = null;
	ArrayList<Integer> numericIdxs = null;
	double[] stressValues;
	int[] idx_map = null;
	int outDims = -1;
	boolean isShuffled = true;
	int stress_runs = 1;
	
	public double[] getStressValues() {
		
		return stressValues;
	}
	
	/**
	 * Calculate the sparse stress value for a given embedding dimension
	 * for this operator.
	 * 
	 * @param embedDim - the embedding of which we measure stress
	 * @return
	 */
	public double getStressValue( int embedDim ) {
	
		float f = 0.f;
		for( int i = 0; i < stress_runs; i++ ) {
			f += genEmbedding(embedDim+1);
		}
		return f / (double) stress_runs;
	}

	/**
	 * Produce the stress values to add to the scree plot
	 */
	public void genStressValues() {
		
		stressValues = new double[ numericDims ];
		
		for( int i = 0; i < stressValues.length; i++ ) {
			
			stressValues[i] = getStressValue( i );
		}
	}
	
	/**
	 * read the data from the table structure into another float buffer
	 * 
	 * @param permute
	 * @return
	 */
	public float[] data_extract( boolean permute ) {
		
		float[] g_data = new float[ input.rows() * numericDims ];
		idx_map = new int[ input.rows() ];
		
		for( int i = 0; i < input.rows(); i++ ) {
		
			idx_map[i] = i;
			for( int j = 0; j < numericDims; j++ ) {
				
				g_data[i*numericDims + j] = (float) this.input.getMeasurement(i, numericIdxs.get(j));
			}
		}
		
		// permute using a knuth shuffle
		if( permute ) {
			
			float[] shuffle_temp = new float[ numericDims ];
			int idx_temp = 0;
			int shuffle_idx = 0;
			for( int i = 0; i < input.rows()*numericDims; i+=numericDims ) {

				shuffle_idx = i + ( myRandom.nextInt(input.rows()-(i/numericDims)) )*numericDims;
				idx_temp 	= idx_map[i/numericDims];
				idx_map[i/numericDims] 	= idx_map[shuffle_idx/numericDims];
				idx_map[shuffle_idx/numericDims] = idx_temp;
				for( int j = 0; j < numericDims; j++ ) {	// swap
				
					shuffle_temp[j]			=	g_data[i+j];
					g_data[i+j] 			= 	g_data[shuffle_idx+j];
					g_data[shuffle_idx+j] 	= 	shuffle_temp[j];
				}		
			}
		}
		
		return g_data;
	}
	
	/**
	 * calculate the embedding coordinates for a given embedding dimension
	 */
	public float genEmbedding( int n_embedding_dims) {
		
		cur_embedding_dim = n_embedding_dims;
		boolean g_done = false;			// controls the movement of points
		boolean g_interpolating=false;	// specifies if we are interpolating yet
		int g_current_level=0;			// current level being processed
		int[] g_heir = new int[50];		// handles up to 8^50 points
		int g_levels=0;					// stores the point-counts at the associated levels
		int iteration=0;				// total number of iterations
		int stop_iteration=0;			// total number of iterations since changing levels
		int N = this.input.rows();		// number of points |V|
		int n_original_dims = this.numericDims;		// original dimension h of the data (set in loadCSV)
		float[] g_embed = null;			// pointer to embedding coords
		float[] g_force = null;			// pointer to embedding coords' force vectors
		float[] g_vel = null;			// pointer to embedding coords' velocity vectors
		float[] g_data = null;			// pointer to input data coords
		IndexType[] g_idx = null;		// pointer to INDEXTYPE coords
		//int g_chalmers = 0;				// flag for doing chalmers
		float[] sstress = new float[MAX_ITERATION];	// sparse stress calculation		

		g_data = data_extract( this.isShuffled );
		
		// allocate embedding and associated data structures
		g_levels = fill_level_count( N, g_heir, 0 );
		
		g_current_level = g_levels-1;
		g_embed	= new float[ n_embedding_dims*N ];
		g_vel	= new float[n_embedding_dims*N];
		g_force	= new float[n_embedding_dims*N];
		g_idx	= new IndexType[N*(V_SET_SIZE+S_SET_SIZE)];
		for( int i = 0; i < g_idx.length; i++ ) {
			
			g_idx[i] = new IndexType();
		}
		
		// initialize embedding
		init_embedding( g_embed, N, n_embedding_dims );
		
//		myapp.buf = g_embed;
		
		while( !g_done ) {

			// move the points
			if( g_interpolating )
				force_directed( 	g_idx, 
									g_heir[ g_current_level ], 
									g_heir[ g_current_level+1 ],
									iteration,
									stop_iteration,
									g_interpolating,
									n_embedding_dims,
									n_original_dims,
									g_data,
									g_embed,
									g_vel,
									g_force);
			else
				force_directed( 	g_idx, 
									g_heir[ g_current_level ], 
									0,
									iteration,
									stop_iteration,
									g_interpolating,
									n_embedding_dims,
									n_original_dims,
									g_data,
									g_embed,
									g_vel,
									g_force);

			// check the termination condition
			if( terminate( 	g_idx, 
							g_heir[g_current_level],
							iteration,
							stop_iteration,
							sstress) ) {
		
				stop_iteration = iteration;
				
				if( g_interpolating ) {

					g_interpolating = false;
				}
				else {

					g_current_level--; // move to the next level down
					g_interpolating = true;

					// check if the algorithm is complete (no more levels)
					if( g_current_level < 0 ) {

						g_done = true;
					}
				}
			}

//		    try {
//				SwingUtilities.invokeAndWait(new Runnable() {
//				    public void run() {
//				      // Set the preferred size so that the layout managers can handle it
//						myapp.redraw();
//				    }
//				  });
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InvocationTargetException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			if( iteration % 25 == 0 ) {
				System.out.print("\titeration = " + iteration + ", stop_iteration = " + stop_iteration);
				System.out.println(", sstress = "+ sstress[iteration]);
			}
			iteration++;	// increment the current iteration count			
		}
		
		// if the embedding dimension == out dimension
		// save the coordinates as the 
		if( n_embedding_dims == this.outDims ) {
			
//			double[][] temp_transfer = new double[ input.rows() ][ this.outDims ];
			this.newCoords = DoubleMatrix.zeros( input.rows(), this.outDims );
			for( int i = 0; i < input.rows(); i++ ) {
				
				for( int j = 0; j < this.outDims; j++ ) {
					
					//temp_transfer[idx_map[i]][j] = g_embed[i*this.outDims + j];
					this.newCoords.put(idx_map[i], j, g_embed[i*this.outDims + j]);
				}
			}
		}
		
		return sstress[iteration-1];
	}
	
	public void softReset( Table table, ArrayList<Integer> numericIdxs, ArrayList<Integer> nonNumericIdxs, int dims ) {
		
		this.numericDims = numericIdxs.size();
		this.numericIdxs = numericIdxs;
		this.nonNumericIdxs = nonNumericIdxs;
		this.outDims = dims;
		this.input = table;
		genEmbedding( dims );
	}
	
	public GlimmerFunction( Table table, ArrayList<Integer> numericIdxs, ArrayList<Integer> nonNumericIdxs, int dims ) {

		// Convert data to a zero means matrix
		this.numericDims = numericIdxs.size();
		this.numericIdxs = numericIdxs;
		this.nonNumericIdxs = nonNumericIdxs;
		this.outDims = dims;
		this.input = table;
		this.myRandom = new Random();
//		myapp = new TestApplet( this, null );
//	    SwingUtilities.invokeLater(new Runnable() {
//	    	
//	        public void run() {
//
//	        	JFrame foo = new JFrame("dos heuvos");
//	        	foo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	        	foo.getContentPane().setLayout(new BorderLayout(5,5));
//	        	foo.setSize(500, 500);
//	        	foo.getContentPane().add(myapp,"Center");
//	        	myapp.init();
//	        	foo.setVisible(true);
//	        }
//	      });
		genStressValues();
		
		//genEmbedding( this.outDims );		
	}
	
	@Override
	public Table apply(Group group) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double compute(int row, int col) {
								
		if( col < outDims ) 
			return newCoords.get(row, col);

		return input.getMeasurement(row, nonNumericIdxs.get(col-outDims));
	}

	@Override
	public Group inverse(Table dims) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] invert( Map map, int row, int col, double value ) {
		
		// TODO HELP ME, properly invert, puh lease
		
		double[] ret = new double[1];
		
		ret[0] = value;
		
		return ret;
	}

	@Override
	public int[] outMap() {
		// TODO Auto-generated method stub
		return null;
	}
}
