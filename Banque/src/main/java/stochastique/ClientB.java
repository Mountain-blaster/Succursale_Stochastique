package stochastique;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.probdist.*;
import umontreal.ssj.stat.Tally;
import java.io.*;
import java.util.StringTokenizer;
import java.util.LinkedList;

public class ClientB extends Client {
	
	// Generer numero client type B
	
	RandomStream numero = new MRG32k3a();
	int numeroClientB = numero.nextInt(1, 12);
	
	
	int numeroConseillerD;
	double heureArrivee;
	RandomStream streamArr = new MRG32k3a(); // For patience times.
	double p;
	RandomVariateGen genArrB;
	
	
	public ClientB(int numeroConseillerD, double heureArrivee, int numeroClientB) {
		
		super();
		
		this.numeroConseillerD = numeroConseillerD;
		this.heureArrivee = heureArrivee;
		this.numeroClientB = numeroClientB;
		// TODO Auto-generated constructor stub
	}
	
	
	// Generer l'arrivee des clients de type B
	
	public RandomVariateGen genererArr (double muR, double sigmaR) {
		
		double u = streamArr.nextDouble();
	      
	     if (u <= p) {
			
			genArrB = new RandomVariateGen(new MRG32k3a(), new NormalDist(muR, sigmaR));
			
			return genArrB;
			
		}	
			
		else {
			
			return null;
		}
		
	}
	
	
	// Instancier la variable p
	
	public void setp(double p) {
		
		this.p = p;
		
	}
	
}
