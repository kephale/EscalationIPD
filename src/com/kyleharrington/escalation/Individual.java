package com.kyleharrington.escalation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class Individual implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -403804332207697780L;
	
	public int memoryLength = 1;
	public BigInteger strategy = BigInteger.valueOf( 0 );
	
	public double frequency = 0;
	
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
		aOutputStream.defaultWriteObject();
	}	
	
	public Individual() {
		
	}
	
	public Individual( String strStrat ) {
		strategy = BigInteger.valueOf( Integer.parseInt( strStrat, 2 ) );
		memoryLength = (int) Math.round( Math.log( strStrat.length() ) / Math.log(2) );
	}
	
	public String strategyKey() {
		// Maps to a unique strategy key				
		
		return ( memoryLength + "_" + strategy.toString() ); 
	}
	
	public String strategyString() {
		String s = strategy.toString( 2 );
		
		String pad = new String(new char[ (int) Math.pow( 2,  ( memoryLength ) ) - s.length() ]).replace("\0", "0");
		
		return pad + s;
	}
	
	public Individual expandMutation( Random RNG, int maxMemory) {
    	Individual mutant = new Individual();
    	mutant.memoryLength = memoryLength;
    	mutant.strategy = strategy;
    	mutant.frequency = frequency;
    	
    	double memoryExpansions = Math.max( 0, Math.min( maxMemory - mutant.memoryLength, 
    													 Math.ceil( Math.abs( RNG.nextGaussian() ) ) ) );
    	
    	//System.out.println( "Expand mutation: " + mutant.memoryLength + "\t" + memoryExpansions );
    	
    	if( memoryLength + memoryExpansions < maxMemory ) {
	    	mutant.memoryLength = (int) (memoryLength + memoryExpansions);
	    	String strStrat = strategyString();
	    	while( memoryExpansions >= 1 ) {
	    		strStrat = strStrat.concat( strStrat );
	    		memoryExpansions--;
	    	}
	    	mutant.strategy = new BigInteger( strStrat, 2 );
	    	//System.out.println( "Mutant: " + mutant.memoryLength + " " + mutant.strategyString() );
    	}
    	
    	return mutant;
    }
	    
    public Individual contractMutation( Random RNG, int maxMemory ) {
    	Individual mutant = new Individual();
    	mutant.memoryLength = memoryLength;
    	mutant.strategy = strategy;
    	mutant.frequency = frequency;
    	
    	//double memoryContractions = Math.min( ( memoryLength - 1 ), Math.ceil( Math.pow( RNG.nextGaussian(), 2 ) ) );
    	double memoryContractions = Math.min( mutant.memoryLength - 1, 
    										  Math.max( 0, Math.ceil( Math.abs( RNG.nextGaussian() ) ) ) ); 
 							  			   	  
    	
    	//System.out.println( "Contract mutation: " + mutant.memoryLength + "\t" + memoryContractions );
    	
    	//mutant.memoryLength = (int) (memoryLength - memoryContractions);
    	
    	String strStrat = strategyString();
    	while( memoryContractions > 0 ) {
    		int mid = strStrat.length() / 2;
    		if( RNG.nextBoolean() ) {
    			strStrat = strStrat.substring( 0, mid );
    		} else {
    			strStrat = strStrat.substring( mid, strStrat.length() );    			
    		}
    		mutant.memoryLength--;
    		memoryContractions--;
    		
    	}
    	mutant.strategy = new BigInteger( strStrat, 2 );
    	//System.out.println( "Mutant strat: " + strStrat + " " + mutant.strategy );
    	//System.out.println( "Mutant: " + mutant.memoryLength + " " + mutant.strategyString() );
    	
    	return mutant;
    }
	    
    public Individual pointMutation( Random RNG, int maxMemory ) {
    	Individual mutant = new Individual();
    	mutant.memoryLength = this.memoryLength;
	    
    	String strStrat = strategyString();
    	int bitIdx = RNG.nextInt( strStrat.length() );
    	//System.out.println( "\npointMutation: " + bitIdx + "\t" + strStrat.charAt( bitIdx ) + "\t" + ( strStrat.charAt( bitIdx ) == '0' ) );
    	//System.out.println( strStrat );
    	if( strStrat.charAt( bitIdx ) == '0' ) {
    	//if( strStrat.substring( bitIdx, bitIdx + 1 ) == "0" ) {
    		if( bitIdx == 0 ) {
    			strStrat = "1" + strStrat.substring( 1 );
    		} else {
    			//System.out.println( strStrat.substring( 0, bitIdx ) + "\t1\t" + strStrat.substring( bitIdx + 1 ) );
    			strStrat = strStrat.substring( 0, bitIdx ) + "1" + strStrat.substring( bitIdx + 1 );
    		}
    	} else {
    		if( bitIdx == 0 )
    			strStrat = "0" + strStrat.substring( 1 );
    		else
    			strStrat = strStrat.substring( 0, bitIdx ) + "0" + strStrat.substring( bitIdx + 1 );
    	}
    	//System.out.println( strStrat );//+ "\t" + strStrat.substring( 0, bitIdx - 1 ) + "\t" + strStrat.substring( bitIdx ) );
    	mutant.strategy = new BigInteger( strStrat, 2 );
    	//System.out.println( "\t" + mutant.strategy.bitLength() );
    	
    	//System.out.println( bitIdx + "\t" + mutant.strategy.bitLength() );
    	//mutant.strategy.flipBit( bitIdx );
    	
    	return mutant;
    }
    
    public Individual uniformMutation( Random RNG, int maxMemory, double flipProb ) {
    	Individual mutant = new Individual();
    	mutant.memoryLength = this.memoryLength;
	    
    	String strStrat = strategyString();

    	String mutStrat = "";
    	
    	for( int bitIdx = 0; bitIdx < strStrat.length(); bitIdx++ ) {
    		//if( RNG.nextBoolean() ) {
    		if( RNG.nextDouble() < flipProb ) {
    			if( strStrat.charAt( bitIdx ) == '0' ) mutStrat += '1'; 
    			else mutStrat += '0';
    		} else {
    			mutStrat += strStrat.charAt( bitIdx );
    		}

    	}
    	//System.out.println( strStrat );//+ "\t" + strStrat.substring( 0, bitIdx - 1 ) + "\t" + strStrat.substring( bitIdx ) );
    	mutant.strategy = new BigInteger( mutStrat, 2 );
    	//System.out.println( "\t" + mutant.strategy.bitLength() );
    	
    	//System.out.println( bitIdx + "\t" + mutant.strategy.bitLength() );
    	//mutant.strategy.flipBit( bitIdx );
    	
    	return mutant;
    }
    
    public Individual uniformMutation( Random RNG, int maxMemory ) {
    	return this.uniformMutation(RNG, maxMemory, 0.5);    	
    }
	
	public static void main( String[] argv ) {
		Random RNG = new Random(0);
		Individual i = new Individual();
		i.strategy = new BigInteger( "10", 2 );
		System.out.println( "Initial: " + i.strategy + "\t" + i.strategyString() );
		
		int maxMemory = 6;
		
		Individual expand = i.expandMutation( RNG, maxMemory );
		System.out.println( "Expand: " + expand.strategy + "\t" + expand.strategyString() );
		 expand = i.expandMutation( RNG, maxMemory );
		System.out.println( "Expand: " + expand.strategy + "\t" + expand.strategyString() );
		 expand = i.expandMutation( RNG, maxMemory );
		System.out.println( "Expand: " + expand.strategy + "\t" + expand.strategyString() );
		 expand = i.expandMutation( RNG, maxMemory );
		System.out.println( "Expand: " + expand.strategy + "\t" + expand.strategyString() );
		 expand = i.expandMutation( RNG, maxMemory );
		System.out.println( "Expand: " + expand.strategy + "\t" + expand.strategyString() );
		
		Individual contract = expand.contractMutation( RNG, maxMemory );
		System.out.println( "Contract: " + contract.strategy + "\t" + contract.strategyString() );
		 contract = expand.contractMutation( RNG, maxMemory );
		System.out.println( "Contract: " + contract.strategy + "\t" + contract.strategyString() );
		 contract = expand.contractMutation( RNG, maxMemory );
		System.out.println( "Contract: " + contract.strategy + "\t" + contract.strategyString() );
		 contract = expand.contractMutation( RNG, maxMemory );
		System.out.println( "Contract: " + contract.strategy + "\t" + contract.strategyString() );
		 contract = expand.contractMutation( RNG, maxMemory );
		System.out.println( "Contract: " + contract.strategy + "\t" + contract.strategyString() );
		
		Individual point = expand.pointMutation( RNG, maxMemory );
		System.out.println( "Point: " + point.strategy + "\t" + point.strategyString() );
		point = expand.pointMutation( RNG, maxMemory );
		System.out.println( "Point: " + point.strategy + "\t" + point.strategyString() );
		point = expand.pointMutation( RNG, maxMemory );
		System.out.println( "Point: " + point.strategy + "\t" + point.strategyString() );
		point = expand.pointMutation( RNG, maxMemory );
		System.out.println( "Point: " + point.strategy + "\t" + point.strategyString() );
		point = expand.pointMutation( RNG, maxMemory );
		System.out.println( "Point: " + point.strategy + "\t" + point.strategyString() );
	}
	
	// Crossover
}
