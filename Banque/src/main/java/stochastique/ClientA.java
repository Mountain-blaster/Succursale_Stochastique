package stochastique;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.probdist.*;
import umontreal.ssj.stat.Tally;
import java.io.*;
import java.util.StringTokenizer;
import java.util.LinkedList;

public class ClientA extends Client {
	
	RandomVariateGen genArrival;
	
	
	public ClientA(double lambda, double muA, double sigmaA) throws IOException {
		
		super(muA, sigmaA);
		
		
		genArrival = new RandomVariateGen(new MRG32k3a(), new PoissonDist(lambda));
		
		
		// TODO Auto-generated constructor stub
	}

}
