package com.kyleharrington.escalation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ExtinctionAnalysis {
	
	public static void analyzeDirectory( String directory, String outfile ) {
		File dirF = new File( directory );
		
		ArrayList<double[]> introduction = new ArrayList<double[]>();
		ArrayList<double[]> extinction= new ArrayList<double[]>();
		
		int maxMem = 12;
		
		for( String filename : dirF.list() ) {
			if( filename.contains("ser") && filename.contains("population") ) {
				double[] add = new double[maxMem];
				double[] del = new double[maxMem];
				
				Population p = Population.loadPopulation( directory + filename, false );
				
				for( String event : p.speciesEvents ) {
					String strat = event.substring(4);
					int memLength = (int) ( Math.log( strat.length() ) / Math.log(2) );
					
					if( event.contains("add") ) 
						add[memLength]++;
					else if( event.contains("del") ) 
						del[memLength]++;
				}
				
				introduction.add( add );
				extinction.add( del );
			}
		}
		
		try {
			BufferedWriter w = new BufferedWriter( new FileWriter( outfile ) );
			
			for( int k = 0; k < introduction.size(); k++ ) {
				String line = "";
				for( int i = 0; i < maxMem; i++ ) {
					line += "\t" + introduction.get(k)[i] + "\t" + extinction.get(k)[i];
				}
				w.write( line + "\n" );
			}
			
			w.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void main( String args[] ) {
		analyzeDirectory( "/Users/kyle/EscalationIPD_final2/1154354806841656_4741@compute-0-18.local/", "testout.csv" );
	}

}
