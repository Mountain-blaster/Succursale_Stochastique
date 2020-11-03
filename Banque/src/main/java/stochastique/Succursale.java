package stochastique;
import umontreal.ssj.simevents.*;


import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.probdist.*;
import umontreal.ssj.stat.Tally;
import java.io.*;
import java.util.StringTokenizer;
import java.util.LinkedList;

public class Succursale {
	
	
	
	
	Accumulate toWaitA = new Accumulate("Taille de la file d'attente A");
	
	Tally custWaitsA = new Tally("Temps d'attente des clients A");
	
	
	
	
	double arrRate = 0.0;  // Current arrival rate.
	double[] lambda;       // Base arrival rate lambda_j for each j.
	double muA;  // Current arrival rate.
	double sigmaA;  // Current arrival rate.
	double lambdaj;  // Current arrival rate.
	int nArrivals;         // Number of arrivals today;
	int nBusyCaissier;         
	int[] nCaissiers;       // Number of agents for each period. 
	int n;       		// Nombre de caissiers durant la periode j
	
	
	int nBusyConseillers;
	int[] nConseillers;
	int m; //Nombre de conseillers durant la periode j;
	
	
	
	
	
	Event nextArrivalA = new ArrivalA();  
	Event nextDepartureA = new DepartureA();
	
	   
	
	RandomStream streamArr = new MRG32k3a();
	
	RandomVariateGen genArr;
	
	RandomVariateGen genServA;
	
	RandomVariateGen genServB;
	
	LinkedList<ClientA> servListA = new LinkedList<ClientA>() ;
	
	LinkedList<ClientA> waitListA = new LinkedList<ClientA>() ;
	
	
	
	public Succursale(double lambda, double muA, double sigmaA, double muB, double sigmaB) {
		

		genArr = new RandomVariateGen(new MRG32k3a(), new PoissonDist(lambda));
		
		
		genServA = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muA, sigmaA));
		
		
		genServB = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muB, sigmaB));
		
		
	}
	
	
	// ClientA possibilite d'etre servi par les conseillers
	
	
	class ArrivalA extends Event {
		
		public void actions() {
						
			nextArrivalA.schedule(ExponentialDist.inverseF (arrRate, streamArr.nextDouble()));
         
			nArrivals++;
			
			ClientA custA = new ClientA(); // Call just arrived.
			
			
			custA.arrivalTime = Sim.time();
			custA.serviceTime = genServA.nextDouble();
			
			if (nBusyCaissier >= n ) {
				
				waitListA.addLast(custA);
				
				toWaitA.update(waitListA.size());
				
				
			}
			
			else {
				
				nBusyCaissier++;
				
				custWaitsA.add(0.0);
				
				servListA.addLast(custA);
				
				new DepartureA().schedule(custA.serviceTime);
				
			}
      }
				
	}
	
	
	class DepartureA extends Event  {
			
		public void actions() {
			
			servListA.removeFirst();
			
			if ((waitListA.size() > 0) && (nBusyCaissier < n)) {
				//commencer un service pour le prochain client
				ClientA custA1 = (ClientA)waitListA.removeFirst();
				toWaitA.update(waitListA.size());
				
				custWaitsA.add (Sim.time() -  custA1.arrivalTime);
				
				servListA.addLast(custA1);
				
				nextDepartureA.schedule (custA1.serviceTime);
				
			}
	      }
					
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		
		
		// TODO Auto-generated method stub

	}
		
}
	
