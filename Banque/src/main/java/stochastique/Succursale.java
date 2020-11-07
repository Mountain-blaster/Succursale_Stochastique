package stochastique;
import umontreal.ssj.simevents.*;

import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.probdist.*;
import umontreal.ssj.stat.Tally;
import java.io.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class Succursale {
	
	 static final double HOUR = 3600.0;  // Time is in seconds.
	
	
	Accumulate toWaitA = new Accumulate("Taille de la file d'attente A");
	Accumulate toWaitB = new Accumulate("Taille de la file d'attente B");
	
	Tally statArrivalsA = new Tally ("Nombre de clients A recu par jour");
	Tally custWaitsA = new Tally("Temps d'attente des clients A");
	Tally statWaitsDayA = new Tally ("Waiting times within a day");
	
	Tally custWaitsB = new Tally("Temps d'attente des clients B");
	Tally statWaitsDayB = new Tally ("Waiting times within a day");
	Tally statArrivalsB = new Tally ("Nombre de clients B recu par jours");
	
	double openingTime;    // Heure d'ouverture de la banque.
	double arrRate = 0.0;  // Current arrival rate.
	double[] lambda;       // Base arrival rate lambda_j for each j.
	double muA;  // Current arrival rate.
	double sigmaA;  // Current arrival rate.
	double muB;  // Current arrival rate.
	double sigmaB;  // Current arrival rate.
	double muR;  // Current arrival rate.
	double sigmaR;  // Current arrival rate.
	double s; //duree pour qu'un conseiller serve un client A
	double p; // probabilite de ne pas se presenter pour le client type B
	double r; // Probabilite d'avoir rendez-vous sur une plage
	int numPeriods;        // Number de periodes de travail (hours) pour un jour.
	int numPlages = 12;        // Nombre de plages pour un jour.
	int n; // nombre de jours
	
	int nArrivalsA;         // Number of arrivals today;
	int nBusyCaissiers;         
	int[] nCaissiers;       // Number of agents for each period. 
	int nj;       		// Nombre de caissiers durant la periode j
	int nClientAPrevu; // Nombre de clients A prevu pour la journee.
	
	int mArrivalsB;         // Number of arrivals today
	int mBusyConseillers; //Nombre de cconseillers occupes durant une periode
	int[] mConseillers; //Nombre de conseillers pour chaque periode 
	int mj; //Nombre de conseillers durant la periode j;
	int mClientBPrevu; // Nombre de clients B prevu pour la journee.
	
	
	Event nextArrivalA = new ArrivalA();  
	Event nextDepartureA = new DepartureA();
	
	Event nextArrivalB = new ArrivalB();
	Event nextDepartureB = new DepartureB();
	   
	RandomStream streamArrA = new MRG32k3a();
	RandomStream streamArrB = new MRG32k3a(); // Probabilite Pour l'arrivee du client B.
	
	RandomVariateGen genArrA;
	RandomVariateGen genArrB;
	RandomVariateGen genServA;
	RandomVariateGen genServB;
	
	LinkedList<ClientA> servListA = new LinkedList<ClientA>() ;
	LinkedList<ClientA> waitListA = new LinkedList<ClientA>() ;
	LinkedList<ClientB> waitListB = new LinkedList<ClientB>() ;
	LinkedList<ClientB> servListB = new LinkedList<ClientB>() ;
	LinkedList<Conseiller> conseillerList = new LinkedList<Conseiller>();
	
	
	
	public Succursale(String fileName) throws IOException{
		//genArrA = new RandomVariateGen(new MRG32k3a(), new PoissonDist(lambda));
		//genArrB = new RandomVariateGen(new MRG32k3a(), new NormalDist(muR, sigmaR));
		readData (fileName);
		genServA = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muA, sigmaA));
		genServB = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muB, sigmaB));
	}
	
	
	class ArrivalA extends Event {
		public void actions() {
			nextArrivalA.schedule(ExponentialDist.inverseF (arrRate, streamArrA.nextDouble()));
			nArrivalsA++;
			System.out.println (nArrivalsA); 
			ClientA custA = new ClientA(); // Call just arrived.
			
			custA.arrivalTime = Sim.time();
			custA.serviceTime =genServA.nextDouble();
			
			if(nBusyCaissiers < nj) {
				custWaitsA.add(0.0);
				servListA.addLast(custA);
				nBusyCaissiers++;
				//new DepartureA().schedule(custA.serviceTime);
			}
			else {
				waitListA.addLast(custA);
				toWaitA.update(waitListA.size());
			}
      }
	}
	
	
	
	
	public void checkQueueA() {
		while((waitListA.size()>0) && (nBusyCaissiers < nj)) {
			ClientA clienta = ((ClientA)waitListA.removeFirst());
			toWaitA.update(waitListA.size());
			custWaitsA.add(Sim.time() - clienta.arrivalTime);
			servListA.addLast(clienta);
			nBusyCaissiers++;
			nextDepartureA.schedule(clienta.serviceTime);
		}
	}
	
		
		
	class DepartureA extends Event  {
		public void actions() {	
			servListA.removeFirst();
			nBusyCaissiers--;
			checkQueueA();
	    }
	}
	
	class NextPeriod extends Event {
		int j; // numero de la periode
		public NextPeriod (int period) { j = period;}
		
		public void actions() {
			if(j < numPeriods) {
				nj = nCaissiers[j];
				mj = mConseillers[j];
				for(int i = 0; i<mj; i++) {
					conseillerList.add(new Conseiller(i));
				}
				System.out.println("Il y a " + conseillerList.size()+ " conseillers pour la periode j="+j);
				arrRate = lambda[j] / HOUR;
				
				if(j == 0) {
					//generer l'arrivee du premier clientA
					nextArrivalA.schedule(ExponentialDist.inverseF (arrRate, streamArrA.nextDouble()));
				}
				else {
					checkQueueA();
					nextArrivalA.reschedule((nextArrivalA.time() - Sim.time()) 
                            * lambda[j-1] / lambda[j]);
				}
				conseillerList.clear();
				new NextPeriod(j+1).schedule(2.0 * HOUR);
			}
			else {nextArrivalA.cancel();}
		}
	}
	
	
	
	// ClientA possibilite d'etre servi par les conseillers
	
	public boolean ConseillerServA(int numeroConseillerD) {
		Conseiller conseiller = new Conseiller(numeroConseillerD);
		ArrayList<Plage> Plage = conseiller.getReunions(r, openingTime*HOUR);
		double heureRv = Plage.get(numPlages).Rv.heureRv;			
		if ((Sim.time() - heureRv) > s) {
			return true;
		}
		
		return false;	
	}
	
	// Generer l'arrivee des clients de type B
	
	public boolean verArrB (ClientB clientb) {
		clientb.p = p;
		double u = streamArrB.nextDouble();
		if (u >= clientb.p) {
				return true;
		}	
		else {	
				return false;
		}	
	}
	
	public Conseiller getConseiller(int numeroConseillerD) {
		Conseiller conseillerB;
		
		
		for(int i = 0; i<conseillerList.size() ; i++) {
			if(conseillerList.get(i).numeroConseillerD == numeroConseillerD ) {
				conseillerB = conseillerList.get(i);
				return conseillerB;
			}
		}
		return null;	
	}
	
	
	class ArrivalB extends Event {
		public void actions() {
			int numeroConsB1 =   new MRG32k3a().nextInt(0, mj-1);
			ClientB custB1 = new ClientB(numeroConsB1); 			
			if (verArrB(custB1)) {
				mArrivalsB++;
				RandomVariateGen R = new RandomVariateGen(new MRG32k3a(), new NormalDist(muR, sigmaR));
				double retard = R.nextDouble();
				custB1.arrivalTime = Sim.time();
				custB1.serviceTime = genServB.nextDouble();
				Conseiller conseillerB1 = getConseiller(custB1.numeroConseillerD);
				ArrayList<Plage> Plage = conseillerB1.getReunions(r, openingTime * HOUR);
				custB1.heureRv = Plage.get(custB1.numeroClientB).Rv.heureRv;
				System.out.println(custB1.heureRv);
				nextArrivalB.schedule(0.5*HOUR + retard);
				
				
				if ((custB1.heureRv <= Sim.time()) && (conseillerB1.libre == true)) {
					if (retard <= 0) {
						double wait = Sim.time() - custB1.arrivalTime;
						custWaitsB.add(wait);
					}
					else {
						custWaitsB.add(0.0);
					}
					conseillerB1.libre = false;
					servListB.addLast(custB1);
					mBusyConseillers++;
					new DepartureB().reschedule(custB1.serviceTime);
					conseillerB1.libre = true;
					
					double interprochainheureRv = Plage.get(numPlages + 1).Rv.heureRv - Sim.time();
					while ((waitListA.size() > 0) && (interprochainheureRv >= s)) {
						//commencer un service pour le client A
						ClientA custA2 = (ClientA)waitListA.removeFirst();
						toWaitA.update(waitListA.size());
						custWaitsA.add (Sim.time() -  custA2.arrivalTime);
						servListA.addLast(custA2);
						nextDepartureA.schedule(custA2.serviceTime);
					}
				}
				else {
					waitListB.addLast(custB1);
					toWaitB.update(waitListB.size());
				}
			}
		}
	}
	
	public void checkQueueB() {
		while((waitListB.size()>0) && (mBusyConseillers <mj)) {
			ClientB custB1 = ((ClientB)waitListB.removeFirst());
			// On recupere le conseiller qui doit servir le client B1
			Conseiller conseillerB1 = getConseiller(custB1.numeroConseillerD);
			
			ArrayList<Plage> Plage = conseillerB1.getReunions(r, openingTime*HOUR);
			RandomVariateGen R = new RandomVariateGen(new MRG32k3a(), new NormalDist(muR, sigmaR));
			double retard = R.nextDouble();
			custB1.heureRv = Plage.get(numPlages).Rv.heureRv;

			if ((custB1.heureRv <= Sim.time()) && (conseillerB1.libre == true)) {
				if (retard < 0) {
					double wait = Sim.time() - custB1.arrivalTime;
					custWaitsB.add(wait);
				}
				else {
					custWaitsB.add(0.0);
				}
				mBusyConseillers++;
				conseillerB1.libre = false;
				servListB.addLast(custB1);
				new DepartureB().schedule(custB1.serviceTime);
				conseillerB1.libre = true;
					
				double interprochainheureRv = Plage.get(numPlages + 1).Rv.heureRv - Sim.time();
					
				while ((waitListA.size() > 0) && (interprochainheureRv >= s)) {
					//commencer un service pour le client A
					ClientA custA2 = (ClientA)waitListA.removeFirst();
					toWaitA.update(waitListA.size());
					custWaitsA.add (Sim.time() -  custA2.arrivalTime);
					servListA.addLast(custA2);
					nextDepartureA.schedule(custA2.serviceTime);
				}
			}
			else {
				waitListB.addLast(custB1);
				toWaitB.update(waitListB.size());
			}
		}
	}
	

	
	class DepartureB extends Event {
		public void actions() {	
			servListB.removeFirst();
			mBusyConseillers--;
	    }
		
	}
	
	class NextPlage extends Event {
		int plage_p;
		public NextPlage(int plage) { plage_p = plage;}
		
		public void actions() {
			if(plage_p < numPlages) {
				if(plage_p%4 == 0) {
					//nextArrivalB.schedule((new RandomVariateGen(new MRG32k3a(), new NormalDist(muR, sigmaR)).nextDouble()));
					nextArrivalB.schedule(0.0);
				}
				else {
					checkQueueB(); 
					nextArrivalB.scheduleNext();
				}
				new NextPlage(plage_p+1).schedule (0.5 * HOUR);
			}
			else {nextArrivalB.cancel();}
		}
	}
	
	
	
	
	// Reads data and construct arrays.
	public void readData (String fileName) throws IOException {
		BufferedReader input = new BufferedReader (new FileReader (fileName));
	    StringTokenizer line = new StringTokenizer (input.readLine());
	    openingTime = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    numPeriods  = Integer.parseInt (line.nextToken());
	      
	    nCaissiers = new int[numPeriods];
	    mConseillers = new int[numPeriods];
	    lambda = new double[numPeriods];
	    nClientAPrevu = 0;
	    mClientBPrevu = 0;
	    for (int j=0; j < numPeriods; j++) {
	    	line = new StringTokenizer (input.readLine());
	        nCaissiers[j] = Integer.parseInt (line.nextToken());
	        mConseillers[j] = Integer.parseInt (line.nextToken());
	        lambda[j]    = Double.parseDouble (line.nextToken());
	        nClientAPrevu = (int) lambda[j];
	        mClientBPrevu += mConseillers[j]*4;
	    }
	    line = new StringTokenizer (input.readLine());
	    muA = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    sigmaA = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    muR = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    sigmaR = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    muB = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    sigmaB = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    r = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    p = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    s = Double.parseDouble (line.nextToken());
	    line = new StringTokenizer (input.readLine());
	    n = Integer.parseInt (line.nextToken());
	    input.close();
	 }
	
	
	
	public void simulateOneDay () { 
	      Sim.init();        	statWaitsDayA.init();		statWaitsDayB.init();
	      nArrivalsA = 0;     	mArrivalsB = 0;     
	      nBusyCaissiers = 0;  	mBusyConseillers = 0;
	      

	      new NextPeriod(0).schedule (openingTime * HOUR);
	      //new NextPlage(0).schedule (openingTime * HOUR);
	      Sim.start();
	      // Here the simulation is running...

	      statArrivalsA.add ((double)nArrivalsA);
	      //statArrivalsB.add ((double)mArrivalsB);
	      custWaitsA.add (statWaitsDayA.sum() / nClientAPrevu);
	      //custWaitsB.add (statWaitsDayB.sum() / mClientBPrevu);
	      
	}

	   
	public static void main (String[] args) throws IOException{ 
	      Succursale cc = new Succursale ("Succursale.dat"); 
	      for (int i=1; i <= 1; i++)  cc.simulateOneDay();
	      System.out.println ("\nNum. clients A expected = " + cc.nClientAPrevu +"\n");
	      System.out.println ("\nNum. clients B expected = " + cc.mClientBPrevu +"\n");
	      System.out.println (cc.toWaitA.report());
	      System.out.println (cc.custWaitsA.report()); 
	      System.out.println (cc.statArrivalsA.report());
	      System.out.println ("\n==================================================================\n");
	      //System.out.println (cc.toWaitB.report());
	      //System.out.println (cc.custWaitsB.report());
	      //System.out.println (cc.statArrivalsB.report());
	      System.out.println (cc.nArrivalsA); 
	      //System.out.println (cc.conseillerList.size()); 
	   }
	}