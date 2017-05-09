package com.kyleharrington.escalation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.jblas.ComplexDoubleMatrix;
import org.jblas.Decompose;
import org.jblas.Decompose.LUDecomposition;
import org.jblas.DoubleMatrix;
import org.jblas.Eigen;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.couchbase.client.CouchbaseClient;
//import com.lowagie.text.Rectangle;
//import com.lowagie.text.pdf.DefaultFontMapper;
//import com.lowagie.text.pdf.PdfContentByte;
//import com.lowagie.text.pdf.PdfTemplate;
//import com.lowagie.text.pdf.PdfWriter;

//public class PopulationKnockout implements Serializable {
public class PopulationKnockout extends Population {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8141641565753763996L;

	public String UID = "";//String.valueOf( System.nanoTime() );
	
	public Random RNG; 
	//public long randomSeed = 0;
	public long runID = System.nanoTime();
	public long randomSeed = runID;
	public String workingDirectory = "./";	
	public String sshTarget = "";
	
	//public String logFilename = workingDirectory + "escalation.log";
	//public String statFilename = workingDirectory + "escalation.csv";
	
	//public boolean dbActive = true;
	public boolean dbActive = false;
	public String dbHost = "marvin.cs.brandeis.edu";
	List<URI> dbHosts;
	public String dbBucket = "escalation";
	public String dbPass = "";
	transient CouchbaseClient dbClient;
	
	public boolean compressResults = true;
	//public String directoryName = Long.toString( runID ) + "_" + ManagementFactory.getRuntimeMXBean().getName();
	public String directoryName;// = Long.toString( runID ) + "_" + ManagementFactory.getRuntimeMXBean().getName();
	public String logFilename = "escalation.log";
	public String statFilename = "escalation.csv";
	
	//public String logFilename = workingDirectory + runID + "_escalation.log";
	//public String statFilename = workingDirectory + runID + "_escalation.csv";
	
	public double extinctionThreshold;
	public int maxMemory = 10;
	public double DT = 0.05;
	public double clearInterval = 200;
	//public double clearInterval = 100;
	//public double clearInterval = 1000;
	public double lastClear = 0;
	public boolean autoGitCommit = false;
	//public int populationSize = 2000;
	public boolean saveNextPopulation = true;
	public boolean neutralFitness = false;
	public boolean continuousMutation = true;// if Moran, flip mutation coin for every child produced
	
	static public int replicatorContinuousTime = 0;
	static public int replicatorDiscreteTime = 1;
	static public int replicatorGillespie= 2;
	static public int replicatorWright= 3;
	static public int replicatorMoran= 4;
	static public int replicatorEigen = 5;
	//public int replicatorMethod = replicatorEigen;
	//public int replicatorMethod = replicatorContinuousTime;
	//public int replicatorMethod = replicatorDiscreteTime;
	//public int replicatorMethod = replicatorWright;
	public int replicatorMethod = replicatorMoran;
		 
	public ArrayList<String> speciesEvents = new ArrayList<String>();
	
	/*public double muExtend = 0.00005;
	public double muContract = 0.00005;
	public double muUniform = 0.00005;
	public double muPoint = 0.0001;*/
	
	/*public double muExtend = 0.000025;
	public double muContract = 0.000025;
	public double muUniform = 0.000025;
	public double muPoint = 0.00005;*/
	
	// Standard Lindgren
	/*public double muExtend = 0.000005;
	public double muContract = 0.000005;
	public double muUniform = 0.0000;
	public double muPoint = 0.00001;
	public double populationSize = 1000;
	public double extinctionThreshold = 1.0 / 1000.0;*/
	
	// Good rates for non-continuous mutation	
	/*public double muExtend = 0.05;
	public double muContract = 0.05;
	public double muUniform = 0.0;
	public double muPoint = 0.01;
	public double muSecondaryUniform = 0.01;// probably of flipping a bit after all variation is performed, 0 if never
	public double populationSize = 1000;*/
	
	public double muExtend =   0.000001;
	public double muContract = 0.000001;
	public double muUniform = 0.0;
	public double muPoint = 0.0005;
	public double muSecondaryUniform = 0.001;// probably of flipping a bit after all variation is performed, 0 if never
	public double populationSize = 200000;
	
	/*public double muExtend = 0.25;
	public double muContract = 0.25;
	public double muPoint = 0.0001;*/
	
	//public double extinctionThreshold = 1.0 / 10000.0;
	//public double extinctionThreshold = 1.0 / 2000.0;
	
	public int numSpecies;
	public int numExtinct;
	public int numMutants;
	public long t = 0;
	
	public ArrayList<Individual> genotype;	
	
	public double[] scores1 = { 1.0, 5.0, 0.0, 3.0 };// Scores for player1
	public double[] scores2 = { 1.0, 0.0, 5.0, 3.0 };// Scores for player 2
	public float q = 0.01f;// Noise, should be [0,1]
	
	public HashMap<BigInteger[],Double[]> pairwiseResults;
	public DoubleMatrix pCC, pCD, pDC, pDD;
	
	public DoubleMatrix payoff;
	//public DoubleMatrix1D population_complexity;		
	
	// Plotting
	boolean enablePlotting = true;
	boolean enableDisplay = true;
	boolean enableTransmission = false;
	
	transient CombinedDomainXYPlot plot;
transient XYSeriesCollection xyColl = new XYSeriesCollection();
	
	transient XYSeries stratFrequency = new XYSeries( "Strategy Frequency" );
	transient XYSeries[] memLenStratFrequency;
	transient XYSeries numStrat = new XYSeries( "Number of Strategies" );
	//XYSeries memLength = new XYSeries( "Memory Length" );
	transient XYSeries[] memLength;// = new XYSeries( "Memory Length" );
	transient XYSeries xyPDD = new XYSeries( "pDD" );
	transient XYSeries xyPDC = new XYSeries( "pDC" );
	transient XYSeries xyPCD = new XYSeries( "pCD" );
	transient XYSeries xyPCC = new XYSeries( "pCC" );
	
	transient JFreeChart chart;
	transient Plotter demo;
	
	transient NumberAxis timeAxis;

	private int knockoutSpecies;
	
	//Population( ) { }
	
	public static Random loadRNG( String rngFilename ) {
		Random rng;
		try
		{
			FileInputStream fileIn = new FileInputStream( rngFilename );
			ObjectInputStream in = new ObjectInputStream(fileIn);
			rng = (Random) in.readObject();
						


			in.close();
			fileIn.close();
			
		}catch(IOException i)
		{
			i.printStackTrace();
			return null;
		}catch(ClassNotFoundException c)
		{
			System.out.println("Random class not found");
			c.printStackTrace();
			return null;
		} 
		return rng;
	}
	
	public static Population loadPopulation( String popFilename, boolean dbActiveResume ) {
		Population pop;
		try
		{
			FileInputStream fileIn = new FileInputStream( popFilename );
			ObjectInputStream in = new ObjectInputStream(fileIn);
			pop = (Population) in.readObject();
						
			if( dbActiveResume ) {
				pop.dbHosts = Arrays.asList(
						new URI("http://" + pop.dbHost + ":8091/pools")
						);
			
				pop.dbClient = new CouchbaseClient(pop.dbHosts, pop.dbBucket, pop.dbPass);
			}
			
			in.close();
			fileIn.close();
			
		}catch(IOException i)
		{
			i.printStackTrace();
			return null;
		}catch(ClassNotFoundException c)
		{
			System.out.println("Population class not found");
			c.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return pop;
	}
	
	/* Construct a population from a serialization file*/
	public static PopulationKnockout loadPopulation( String filename ) {
		PopulationKnockout pop;
		try
		{
			FileInputStream fileIn = new FileInputStream( filename );
			ObjectInputStream in = new ObjectInputStream(fileIn);
			pop = (PopulationKnockout) in.readObject();
			
			if( pop.dbActive ) {
				pop.dbHosts = Arrays.asList(
						new URI("http://" + pop.dbHost + ":8091/pools")
						);
			
				pop.dbClient = new CouchbaseClient(pop.dbHosts, pop.dbBucket, pop.dbPass);
			}
			
			in.close();
			fileIn.close();
		}catch(IOException i)
		{
			i.printStackTrace();
			return null;
		}catch(ClassNotFoundException c)
		{
			System.out.println("Population class not found");
			c.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return pop;
	}
	
	public void savePopulation( String filename ) {
		try {
			FileOutputStream fileOut = new FileOutputStream( filename );
			ObjectOutputStream out = new ObjectOutputStream( fileOut );
			//writeObject( out );
			out.writeObject( this );
			out.close();
			fileOut.close();
			
			speciesEvents.clear();// otherwise this gets too big
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Always treat de-serialization as a full-blown constructor, by
	 * validating the final state of the de-serialized object.
	 */
	private void readObject(
			ObjectInputStream aInputStream
			) throws ClassNotFoundException, IOException {
		//always perform the default de-serialization first
		aInputStream.defaultReadObject();

		//make defensive copy of the mutable Date field
		//fDateOpened = new Date(fDateOpened.getTime());

		//ensure that object state has not been corrupted or tampered with maliciously
		//validateState();
	}

	/**
	 * This is the default implementation of writeObject.
	 * Customise if necessary.
	 */
	private void writeObject(
			ObjectOutputStream aOutputStream
			) throws IOException {
		//perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject( );
	}
	
	public void init_stats() {
		memLength = new XYSeries[maxMemory - 1];
		for( int k = 1; k < maxMemory; k++ ) {
			memLength[k-1] = new XYSeries( "Memory" + k );
		}
		
		memLenStratFrequency = new XYSeries[maxMemory - 1];
		for( int k = 1; k < maxMemory; k++ ) {
			memLenStratFrequency[k-1] = new XYSeries( "Memory" + k );
		}
		
		xyColl = new XYSeriesCollection();
		
		stratFrequency = new XYSeries( "Strategy Frequency" );
		numStrat = new XYSeries( "Number of Strategies" );

		xyPDD = new XYSeries( "pDD" );
		xyPDC = new XYSeries( "pDC" );
		xyPCD = new XYSeries( "pCD" );
		xyPCC = new XYSeries( "pCC" );
	}
	
	
	public void init() throws Exception {
		
		extinctionThreshold = 1.0 / (double) populationSize;
		
		if( replicatorMethod == replicatorDiscreteTime ) DT = 1;
		else if( replicatorMethod == replicatorWright) DT = 1;
		else if( replicatorMethod == replicatorMoran) DT = 1;
		//else if( replicatorMethod == replicatorMoran) DT = extinctionThreshold;
		
		if( dbActive ) {
			dbHosts = Arrays.asList(
					new URI("http://" + dbHost + ":8091/pools")
					);
		
			dbClient = new CouchbaseClient(dbHosts, dbBucket, dbPass);
		}
		
		//directoryName = Long.toString( randomSeed ); 
		//directoryName = Long.toString( runID ) + "_" + ManagementFactory.getRuntimeMXBean().getName();
				
		//directoryName = UID;
		directoryName = workingDirectory;
		
		File outDir = new File( directoryName );
		if (!outDir.exists()) outDir.mkdir();  
		
		/*try{
			FileWriter fw = new FileWriter( directoryName + "/config", false );
			fw.write( "runID = " + runID + "\n" );
			fw.write( "randomSeed = " + randomSeed  + "\n" );
			fw.write( "maxMemory = " + maxMemory  + "\n" );
			fw.write( "DT = " + DT  + "\n" );
			fw.write( "clearInterval = " + clearInterval  + "\n" );
			fw.write( "gitCommit = " + autoGitCommit  + "\n" );
			fw.write( "replicatorMethod = " + replicatorMethod  + "\n" );
			fw.write( "muExtend = " + muExtend  + "\n" );
			fw.write( "muContract = " + muContract  + "\n" );
			fw.write( "muUniform = " + muUniform  + "\n" );
			fw.write( "muPoint = " + muPoint  + "\n" );
			fw.write( "extinctionThreshold = " + extinctionThreshold  + "\n" );
			fw.write( "populationSize = " + populationSize + "\n" );
			fw.write( "scores1 = {" );
			for( int k = 0; k < scores1.length; k++ ) fw.write( "\t" + scores1[k] + "," );
			fw.write( "}\n" );
			fw.write( "scores2 = {" );
			for( int k = 0; k < scores2.length; k++ ) fw.write( "\t" + scores2[k] + "," );
			fw.write( "}\n" );			
		    fw.close();
		    if( autoGitCommit )
		    	gitCommit( directoryName + "/config" );
		} catch(Exception e) {
			
		}*/
		
		genotype = new ArrayList<Individual>();
		pairwiseResults = new HashMap<BigInteger[],Double[]>();
		//initUniformPrimitive();
		
		init_stats();
		
		RNG = new Random( randomSeed );
		
		try{
		    /*FileWriter fw = new FileWriter( logFilename, false );
		    fw.close();*/
		    FileWriter fw = new FileWriter( directoryName + "/" + statFilename, false );
		    fw.close();
		} catch(Exception e){
			System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");		    
		}
	}
	
	public void resume() {	
		
		genotype = new ArrayList<Individual>();
		pairwiseResults = new HashMap<BigInteger[],Double[]>();
		//initUniformPrimitive();
		
		memLength = new XYSeries[maxMemory - 1];
		for( int k = 1; k < maxMemory; k++ ) {
			memLength[k-1] = new XYSeries( "Memory" + k );
		}
		
		memLenStratFrequency = new XYSeries[maxMemory - 1];
		for( int k = 1; k < maxMemory; k++ ) {
			memLenStratFrequency[k-1] = new XYSeries( "Memory" + k );
		}
		
		RNG = new Random( randomSeed );
		
		/*try{
		    FileWriter fw = new FileWriter( directoryName + "/" + statFilename, false );
		    fw.close();
		} catch(Exception e){
			System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");		    
		}*/
	}
	
	// Compute payoff (seems to be incorrect)
	/*public DoubleMatrix BinaryMask( int gameMemo, Individual p1, Individual p2 ) {
	    long nStates = (long)Math.pow( 2, gameMemo );
	    DoubleMatrix transf = new DoubleMatrix( (int)nStates, (int)nStates );//zeros( nStates, nStates );

	    BigInteger mask = BigInteger.valueOf( nStates - 1 );  

	    BigInteger evnOnes = new BigInteger( new String(new char[gameMemo]).replace( "\0", "01" ) );
	    BigInteger oddOnes = new BigInteger( new String(new char[gameMemo]).replace( "\0", "10" ) );
	    	    
	    //System.out.println( "BinaryMask: " + transf.columns() + "\t" + transf.rows() + "\n" );
	    
	    for( int state = 0; state < (nStates); state++ ) {
	    	BigInteger xState = BigInteger.valueOf( state ).and( evnOnes ).shiftLeft( 1 ).or( 
	    							BigInteger.valueOf( state ).and( oddOnes ).shiftRight( 1 ) );
	    	
	    	BigInteger bigstate = BigInteger.valueOf( state );
	    	
	    	int act1 = 0;
	    	if( p1.strategy.testBit( bigstate.and( BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength ) - 1 ) ).intValue() ) ) act1 = 1;
	    	int act2 = 0;
	    	if( p2.strategy.testBit( xState.and( BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength ) - 1 ) ).intValue() ) ) act2 = 1;

	        BigInteger newState = BigInteger.valueOf( state ).shiftLeft( 2 ).and( mask ).or( BigInteger.valueOf( 2 * act1 + act2 ) );

	        transf.put( state, newState.intValue(), ( (1 - q) * (1 - q) ) );
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 1 ) ) ).intValue(), (1 - q) * q );    // maybe the nexts
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 2 ) ) ).intValue(), (1 - q) * q );            
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 3 ) ) ).intValue(), ( q * q ) );
	    }
	    
	    return transf;
	}*/
	
	public DoubleMatrix BinaryMask( int gameMemo, Individual p1, Individual p2 ) {
	    long nStates = (long)Math.pow( 2, gameMemo );
	    DoubleMatrix transf = new DoubleMatrix( (int)nStates, (int)nStates );//zeros( nStates, nStates );

	    BigInteger mask = BigInteger.valueOf( nStates - 1 );  

	    BigInteger evnOnes = new BigInteger( new String(new char[gameMemo]).replace( "\0", "01" ), 2 );	    
	    BigInteger oddOnes = new BigInteger( new String(new char[gameMemo]).replace( "\0", "10" ), 2 );
	    	    
	    //System.out.println( "BinaryMask: " + transf.columns() + "\t" + transf.rows() + "\n" );
	    String p1s = p1.strategyString();
	    String p2s = p2.strategyString();
	    
	    for( int state = 0; state < (nStates); state++ ) {	    	
	    	BigInteger xState = BigInteger.valueOf( state ).and( evnOnes ).shiftLeft( 1 ).or( 
	    			BigInteger.valueOf( state ).and( oddOnes ).shiftRight( 1 ) );
	    	
	    	BigInteger bigstate = BigInteger.valueOf( state );
	    	
	    	int act1 = 0;
	    	if( p1s.charAt( bigstate.and( BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength ) - 1 ) ).intValue() ) == '1' ) act1 = 1;
	    	//if( p1.strategy.testBit( bigstate.and( BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength ) - 1 ) ).intValue() ) ) act1 = 1;	    	
	    	//if( p1.strategy.testBit( bigstate.and( BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength + 1 ) - 1 ) ).intValue() ) ) act1 = 1; 	    			
	    	int act2 = 0;
	    	if( p2s.charAt( xState.and( BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength ) - 1 ) ).intValue() ) == '1' ) act2 = 1; 
	    	//if( p2.strategy.testBit( xState.and( BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength ) - 1 ) ).intValue() ) ) act2 = 1; 
	    	//if( p2.strategy.testBit( xState.and( BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength + 1 ) - 1 ) ).intValue() ) ) act2 = 1; 

	        BigInteger newState = BigInteger.valueOf( state ).shiftLeft( 2 ).and( mask ).or( BigInteger.valueOf( 2 * act1 + act2 ) );

	        transf.put( state, newState.intValue(), ( (1 - q) * (1 - q) ) );
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 1 ) ) ).intValue(), (1 - q) * q );    // maybe the nexts
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 2 ) ) ).intValue(), (1 - q) * q );            
	        transf.put( state, ( newState.xor( BigInteger.valueOf( 3 ) ) ).intValue(), ( q * q ) );
	        
	        /*System.out.println( "\t" + state + "\t" + xState + "\t" + act1 + "\t" + act2 + "\t" + newState +
	        		"\t" + bigstate.and( BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength ) - 1 ) ) +
	        		"\t" + xState.and( BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength ) - 1 ) ) +
	        		"\t" + bigstate + "\t" + BigInteger.valueOf( (long)Math.pow( 2, p1.memoryLength ) - 1 ) +
	        		"\t" + xState + "\t" +  BigInteger.valueOf( (long)Math.pow( 2, p2.memoryLength ) - 1 ) +
	        		"\t" + BigInteger.valueOf( state ).and( evnOnes ).shiftLeft( 1 ) +
	        		"\t" + BigInteger.valueOf( state ).and( oddOnes ).shiftRight( 1 ) +
	        		"\t" + BigInteger.valueOf( state ).and( evnOnes ) +
	        		"\t" + BigInteger.valueOf( state ).and( oddOnes ) );*/
	    }
	    //System.out.println( evnOnes.toString(2));
	    //System.out.println( oddOnes.toString(2) );
	    //System.out.println( p1.memoryLength + "\t" + p2.memoryLength );
	    //System.out.println( p1.strategy + " " + p2.strategy );
	    //System.out.println( p1.strategyString() + " " + p2.strategyString() );
	    
	    return transf;
	}
	
	public static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	public double[] playGame( Individual p1, Individual p2 ) throws Exception {
		double[] roundDist = new double[4];;
		
		boolean dbFetched = false;
		
		// if we use a DB, then first check if we have the answer already		
		if( dbActive ) {
			//roundDist = (double[]) dbClient.get( p1.strategy + "," + p2.strategy );
			//roundDist = (double[]) dbClient.get( p1.strategyKey() + "," + p2.strategyKey() );
			roundDist = (double[]) dbClient.get( sha256( p1.strategyKey() + "," + p2.strategyKey() ) );
			if ( roundDist != null ) dbFetched = true;
		} 
		
		if( !dbFetched ) {
			roundDist = new double[4];
			
		    int gameMemo = Math.max( p1.memoryLength, p2.memoryLength );
		    gameMemo = gameMemo + gameMemo % 2;
	
		    DoubleMatrix binmask = BinaryMask( gameMemo, p1, p2 );
		    
		    DoubleMatrix transf = binmask.transpose();
		    
		    // Flip columns
		    for( int k = 0; k < ( transf.columns / 2 ); k++ ) {
		    	transf.swapColumns( k, transf.columns - k - 1 );
		    }
		    
		    ComplexDoubleMatrix[] eigenvalvec = Eigen.eigenvectors( transf );
		    
		    ComplexDoubleMatrix evec = eigenvalvec[0];
		    ComplexDoubleMatrix diag = eigenvalvec[1];
		    
		    ComplexDoubleMatrix eval = diag.diag();
		    
		    int maxidx = 0;
		    for( int k = 0; k < eval.length; k++ ) 
		    	if( eval.get(k).abs() > eval.get(maxidx).abs() ) maxidx = k;
		    
		    DoubleMatrix maxEigVec = evec.getColumn( maxidx ).getReal();
		    for( int k = 0; k < maxEigVec.length; k++ )
		    	maxEigVec.put( k, Math.abs( maxEigVec.get(k) ) );	    
			
			DoubleMatrix vec1 = maxEigVec.div( maxEigVec.sum() ); 
				    
		    for( int k = 0; k < ( vec1.length / 4 ); k++ ) {
		    	roundDist[0] += vec1.get( 4 * k );
		    	roundDist[1] += vec1.get( 4 * k + 1 );
		    	roundDist[2] += vec1.get( 4 * k + 2 );
		    	roundDist[3] += vec1.get( 4 * k + 3 );
		    }
		}
	    
	    //System.out.println( "{ " + p1.strategy + "\t" + p2.strategy + "\t[" + roundDist[0] + "\t" + roundDist[1] + "\t" + roundDist[2] + "\t" + roundDist[3] + "]" );
	    
	    // if we use a DB, then transmit our answer
	    if( dbActive ) {
	    	//dbClient.set( p1.strategy + "," + p2.strategy, roundDist).get();
	    	dbClient.set( sha256( p1.strategyKey() + "," + p2.strategyKey() ), roundDist).get();
	    }
	    
	    return roundDist;//normalize( roundDist );
	}
	
	public double[] normalize( double[] v ) {
		double sum = 0;
		for( int k = 0; k < v.length; k++ ) sum += v[k];
		for( int k = 0; k < v.length; k++ ) v[k] /= sum;
		return v;
	}
	
	public boolean equals( Individual a, Individual b ) {
		return ( a.strategyString() == b.strategyString() );
	}
	
	public void addGenotype( Individual ng ) throws Exception {		
		
		//System.out.println( "ADD\t" + ng.strategy + "\t" + ng.strategyString() );
		
		// First check to see if we already represent this strategy type
	    int idx = -1;
	    for( int k = 0; k < genotype.size(); k++ ) {
	    	//System.out.println( ng.strategy + "\t" + genotype.get(k).strategy );
	        if( genotype.get(k).strategy.equals( ng.strategy ) ) {
	            idx = k;
	            break;
	        }
	    }
	    
	    if( idx == -1 ) {
	    	
	    	// Expand matrices
	    	DoubleMatrix newPayoff = new DoubleMatrix( payoff.rows + 1, payoff.columns + 1 );
	    	DoubleMatrix newpCC = new DoubleMatrix( payoff.rows + 1, payoff.columns + 1 );
	    	DoubleMatrix newpCD = new DoubleMatrix( payoff.rows + 1, payoff.columns + 1 );
	    	DoubleMatrix newpDC = new DoubleMatrix( payoff.rows + 1, payoff.columns + 1 );
	    	DoubleMatrix newpDD = new DoubleMatrix( payoff.rows + 1, payoff.columns + 1 );
	    	
	    	// Copy old data
	    	for( int r = 0; r < payoff.rows; r++ ) {
	    		for( int c = 0; c < payoff.columns; c++ ) {
	    			newPayoff.put( r, c, payoff.get( r, c ) );
	    			newpCC.put( r, c, pCC.get( r, c ) );
	    			newpCD.put( r, c, pCD.get( r, c ) );
	    			newpDC.put( r, c, pDC.get( r, c ) );
	    			newpDD.put( r, c, pDD.get( r, c ) );
	    		}
	    	}
	    	
	    	genotype.add( ng );

	        //disp( [ 'Adding genotype: ' num2str( ng(1:end-1) ) ] );
	        
	        int r = numSpecies;
            for( int c = 0; c < numSpecies; c++ ) {
            	double[] roundPairs = playGame( genotype.get( r ), genotype.get( c ) );
            	Double[] rps = new Double[4];
            	for( int k = 0; k < 4; k++ ) rps[k] = roundPairs[k];
            	
            	/*pairwiseResults.put( new BigInteger[] {genotype.get( r ).strategy, genotype.get( c ).strategy}, 
            						 rps );*/
            	
            	double p1score = 0;
            	double p2score = 0;
            	for( int k = 0; k < roundPairs.length; k++ ) {
            		p1score += roundPairs[k] * scores1[k];
            		p2score += roundPairs[k] * scores2[k];
            	}
            	//System.out.println( "P1: " + p1score + " P2: " + p2score );
            	
            	/*newPayoff.set( populationSize, c, p1score - p2score );
            	newPayoff.set( c, populationSize, p2score - p1score );*/
            	
            	if( c == numSpecies ) {
            		newPayoff.put( numSpecies, c, p1score - p2score );
            	} else {            	
            		newPayoff.put( numSpecies, c, p1score );
            		newPayoff.put( c, numSpecies, p2score );
            	}
            	
            	newpDD.put( numSpecies, c, roundPairs[0] );
            	newpDC.put( numSpecies, c, roundPairs[1] );
            	newpCD.put( numSpecies, c, roundPairs[2] );
            	newpCC.put( numSpecies, c, roundPairs[3] );
            	
            	newpDD.put( c, numSpecies, roundPairs[0] );
            	//newpDC.put( c, numSpecies, roundPairs[1] );
            	//newpCD.put( c, numSpecies, roundPairs[2] );
            	newpCD.put( c, numSpecies, roundPairs[1] );
            	newpDC.put( c, numSpecies, roundPairs[2] );
            	newpCC.put( c, numSpecies, roundPairs[3] );
            	
                //round_results(r,c,:) = round_pairs;
            	// Save scores
            }
            speciesEvents.add( "add\t" + ng.strategyString() );
            numSpecies++;
            payoff = newPayoff;
            pCC = newpCC;
            pCD = newpCD;
            pDC = newpDC;
            pDD = newpDD;
	    } else {// If we already have it, then increase the frequency
	    	genotype.get(idx).frequency += ng.frequency;
	    }
	    
	    if( neutralFitness ) {
	    	payoff = DoubleMatrix.ones( payoff.columns, payoff.rows ).mul( 1 / numSpecies );
	    }
	}
	
	public void computeFitness() throws Exception {
		payoff = new DoubleMatrix( numSpecies, numSpecies );
	    payoff.fill( 0 );
	    
	    pCC = new DoubleMatrix( numSpecies, numSpecies );
	    pCD = new DoubleMatrix( numSpecies, numSpecies );
	    pDC = new DoubleMatrix( numSpecies, numSpecies );
	    pDD = new DoubleMatrix( numSpecies, numSpecies );

	    //DoubleMatrix1D roundPairs = new DenseDoubleMatrix1D(4);
	    double[] roundPairs = new double[4];
	    
	    // Compute payoff
	    for( int r = 0; r < numSpecies; r++ ) {

	        for( int c = r; c < numSpecies; c++ ) {

	        	// Compute every time	        	
	            roundPairs = playGame( genotype.get(r), genotype.get(c) );	            	         
            	
            	double p1score = 0;
            	double p2score = 0;
            	
            	//System.out.print( "Playing " + genotype.get(r).strategyString() + " v." + genotype.get(c).strategyString() );
            	for( int k = 0; k < roundPairs.length; k++ ) {
            		p1score += roundPairs[k] * scores1[k];
            		p2score += roundPairs[k] * scores2[k];
            		//System.out.print( "\t\t" + roundPairs[k] + ":" + scores1[k] );
            	}
            	//System.out.print( "\n" );
            	
            	payoff.put( r, c, p1score );
            	payoff.put( c, r, p2score );	            

            	pCC.put( r, c, roundPairs[3] );
            	pCD.put( r, c, roundPairs[2] );
            	pDC.put( r, c, roundPairs[1] );
            	pDD.put( r, c, roundPairs[0] );
			}
		}   
	}	
	
	public void eigenReplicator() {
		//while p.dt * t < p.max_t
		
		    /*if compute_payoff
		        computeFitness();
		        compute_payoff = 0;
		    end*/
			
			double avScore = 0;
			double rsum;
			DoubleMatrix df = new DoubleMatrix( numSpecies );
			DoubleMatrix freqs = new DoubleMatrix( numSpecies );
			for( int col = 0; col < df.length; col++ ) {
				rsum = 0;
				for( int row = 0; row < df.length; row++ ) {
					rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
				}
				df.put( col, rsum );
				avScore += rsum * genotype.get( col ).frequency;
				freqs.put( col , genotype.get(col).frequency );
			}		
			
			 
			DoubleMatrix systemState = payoff.mul( freqs.mmul( freqs.transpose() ) );
			//DoubleMatrix systemState = payoff;//Also consider payoff only. This might have positive feedback
			
			ComplexDoubleMatrix[] eigenvalvec = Eigen.eigenvectors( systemState );
		    
		    ComplexDoubleMatrix evec = eigenvalvec[0];
		    ComplexDoubleMatrix diag = eigenvalvec[1];
		    
		    ComplexDoubleMatrix eval = diag.diag();
		    
		    int maxidx = 0;
		    for( int k = 0; k < eval.length; k++ ) 
		    	if( eval.get(k).abs() > eval.get(maxidx).abs() ) maxidx = k;
		    
		    DoubleMatrix maxEigVec = evec.getColumn( maxidx ).getReal();
		    for( int k = 0; k < maxEigVec.length; k++ )
		    	maxEigVec.put( k, Math.abs( maxEigVec.get(k) ) );	    
			
			DoubleMatrix vec1 = maxEigVec.div( maxEigVec.sum() );// maybe unnecessary 			
		
			double dfreq;
			for( int k = 0; k < numSpecies; k++ ) {
				dfreq = DT * vec1.get( k );
				genotype.get( k ).frequency += dfreq;
				//System.out.println( "Strategy: " + genotype.get(k).strategy + " freq: " + genotype.get(k).frequency + " dfreq: " + dfreq + " [" + genotype.get( k ).frequency + "," + df.get( k ) + "," + avScore + "]" ); 
			}		
			
			normalizePopulation();
					
		    // Update population counts
		    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
		    		
		}
	
	public void replicatorContinuousTime() {
		//while p.dt * t < p.max_t
		
		    /*if compute_payoff
		        computeFitness();
		        compute_payoff = 0;
		    end*/
			
			for( int timestep = 0; timestep < 1.0 / DT; timestep++ ) {
							
				double avScore = 0;
				double rsum;
				DoubleMatrix df = new DoubleMatrix( numSpecies );
				for( int col = 0; col < df.length; col++ ) {
					rsum = 0;
					for( int row = 0; row < df.length; row++ ) {
						rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
					}
					df.put( col, rsum );
					avScore += rsum * genotype.get( col ).frequency;
				}		
			
				double dfreq;
				for( int k = 0; k < numSpecies; k++ ) {
					dfreq = DT * genotype.get( k ).frequency * ( df.get( k ) - avScore );		
					genotype.get( k ).frequency += dfreq;
					//System.out.println( "Strategy: " + genotype.get(k).strategy + " freq: " + genotype.get(k).frequency + " dfreq: " + dfreq + " [" + genotype.get( k ).frequency + "," + df.get( k ) + "," + avScore + "]" ); 
				}		
				
				normalizePopulation();
			}
					
		    // Update population counts
		    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
		    		
		}
	
		public void replicatorDiscreteTime() {
		//while p.dt * t < p.max_t
		
		    /*if compute_payoff
		        computeFitness();
		        compute_payoff = 0;
		    end*/
			
			double avScore = 0;
			double rsum;
			DoubleMatrix df = new DoubleMatrix( numSpecies );
			for( int col = 0; col < df.length; col++ ) {
				rsum = 0;
				for( int row = 0; row < df.length; row++ ) {
					rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
				}
				df.put( col, rsum );
				avScore += rsum * genotype.get( col ).frequency;
			}		
		
			double newfreq;
			for( int k = 0; k < numSpecies; k++ ) {
				newfreq = genotype.get( k ).frequency * ( df.get( k ) / avScore );		
				genotype.get( k ).frequency = newfreq;
				//System.out.println( "Strategy: " + genotype.get(k).strategy + " freq: " + genotype.get(k).frequency + " dfreq: " + dfreq + " [" + genotype.get( k ).frequency + "," + df.get( k ) + "," + avScore + "]" ); 
			}		
			
			normalizePopulation();
					
		    // Update population counts
		    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
		    		
		}
		
		public void replicatorGillespie() {
			//while p.dt * t < p.max_t
			
			    /*if compute_payoff
			        computeFitness();
			        compute_payoff = 0;
			    end*/
				
				double avScore = 0;
				double rsum;
				DoubleMatrix df = new DoubleMatrix( numSpecies );
				for( int col = 0; col < df.length; col++ ) {
					rsum = 0;
					for( int row = 0; row < df.length; row++ ) {
						rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
					}
					df.put( col, rsum );
					avScore += rsum * genotype.get( col ).frequency;
				}		
			
				double dfreq;
				for( int k = 0; k < numSpecies; k++ ) {
					dfreq = DT * genotype.get( k ).frequency * ( df.get( k ) - avScore );		
					genotype.get( k ).frequency += dfreq;
					//System.out.println( "Strategy: " + genotype.get(k).strategy + " freq: " + genotype.get(k).frequency + " dfreq: " + dfreq + " [" + genotype.get( k ).frequency + "," + df.get( k ) + "," + avScore + "]" ); 
				}		
				
				normalizePopulation();
						
			    // Update population counts
			    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
			    		
			}
		
		public void replicatorWright() {
			//while p.dt * t < p.max_t
			
			    /*if compute_payoff
			        computeFitness();
			        compute_payoff = 0;
			    end*/
				
				double avScore = 0;
				double rsum;
				DoubleMatrix df = new DoubleMatrix( numSpecies );
				for( int col = 0; col < df.length; col++ ) {
					rsum = 0;
					for( int row = 0; row < df.length; row++ ) {
						rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
					}
					df.put( col, rsum );
					avScore += rsum * genotype.get( col ).frequency;
				}	
				
				DoubleMatrix wheel = new DoubleMatrix( numSpecies );
				double cumsum = 0;
				for( int k = 0; k < numSpecies; k++ ) {
					if( k == numSpecies - 1 )
						cumsum = 1;
					else
						//cumsum += df.get( k );
						cumsum += df.get( k ) * genotype.get( k ).frequency / avScore;
					wheel.put( k, cumsum );
					genotype.get( k ).frequency = 0;
				}
			
				//int[] speciesTypes = new int[populationSize];
				for( int k = 0; k < populationSize; k++ ) {
					double r = RNG.nextDouble();
					int idx = 0;
					while( r > wheel.get(idx) ) idx++;
					
					//speciesTypes[k] = idx;
					
					genotype.get( idx ).frequency += 1.0 / (double) populationSize;
					//System.out.println( "Strategy: " + genotype.get(idx).strategy + " freq: " + genotype.get(idx).frequency ) ; 
				}		
				
				normalizePopulation();
						
			    // Update population counts
			    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
			    		
			}
		
		public void replicatorMoran() {
			//while p.dt * t < p.max_t
			
			    /*if compute_payoff
			        computeFitness();
			        compute_payoff = 0;
			    end*/				
			
				//int[] speciesTypes = new int[populationSize];
			
				for( int k = 0; k < populationSize; k++ ) {
					double avScore = 0;
					double rsum;
					DoubleMatrix df = new DoubleMatrix( numSpecies );
					for( int col = 0; col < df.length; col++ ) {
						rsum = 0;
						for( int row = 0; row < df.length; row++ ) {
							rsum += payoff.get( col, row ) * genotype.get( row ).frequency;
						}
						df.put( col, rsum );
						avScore += rsum * genotype.get( col ).frequency;
					}	
					
					// Selecting the new species type
					DoubleMatrix wheel = new DoubleMatrix( numSpecies );
					double cumsum = 0;
					for( int sp = 0; sp < numSpecies; sp++ ) {
						if( sp == numSpecies - 1 )
							cumsum = 1;
						else
							//cumsum += df.get( sp );
							cumsum += df.get( sp ) * genotype.get( sp ).frequency / avScore;
						wheel.put( sp, cumsum );						
					}		
					
					double r = RNG.nextDouble();
					int newidx = 0;
					while( r > wheel.get(newidx) ) newidx++;
					
					// Consider mutating new species if continuous mutation 
					if( continuousMutation ) {
						
				        Individual g = genotype.get(newidx);
				        Individual mutant = null;
				
				        if( g.frequency > extinctionThreshold ) {
					        
					        double ml = g.memoryLength;	        
					        r = RNG.nextDouble() / DT;
					        					        
					        
				            //compute_payoff = 1;
				            if( r < ( muExtend ) * g.frequency ) {
				            	
				            	mutant = g.expandMutation( RNG, maxMemory );
				            	//System.out.println( "Mutate expand\t" + g.strategyString() + " to " + mutant.strategyString() );
				            } else if( r < ( muExtend + muContract )  * g.frequency ) {
				            	//System.out.println( "Mutate contract" );
				            	mutant = g.contractMutation( RNG, maxMemory );
				            	//System.out.println( "Mutate contract\t" + g.strategyString() + " to " + mutant.strategyString() );
				            } else if( r < ( muExtend + muContract + muUniform )  * g.frequency  ){
				            	//System.out.println( "Mutate point" );
				            	mutant = g.uniformMutation( RNG, maxMemory );
				            } else if( r < ( muExtend + muContract + muUniform + muPoint * Math.pow( 2, ml )  )  * g.frequency ){
				            	//System.out.println( "Mutate point" );
				            	//mutant = g.pointMutation( RNG, maxMemory );
				            	mutant = new Individual();
				            	mutant.memoryLength = g.memoryLength;
				            	mutant.strategy = g.strategy;
				            	mutant.frequency = g.frequency;
				            	//System.out.println( "Mutate point\t" + g.strategyString() + " to " + mutant.strategyString() );
				            }	     
				            
				            
				            
				            if( mutant != null ) {
				            	if( muSecondaryUniform > 0 ) {
					            	mutant = mutant.uniformMutation(RNG, maxMemory, muSecondaryUniform);
					            }
				            	
				            	mutant.frequency = extinctionThreshold;//1.0 / populationSize;
				            
				            	genotype.get(newidx).frequency -= extinctionThreshold;
				            	
				            	try {
									addGenotype( mutant );
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
				            }
				        }

					}
					
					// Selecting the species type to replace
					wheel = new DoubleMatrix( numSpecies );
					cumsum = 0;
					for( int sp = 0; sp < numSpecies; sp++ ) {
						if( sp == numSpecies - 1 )
							cumsum = 1;
						else
							cumsum += genotype.get( sp ).frequency;
						wheel.put( sp, cumsum );						
					}							
					
					r = RNG.nextDouble();
					int oldidx = 0;
					while( r > wheel.get(oldidx) ) oldidx++;
					
					//speciesTypes[k] = idx;
					
					genotype.get( oldidx ).frequency -= 1.0 / (double) populationSize;
					genotype.get( newidx ).frequency += 1.0 / (double) populationSize;
					//System.out.println( "Strategy: " + genotype.get(k).strategy + " freq: " + genotype.get(k).frequency + " dfreq: " + dfreq + " [" + genotype.get( k ).frequency + "," + df.get( k ) + "," + avScore + "]" ); 
				}		
				
				normalizePopulation();
						
			    // Update population counts
			    //frequency = frequency + p.dt .* frequency .* ( ( payoff * frequency' )' - ( frequency * payoff ) * frequency' );
			    		
			}
	
	public void extinction() {
	    // Extinction
	    int init_num_species = Math.min( genotype.size(), numSpecies );
	    int k = genotype.size() - 1;
	    numExtinct = 0;
	    while( k >= 0 ) {
	        if( genotype.get( k ).frequency < extinctionThreshold ) {
	        	
	        	speciesEvents.add( "del\t" + genotype.get(k).strategyString() );
	        	
	        	genotype.remove(k);
	        	
	        	// Remove from payoff matrix
	        	DoubleMatrix newPayoff = new DoubleMatrix( payoff.rows - 1, payoff.columns - 1 );
	        	DoubleMatrix newpCC = new DoubleMatrix( payoff.rows - 1, payoff.columns - 1 );
		    	DoubleMatrix newpCD = new DoubleMatrix( payoff.rows - 1, payoff.columns - 1 );
		    	DoubleMatrix newpDC = new DoubleMatrix( payoff.rows - 1, payoff.columns - 1 );
		    	DoubleMatrix newpDD = new DoubleMatrix( payoff.rows - 1, payoff.columns - 1 );	        	
	        	
	        	for( int r = 0; r < newPayoff.rows; r++ ) {
	        		for( int c = 0; c < newPayoff.columns; c++ ) {
	        			newPayoff.put( r, c, payoff.get( ( r > k ? r + 1 : r ),
								 ( c > k ? c + 1 : c ) ) );
	        			newpCC.put( r, c, pCC.get( ( r > k ? r + 1 : r ),
								 ( c > k ? c + 1 : c ) ) );
	        			newpCD.put( r, c, pCD.get( ( r > k ? r + 1 : r ),
								 ( c > k ? c + 1 : c ) ) );
	        			newpDC.put( r, c, pDC.get( ( r > k ? r + 1 : r ),
								 ( c > k ? c + 1 : c ) ) );
	        			newpDD.put( r, c, pDD.get( ( r > k ? r + 1 : r ),
								 ( c > k ? c + 1 : c ) ) );
	        		}
	        	}
	        	
	        	payoff = newPayoff;
	        	pCC = newpCC;
	        	pCD = newpCD;
	        	pDC = newpDC;
	        	pDD = newpDD;

	            numExtinct = numExtinct+ 1;
	            	            
	            
	            numSpecies--;
	        }
	        k = k - 1;	        
	    }
	    
	    
	    numSpecies = genotype.size();
	    normalizePopulation();	    
	}
	
	public void normalizePopulation() {
		/* 
		 * Normalize the frequency of all species such that the population sums to 1
		 */
	
		double sum = 0;
		// Compute sum
		for( int k = 0; k < genotype.size(); k++ ) sum += genotype.get(k).frequency;
		// Divide by sum
		for( int k = 0; k < genotype.size(); k++ ) genotype.get(k).frequency /= sum;
	}	
	
	public Individual selectIndividual( ) {
		//return genotype.get( RNG.nextInt( populationSize ) );
		double r = RNG.nextDouble();
		int k = -1;
		double sum = 0;
		while( sum < r && k < numSpecies - 1 ) {
			k++;
			sum += genotype.get( k ).frequency;
			
		} 
		k = Math.min( k, genotype.size() - 1 );
		//System.out.println( "Selected " + k + " " + genotype.get(k).frequency + " r: " + r + " sum: " + sum );
		return genotype.get( k );		
	}
	
	public void speciesMutation() throws Exception {
		numMutants = (int) Math.round( Math.abs( RNG.nextGaussian() * DT * 10 ) );
	    // Mutation    
		
		// First we make a list of mutants
		ArrayList<Individual> mutants = new ArrayList<Individual>();
	    for( int k = 0; k < numSpecies; k++ ) {
	       	    		    
	        Individual g = genotype.get(k);
	
	        if( g.frequency > extinctionThreshold ) {
		        
		        double ml = g.memoryLength;	        
		        double r = RNG.nextDouble() / DT;
		        
		        Individual mutant = null;
		        
	            //compute_payoff = 1;
	            if( r < ( muExtend ) * g.frequency ) {
	            	
	            	mutant = g.expandMutation( RNG, maxMemory );
	            	//System.out.println( "Mutate expand\t" + g.strategyString() + " to " + mutant.strategyString() );
	            } else if( r < ( muExtend + muContract )  * g.frequency ) {
	            	//System.out.println( "Mutate contract" );
	            	mutant = g.contractMutation( RNG, maxMemory );
	            	//System.out.println( "Mutate contract\t" + g.strategyString() + " to " + mutant.strategyString() );
	            } else if( r < ( muExtend + muContract + muUniform )  * g.frequency  ){
	            	//System.out.println( "Mutate point" );
	            	mutant = g.uniformMutation( RNG, maxMemory );
	            } else if( r < ( muExtend + muContract + muUniform + muPoint * Math.pow( 2, ml )  )  * g.frequency ){
	            	//System.out.println( "Mutate point" );
	            	mutant = g.pointMutation( RNG, maxMemory );
	            	//System.out.println( "Mutate point\t" + g.strategyString() + " to " + mutant.strategyString() );
	            }	     
	            
	            
	            
	            if( mutant != null ) {
	            	if( muSecondaryUniform > 0 ) {
		            	mutant = mutant.uniformMutation(RNG, maxMemory, muSecondaryUniform);
		            }
	            	
	            	mutant.frequency = extinctionThreshold;//1.0 / populationSize;
	            
	            	genotype.get(k).frequency -= extinctionThreshold;
	            	
	            	mutants.add( mutant );
	            }
	        }
        }
	    
	    // Then we add the list of mutants to the population
	    for( Individual c : mutants ) {
	    	addGenotype( c );
	    	//System.out.println( "Mutant\t{" + c.strategyString() + "}");
	    }
	    
	    numSpecies = genotype.size();
	    normalizePopulation();
	}
	
	public void mutation() throws Exception {
		numMutants = (int) Math.round( Math.abs( RNG.nextGaussian() * DT * 10 ) );
	    // Mutation    
		
		// First we make a list of mutants
		ArrayList<Individual> mutants = new ArrayList<Individual>();
	    for( int k = 0; k < numMutants; k++ ) {
	       
	        Individual g = selectIndividual();
	
	        double ml = g.memoryLength;	        
	        double r = RNG.nextDouble();// / DT;
	        
	        Individual mutant;
	        
            //compute_payoff = 1;
            if( r < muExtend ) {
            	//System.out.println( "Mutate expand" );
            	mutant = g.expandMutation( RNG, maxMemory );
            } else if( r < ( muExtend + muContract ) ) {
            	//System.out.println( "Mutate contract" );
            	mutant = g.contractMutation( RNG, maxMemory );
            } else {
            	//System.out.println( "Mutate point" );
            	mutant = g.pointMutation( RNG, maxMemory );
            }
            
            mutant.frequency = 1.0 / numSpecies;
            
            mutants.add( mutant );
        }
	    
	    // Then we add the list of mutants to the population
	    for( Individual c : mutants ) {
	    	addGenotype( c );
	    	//System.out.println( "Mutant\t" + c.strategy );
	    }
	    
	    numSpecies = genotype.size();
	    normalizePopulation();
	}
	
	public void uniformFrequency() {
		for( int k = 0; k < numSpecies; k++ ) {
			genotype.get(k).frequency = 1.0 / numSpecies;
		}
		
	}
	
	public void initUniformPrimitive() throws Exception {
		payoff = new DoubleMatrix( 0, 0 );
		
		Individual i1 = new Individual();
		i1.strategy = new BigInteger( "00", 2 );
		addGenotype( i1 ); //genotype.add( i1 );
		
		Individual i2 = new Individual();
		i2.strategy = new BigInteger( "01", 2 );
		addGenotype( i2 ); //genotype.add( i2 );
		
		Individual i3 = new Individual();
		i3.strategy = new BigInteger( "10", 2 );
		addGenotype( i3 ); //genotype.add( i3 );
		
		Individual i4 = new Individual();
		i4.strategy = new BigInteger( "11", 2 );
		addGenotype( i4 ); //genotype.add( i4 );
		
		uniformFrequency();
		numSpecies = 4;
	}
	
	/**
	* Save chart as PDF file. Requires iText library.
	*
	* @param chart JFreeChart to save.
	* @param fileName Name of file to save chart in.
	* @param width Width of chart graphic.
	* @param height Height of chart graphic.
	* @throws Exception if failed.
	* @see <a href="http://www.lowagie.com/iText">iText</a>
	*/
	public void saveChartToPDF(JFreeChart chart, String fileName, int width, int height) throws Exception {
//	    if (chart != null) {
//	        BufferedOutputStream out = null;
//	        try {
//	            out = new BufferedOutputStream(new FileOutputStream(fileName));
//	               
//	            //convert chart to PDF with iText:
//	            Rectangle pagesize = new Rectangle(width, height);
//	            com.lowagie.text.Document document = new com.lowagie.text.Document(pagesize, 50, 50, 50, 50);
//	            try {
//	                PdfWriter writer = PdfWriter.getInstance(document, out);
//	                document.addAuthor("JFreeChart");
//	                document.open();
//	       
//	                PdfContentByte cb = writer.getDirectContent();
//	                PdfTemplate tp = cb.createTemplate(width, height);
//	                Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
//	       
//	                Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
//	                chart.draw(g2, r2D, null);
//	                g2.dispose();
//	                cb.addTemplate(tp, 0, 0);
//	            } finally {
//	                document.close();
//	            }
//	        } finally {
//	            if (out != null) {
//	                out.close();
//	            }
//	        }
//	    }//else: input values not availabel
	}//saveChartToPDF()
	
	/*public void savePopulation( String filename ) {
		FileWriter fw;
		BufferedWriter writer;
		
		try{
		    fw = new FileWriter( filename );
		    writer = new BufferedWriter(fw);

		    for( int k = 0; k < genotype.size(); k++ ) {
		    	String line = genotype.get( k ).frequency + "\t" + genotype.get( k ).strategyString() + "\n";
		    	writer.write( line );
		    }
		    
		    for( int y = 0; y < payoff.columns; y++ ) {
		    	for( int x = 0; x < payoff.rows; x++ ) {
		    		writer.write( "\t" + payoff.get( x, y ) );
		    	}		    			 
		    	writer.write( "\n" );
		    }
		    
		    writer.close();
		    fw.close();		    
		       
		} catch(Exception e){
		    System.err.println("Cannot save population.");
		    
		}

	}*/
	
	public void transmitFile( String filename ) {
		try {
		    Process p=Runtime.getRuntime().exec("gzip " + filename); 
		    p.waitFor(); 
			
		    String sshDest = "kyle@puritan.cs-i.brandeis.edu:/Volumes/PuritanData/Escalation/";		    
		    
		    p=Runtime.getRuntime().exec("scp " + filename + ".gz " + sshDest ); 
		    p.waitFor();		    		    
		    
		} catch(Exception e){
		    System.err.println("Cannot transmit " + filename );
		    
		}
	}
	
	public void report() {
		if( !enablePlotting )
			//System.out.print( "Time: " + ( DT * t ) + "\tPopulation size: " + numSpecies + " # mutants: " + numMutants + " # extinct: " + numExtinct );
			System.out.print( "Time: " + ( t ) + "\tPopulation size: " + numSpecies + " # mutants: " + numMutants + " # extinct: " + numExtinct );
		
		if( enablePlotting ) {
			
			if( saveNextPopulation ) {
				
				//String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".pop";
				String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".ser";
				savePopulation( pop_fn );
				if( autoGitCommit ) gitCommit( pop_fn  );
							
				
				saveNextPopulation = false;
				
			}
			
			if( t - lastClear >= clearInterval ) {
				
				//save plot
				try {
					String chart_fn = directoryName + "/" + "plot_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".pdf";
					//String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".pop";
					String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".ser";
					String rng_fn = directoryName + "/" + "RNG_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".ser";
					saveChartToPDF( chart, chart_fn, 1000, 750 );
					//if( enableTransmission ) transmitFile( chart_fn );						
					//if( gitCommit ) gitCommit( chart_fn  );			
					savePopulation( pop_fn );
					saveNextPopulation = true;
					//if( enableTransmission ) transmitFile( pop_fn );						
					if( autoGitCommit ) gitCommit( pop_fn  );
					
					// Save the RNG state
					FileOutputStream fout = new FileOutputStream( rng_fn );
					ObjectOutputStream out = new ObjectOutputStream( fout );
					out.writeObject( RNG);
					out.close();
					fout.close();
				} catch( Exception e ) {
					
				}
				
				/*plot.configureDomainAxes();
				for( Object xyp : plot.getSubplots() ) {
					((XYPlot) xyp).configureDomainAxes();
				}*/
				
				//timeAxis.setRange( ( DT * t ) , ( DT *  t + clearInterval ) );
				timeAxis.setRange( t, t + clearInterval );
				
				lastClear = t;
							
				stratFrequency.clear();
				
				for( int k = 1; k < maxMemory; k++ ) {
					memLength[k-1].clear();
				}
				
				numStrat.clear();
				
				xyPDD.clear();
				xyPDC.clear();
				xyPCD.clear();
				xyPCC.clear();
							
				//plot();
			}
			
		}
		
		/*FileWriter fw = null;
		BufferedWriter writer = null;
		
		try{
		    fw = new FileWriter( logFilename, true );

		    writer = new BufferedWriter(fw);

		} catch(Exception e){
			System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");		    
		}*/
		
		double[] memLenFreq = new double[maxMemory];
		double[] memLenCount= new double[maxMemory];
		
		double sumFreq = 0;
		double minFreq = 1;
		double maxFreq = 0;
		
		//System.out.println( "Population profile:" );
		for( int k = 0; k < genotype.size(); k++ ) {
			Individual g = genotype.get(k);
			//System.out.println( "\t" + k + "\t" + g.memoryLength + "\t" + g.frequency + "\t" + g.strategy + "\t" + g.strategyString() );
			/*try {
				//writer.write( t + "\t" + k + "\t" + g.memoryLength + "\t" + g.frequency + "\t" + g.strategy + "\t" + g.strategyString() + "\n" );
				writer.write( t + "\t" + k + "\t" + g.memoryLength + "\t" + g.frequency + "\t" + g.strategy + "\n" );
			} catch(Exception e) {
				System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");
			}*/
			
			if( enablePlotting ) {
				//if( g.frequency > 0 ) {//0.01 ) { 
					//stratFrequency.add( t*DT, g.frequency );
				stratFrequency.add( t, g.frequency );
				//memLenStratFrequency[ g.memoryLength - 1 ].add( t*DT, g.frequency );
				memLenStratFrequency[ g.memoryLength - 1 ].add( t, g.frequency );
				//}
			}
			
			memLenFreq[g.memoryLength - 1] += g.frequency;
			memLenCount[g.memoryLength - 1] ++;
			
			sumFreq += g.frequency;
			if( g.frequency < minFreq ) minFreq = g.frequency;
			if( g.frequency > maxFreq ) maxFreq = g.frequency;
		}
		
		/*double pCC = 0, pCD = 0, pDC = 0, pDD = 0;
		for( int c = 0; c < populationSize; c++ ) {
			for( int r = 0; r < populationSize; r++ ) {
				//Double[] rps = pairwiseResults.get( new BigInteger[] {genotype.get(c).strategy,genotype.get(r).strategy} );
				//double[] rps= playGame( genotype.get( r ), genotype.get( c ) );
				pDD += rps[0];
				pDC += rps[1];
				pCD += rps[2];
				pCC += rps[3];
			}
		}*/
		
		try {
			FileWriter statfw = new FileWriter( directoryName + "/" + statFilename, true );

		    BufferedWriter statwriter = new BufferedWriter(statfw);
		
		    int maxIdx = 0;
		    for( int k = 1; k < genotype.size(); k++ ) {
		    	if( genotype.get(k).frequency > genotype.get(maxIdx).frequency ) maxIdx = k;		    		
		    }
		    		    
		    LUDecomposition<DoubleMatrix> lup = Decompose.lu( payoff );
		    
		    double det = lup.u.diag().prod();
		    
			statwriter.write( t + "\t" + 
//							t * DT + "\t" + 
							genotype.get(maxIdx).strategy + "\t" + 
							genotype.get(maxIdx).memoryLength + "\t" + 
							numSpecies + "\t" + 
							sumFreq / numSpecies + "\t" + 
							minFreq + "\t" + 
							maxFreq + "\t" +
							det );
			for( int k = 1; k < maxMemory; k++ ) {
				statwriter.write( "\t" + memLenFreq[k-1] );
				//memLength.add( t*DT, memLenFreq[k] );
				if( enablePlotting )
					//memLength[k-1].add( t*DT, memLenFreq[k - 1] );
					memLength[k-1].add( t, memLenFreq[k - 1] );
				else
					System.out.print( "\t" + memLenFreq[k-1] );
			}
			//System.out.println();
			
			//numStrat.add( t*DT, genotype.size() );						
			numStrat.add( t, genotype.size() );						
			
			double outcomes = pDD.sum() + pDC.sum() + pCD.sum() + pCC.sum();
			
			statwriter.write( "\t" + pDD.sum() / outcomes + "\t" + 
						pDC.sum() / outcomes + "\t" + 
						pCD.sum() / outcomes + "\t" + 
						pCC.sum() / outcomes  );
			
			if( enablePlotting ) {
				/*xyPDD.add( t*DT, pDD.sum() / ( ( numSpecies - numMutants + numExtinct ) * ( numSpecies - numMutants + numExtinct ) ) );
				xyPDC.add( t*DT, pDC.sum() / ( ( numSpecies - numMutants + numExtinct ) * ( numSpecies - numMutants + numExtinct ) ) );
				xyPCD.add( t*DT, pCD.sum() / ( ( numSpecies - numMutants + numExtinct ) * ( numSpecies - numMutants + numExtinct ) ) );
				xyPCC.add( t*DT, pCC.sum() / ( ( numSpecies - numMutants + numExtinct ) * ( numSpecies - numMutants + numExtinct ) ) );*/
				xyPDD.add( t, pDD.sum() / outcomes );
				xyPDC.add( t, pDC.sum() / outcomes );
				xyPCD.add( t, pCD.sum() / outcomes );
				xyPCC.add( t, pCC.sum() / outcomes );
			}
			//for( )
			
			statwriter.write( "\n" );
			
			statwriter.close();
			statfw.close();
		} catch(Exception e) {
			System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");
		}
		
		/*try {
			writer.close();
			fw.close();
		} catch(Exception e) {
			System.err.println("ERROR: CANNOT READ OR WRITE TO LOG FILE");
		}*/

		/*System.out.println( "Payoff:" );
		for( int r = 0; r < payoff.rows(); r++ ) {
			for( int c = 0; c < payoff.columns(); c++ ) {
				System.out.print( payoff.get(r, c) + "\t" );
    		}
			System.out.print( "\n" );
    	}*/
		
		
	}	
	
	public void gitCommit( String filename ) {
		try {
			/*System.out.println( "git commit of " + filename );
			 String current = new java.io.File( "." ).getCanonicalPath();
		     System.out.println("Current dir:"+current);
		     String currentDir = System.getProperty("user.dir");
		     System.out.println("Current dir using System:" +currentDir);
		     System.out.println( "git add " + filename + ";" );*/
		        
			Process p;
			
			//Process p=Runtime.getRuntime().exec("git add " + filename + ";" );
			if( compressResults ) {
				p = new ProcessBuilder("gzip", filename ).start(); 						
				p.waitFor();
				filename = filename + ".gz";
			}
			
			p = new ProcessBuilder("git", "add", filename ).start(); 						
			p.waitFor();
			
		    p = new ProcessBuilder("git", "commit", "-m", "\"Auto-result add: " + filename + "\"",  filename).start();
		    p.waitFor();
		    
	    
		} catch(Exception e){
		    System.err.println("Cannot commit " + filename );
		    
		}
		
	}
	       
public void evolve( long numSteps ) throws Exception {
		
		if( enablePlotting ) {
			plot();
		}
		
		initUniformPrimitive();
		
		computeFitness();
		
		for( t = 0; t < numSteps; t++ ) {
			//if( t % 1 == 0 )
				report();
			if( replicatorMethod == replicatorContinuousTime ) {
				replicatorContinuousTime();
			} else if( replicatorMethod == replicatorDiscreteTime) {
				replicatorDiscreteTime();
			} else if( replicatorMethod == replicatorMoran) {
				replicatorMoran();
			} else if( replicatorMethod == replicatorGillespie) {
				replicatorGillespie();
			} else if( replicatorMethod == replicatorWright) {
				replicatorWright();
			} else if( replicatorMethod == replicatorEigen ) {
				eigenReplicator();
			}
			extinction();
			//mutation();			
			speciesMutation();			
		}
		
		//if( gitCommit ) gitUpdate( directoryName + "/" +  );			
		
	}	

	public void evolve_resume( long numSteps ) throws Exception {
	
		if( enablePlotting ) {
			plot();
		}
		
		//initUniformPrimitive();
		
		computeFitness();
		
		System.out.println( "Resuming from " + t + " running to " + numSteps + " replicatorMethod is " + replicatorMethod);
		
		for( t = t; t < numSteps; t++ ) {
			//if( t % 1 == 0 )
				report();
			if( replicatorMethod == replicatorContinuousTime ) {
				replicatorContinuousTime();
			} else if( replicatorMethod == replicatorDiscreteTime) {
				replicatorDiscreteTime();
			} else if( replicatorMethod == replicatorMoran) {
				replicatorMoran();
			} else if( replicatorMethod == replicatorGillespie) {
				replicatorGillespie();
			} else if( replicatorMethod == replicatorWright) {
				replicatorWright();
			} else if( replicatorMethod == replicatorEigen ) {
				eigenReplicator();
			}
			extinction();
			//mutation();			
			speciesMutation();			
		}
		
		System.out.println( "Stopped at " + t );
		
		try {
			//String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".pop";
			String pop_fn = directoryName + "/" + "population_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".ser";
			String rng_fn = directoryName + "/" + "RNG_MaxMem_" + maxMemory + "_randomSeed_" + randomSeed + "_UID_" + UID + "_T_" + t + ".ser";
			//if( enableTransmission ) transmitFile( chart_fn );						
			//if( gitCommit ) gitCommit( chart_fn  );			
			savePopulation( pop_fn );
			
			System.out.println( "Saved population to " + pop_fn );
			
			//saveNextPopulation = true;
			//if( enableTransmission ) transmitFile( pop_fn );						
			
			// Save the RNG state
			FileOutputStream fout = new FileOutputStream( rng_fn );
			ObjectOutputStream out = new ObjectOutputStream( fout );
			out.writeObject( RNG);
			out.close();
			fout.close();
			
			System.out.println( "Saved RNG to " + rng_fn );
			
		} catch( Exception e ) {
			
		}
		
		//if( gitCommit ) gitUpdate( directoryName + "/" +  );			
		
	}	
	
	public class Plotter extends ApplicationFrame {

	    /**
	     * Creates a new demo instance.
	     *
	     * @param title  the frame title.
	     */
	    public Plotter(final String title, JFreeChart chart) {
	        super(title);
	        //IntervalXYDataset dataset = createDataset();
	        //JFreeChart chart = createChart(dataset);
	        final ChartPanel chartPanel = new ChartPanel(chart);
	        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 600));
	        setContentPane(chartPanel);
	    }
	}
	
	public void plot() {
		
		/*xyColl.addSeries( stratFrequency );
		xyColl.addSeries( numStrat );
		xyColl.addSeries( memLength );
		xyColl.addSeries( xyPDD );
		xyColl.addSeries( xyPDC );
		xyColl.addSeries( xyPCD );
		xyColl.addSeries( xyPCC );
		
		final JFreeChart chart = ChartFactory.createXYLineChart(
            "Escalation",
            "time",
            "frequency",
            xyColl,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );*/
		
		timeAxis = new NumberAxis( "Time" );
		
		plot = new CombinedDomainXYPlot( timeAxis );
		
		/* One color for all strats
		final XYPlot subplot1 = new XYPlot(
				new XYSeriesCollection( stratFrequency ), null, new NumberAxis( "frequency" ), new XYLineAndShapeRenderer(false,true)//new StandardXYItemRenderer()
        );
        subplot1.setBackgroundPaint(Color.white);
        subplot1.setDomainGridlinePaint(Color.lightGray);
        subplot1.setRangeGridlinePaint(Color.lightGray);
        plot.add(subplot1);*/
		
		/* One color for all strats of each memory length */
		XYSeriesCollection stratMemColl = new XYSeriesCollection( );
        for( int k = 1; k < maxMemory; k++ ) {
        	stratMemColl.addSeries( memLenStratFrequency[k-1] );
        }
        final XYPlot subplot1 = new XYPlot(
        		stratMemColl, null, new NumberAxis("ind mem freq"), new XYLineAndShapeRenderer(false,true)//new StandardXYItemRenderer()
        );
        subplot1.setBackgroundPaint(Color.white);
        subplot1.setDomainGridlinePaint(Color.lightGray);
        subplot1.setRangeGridlinePaint(Color.lightGray); 
        plot.add(subplot1);
        
        final XYPlot subplot2 = new XYPlot(
                new XYSeriesCollection( numStrat ), null, new NumberAxis("diversity"), new StandardXYItemRenderer()
        );
        subplot2.setBackgroundPaint(Color.white);
        subplot2.setDomainGridlinePaint(Color.lightGray);
        subplot2.setRangeGridlinePaint(Color.lightGray);
        plot.add(subplot2);
        
        XYSeriesCollection memColl = new XYSeriesCollection( );
        for( int k = 1; k < maxMemory; k++ ) {
        	memColl.addSeries( memLength[k-1] );
        }
        final XYPlot subplot3 = new XYPlot(
                memColl, null, new NumberAxis("mem freq"), new XYLineAndShapeRenderer(false,true)//new StandardXYItemRenderer()
        );
        subplot3.setBackgroundPaint(Color.white);
        subplot3.setDomainGridlinePaint(Color.lightGray);
        subplot3.setRangeGridlinePaint(Color.lightGray);
        plot.add(subplot3);
        
        XYSeriesCollection roundResults = new XYSeriesCollection( xyPDD );
        roundResults.addSeries( xyPDC );
        roundResults.addSeries( xyPCD );
        roundResults.addSeries( xyPCC );
        final XYPlot subplot4 = new XYPlot(
                roundResults, null, new NumberAxis("probability"), new StandardXYItemRenderer()
        );
        subplot4.setBackgroundPaint(Color.white);
        subplot4.setDomainGridlinePaint(Color.lightGray);
        subplot4.setRangeGridlinePaint(Color.lightGray);
        plot.add(subplot4);
		
        //XYPlot plot = (XYPlot) chart.getPlot();
        //final IntervalMarker target = new IntervalMarker(400.0, 700.0);
        //target.setLabel("Target Range");
        //target.setLabelFont(new Font("SansSerif", Font.ITALIC, 11));
        //target.setLabelAnchor(RectangleAnchor.LEFT);
        //target.setLabelTextAnchor(TextAnchor.CENTER_LEFT);
        //target.setPaint(new Color(222, 222, 255, 128));
        //plot.addRangeMarker(target, Layer.BACKGROUND);
        
        //final JFreeChart chart = new JFreeChart("Escalation of Complex Strategies", plot);
        chart = new JFreeChart("Escalation of Complex Strategies", plot);
//      chart.getLegend().setAnchor(Legend.EAST);
        chart.setBorderPaint(Color.black);
        chart.setBorderVisible(true);
        chart.setBackgroundPaint(Color.white);
      
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
    
        if( enableDisplay ) {
	        demo = new Plotter("Escalation",chart);
	        demo.pack();
	        RefineryUtilities.centerFrameOnScreen(demo);
	        demo.setVisible(true);
        }
	
	}	
	
	public static void main( String[] args ) throws Exception {
		int maxGens = 100000;		
		
		PopulationKnockout p = new PopulationKnockout( );
		//Boolean resume = false;
		String resumeFilename = "";		
		String checkpointFilename = "";
		boolean checkpoint = false;
		int extendedGens = -1;
		
		final ShutdownhookWrapper shw = new ShutdownhookWrapper();
		
		for (int k = 0; k < args.length; k++ ) {
//			System.err.print("\t" + args[k]);

			if ( args[k].compareTo("maxGens") == 0) {
				maxGens = Integer.parseInt( args[k+1] );
			} else if ( args[k].compareTo("extendedGens") == 0 ) {
				extendedGens = Integer.parseInt( args[k+1] );			
			} else if ( args[k].compareTo("populationSize") == 0 ) {
				p.populationSize = Integer.parseInt( args[k+1] );			
			} else if ( args[k].compareTo("randomSeed") == 0 ) {
				p.randomSeed= Integer.parseInt( args[k+1] );
			/*} else if ( args[k].compareTo("sshTarget") == 0 ) {
				p.sshTarget= args[k+1];*/
			//} else if ( args[k].compareTo("runID") == 0 ) {
			} else if ( args[k].compareTo("resumeFilename") == 0 ) {
				resumeFilename = args[k+1];
			} else if ( args[k].compareTo("checkpointFilename") == 0 ) {
				checkpointFilename = args[k+1];
				checkpoint = true;
			} else if ( args[k].compareTo("autoGitCommit") == 0 ) {
				p.autoGitCommit = Boolean.parseBoolean( args[k+1] );
			} else if ( args[k].compareTo("workingDirectory") == 0 ) {
				p.workingDirectory = args[k+1];
			} else if ( args[k].compareTo("logFilename") == 0 ) {
				p.logFilename = args[k+2];
			} else if ( args[k].compareTo("statFilename") == 0 ) {
				p.statFilename = args[k+2];
			} else if ( args[k].compareTo("maxMemory") == 0 ) {
				p.maxMemory= Integer.parseInt( args[k+1] );			
			} else if ( args[k].compareTo("DT") == 0 ) {
				p.DT = Double.parseDouble( args[k+1] );	
			} else if ( args[k].compareTo("clearInterval") == 0 ) {			
				p.clearInterval = Double.parseDouble( args[k+1] );	
			} else if ( args[k].compareTo("replicatorMethod") == 0 ) {			
				p.replicatorMethod= Integer.parseInt( args[k+1] );	
			} else if ( args[k].compareTo("muExtend") == 0 ) {			
				p.muExtend = Double.parseDouble( args[k+1] );	
			} else if ( args[k].compareTo("muContract") == 0 ) {			
				p.muContract= Double.parseDouble( args[k+1] );	
			} else if ( args[k].compareTo("muUniform") == 0 ) {			
				p.muUniform= Double.parseDouble( args[k+1] );	
			} else if ( args[k].compareTo("muPoint") == 0 ) {			
				p.muPoint= Double.parseDouble( args[k+1] );	
			/*} else if ( args[k].compareTo("extinctionThreshold") == 0 ) {			
				p.extinctionThreshold = Double.parseDouble( args[k+1] );*/	
			} else if ( args[k].compareTo("enablePlotting") == 0 ) {			
				p.enablePlotting = Boolean.parseBoolean( args[k+1] );
			} else if ( args[k].compareTo("enableDisplay") == 0 ) {			
				p.enableDisplay = Boolean.parseBoolean( args[k+1] );
			} else if ( args[k].compareTo("neutralFitness") == 0 ) {			
				p.neutralFitness = Boolean.parseBoolean( args[k+1] );
			} else if ( args[k].compareTo("UID") == 0 ) {			
				p.UID = args[k+1];
			} else if ( args[k].compareTo("knockoutSpecies") == 0 ) {			
				p.knockoutSpecies = Integer.parseInt( args[k+1] );
			}
			//public double[] scores1 = { 1.0, 5.0, 0.0, 3.0 };// Scores for player1
			//public double[] scores2 = { 1.0, 0.0, 5.0, 3.0 };// Scores for player 2*/						
		}			
		
		//final Thread mainThread = Thread.currentThread();		
		
		//p.randomSeed = 41332;
		
		/*if( checkpoint ) {
			shw.p = p;
			shw.checkpointFilename = checkpointFilename;
			
			Runtime.getRuntime().addShutdownHook(new Thread() {			   
				public void run() {
				   shw.p.savePopulation( shw.checkpointFilename );
				   System.out.println("Checkpointing");
				   //mainThread.join();
			   }
			  });
		}*/
		
		//if( p.UID == "" ) {
		if( resumeFilename == "" ) {
			//p = new Population( );
			//p.init();
			// evolve shouldn't do initialization stuff
			//p.evolve( maxGens );
			
			System.out.println( "No resume filename" );
		} else {			
			String resumePopFilename = "";
			String resumeRNGFilename = "";
			
			//String oldUID = p.UID;// TEMPORARY HACK
			
			//String directory = p.UID;
			String directory = resumeFilename;
			
			for( File entry : (new File(directory)).listFiles() ) {
				String name = entry.getName();
				if( name.contains("population") ) {
					if( resumePopFilename == "" ) {
						resumePopFilename = name;
					} else { 
						String[] best = resumePopFilename.split("[_\\.]");
						String[] curr = name.split("[_\\.]");
						if( Integer.parseInt( curr[8] ) > Integer.parseInt( best[8] ) ) 
							resumePopFilename = name;
					}
				} else if( name.contains("RNG") ) {
					if( resumeRNGFilename == "" ) {
						resumeRNGFilename = name;
					} else { 
						String[] best = resumeRNGFilename.split("[_\\.]");
						String[] curr = name.split("[_\\.]");
						//System.out.println( resumeRNGFilename + "\t" + name + "\t" + best.length + "\t" + curr.length );
						if( Integer.parseInt( curr[8] ) > Integer.parseInt( best[8] ) ) 
							resumeRNGFilename = name;
					}
				}
			}
			
			if( resumePopFilename == "" || resumeRNGFilename == "" ) {
				p.init();
				System.out.println("Population started from scratch.");
			} else {			
				p = loadPopulation( directory + "/" + resumePopFilename );
				p.RNG = loadRNG( directory + "/" + resumeRNGFilename );
				p.init_stats();
				System.out.println("Population loaded from file. Current generation: " + p.t + " Max gens is: " + maxGens);
			}
			
			// Now do knockout			
			//p.computeFitness();// lazy way
			
	    	// Reduce matrices			
	    	DoubleMatrix newPayoff = new DoubleMatrix( p.payoff.rows - 1, p.payoff.columns - 1 );
	    	DoubleMatrix newpCC = new DoubleMatrix( p.payoff.rows - 1, p.payoff.columns - 1 );
	    	DoubleMatrix newpCD = new DoubleMatrix( p.payoff.rows - 1, p.payoff.columns - 1 );
	    	DoubleMatrix newpDC = new DoubleMatrix( p.payoff.rows - 1, p.payoff.columns - 1 );
	    	DoubleMatrix newpDD = new DoubleMatrix( p.payoff.rows - 1, p.payoff.columns - 1 );
	    	
	    	// Copy old data
	    	for( int r = 0; r < p.payoff.rows; r++ ) {
	    		for( int c = 0; c < p.payoff.columns; c++ ) {
	    			int thisR = r; int thisC = c;
	    			if( r != p.knockoutSpecies && c != p.knockoutSpecies ) {	    				
	    				if( r > p.knockoutSpecies && c > p.knockoutSpecies ) {
	    					thisR--; thisC--;
	    				}
		    			newPayoff.put( thisR, thisC, p.payoff.get( thisR, thisC ) );
		    			newpCC.put( thisR, thisC, p.pCC.get( thisR, thisC ) );
		    			newpCD.put( thisR, thisC, p.pCD.get( thisR, thisC ) );
		    			newpDC.put( thisR, thisC, p.pDC.get( thisR, thisC ) );
		    			newpDD.put( thisR, thisC, p.pDD.get( thisR, thisC ) );
	    			}
	    		}
	    	}
	    	
	    	p.genotype.remove( p.knockoutSpecies );	    	
			
			// evolve shouldn't do initialization stuff
			if( extendedGens > 0 )
				p.evolve_resume( extendedGens );
			else
				p.evolve_resume( maxGens );
			//p.UID = oldUID;// TEMPORARY HACK
			shw.p = p;
			//p.init();
		}
		
		
		
		if( p.autoGitCommit )
			p.gitCommit( p.directoryName + "/" + p.statFilename );
		
		// For debugging 
		/*Individual p1 = new Individual( "1100");
		Individual p2 = new Individual( "0110");
		double[] roundDist = p.playGame( p1, p2 );
		System.out.println( "1100 v. 0110" );
		for( int k = 0; k < roundDist.length; k++ )
			System.out.print( roundDist[k] + " " );
		System.out.println( "" );
		System.out.println( p.BinaryMask( 2, p1, p2) );
		System.out.println( "" );
		
		
		p1 = new Individual( "11001101");
		p2 = new Individual( "01100110");
		roundDist = p.playGame( p1, p2 );
		System.out.println( "11001101 v. 01100110" );
		for( int k = 0; k < roundDist.length; k++ )
			System.out.print( roundDist[k] + " " );
		System.out.println( "" );
		DoubleMatrix bm = p.BinaryMask( 4, p1, p2);
		System.out.println( bm );
		List<DoubleMatrix> rows = bm.rowsAsList();
		for( int k = 0 ; k < bm.rows; k++ )
			System.out.println( rows.get(k) ); 
		System.out.println( "" );		*/
		
		
		//p.plot();
		//p.evolve( 10000 );
		
		/*Individual i1 = new Individual(), i2 = new Individual();
		String[] strats = new String[] { "00", "01", "10", "11" };
		for( int i = 0; i < 4; i++ ) {
			i1.strategy = new BigInteger( strats[i], 2 );
			for( int j = i; j < 4; j++ ) {
				i2.strategy = new BigInteger( strats[j], 2 );
				double[] roundPairs = p.playGame( i1, i2 );
				System.out.print( "Playing " + i1.strategyString() + " v. " + i2.strategyString() );
				for( int k = 0; k < 4; k++ ) {
					System.out.print( "\t" + roundPairs[k] );
				}
				System.out.println();
			}
		}*/
	}
}
