package stochastique;
import umontreal.ssj.simevents.*;


import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.probdist.*;
import umontreal.ssj.stat.Tally;
import java.io.*;
import java.util.StringTokenizer;

import stochastique.Succursale;

import java.util.LinkedList;


public abstract class Client {
	
	// Data
	
	double arrivalTime, serviceTime, patienceTime;
	
	RandomVariateGen genServe; // For service times; created in readData().
	 
	public Client(double mu, double sigma) throws IOException {
		
	      // genServ can be created only after its parameters are read.
	      // The acceptanc/rejection method is much faster than inversion.
	     
		genServe = new RandomVariateGen(new MRG32k3a(), new LognormalDist(mu, sigma));  
	      
	      
	  }

}
