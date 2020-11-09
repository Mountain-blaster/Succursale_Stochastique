package stochastique;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import umontreal.ssj.charts.HistogramChart;
import umontreal.ssj.probdist.ExponentialDist;
import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdist.PoissonDist;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.stat.Tally;
import umontreal.ssj.stat.TallyStore;

public class Succursale {
	
	 static final double HOUR = 3600.0;  // Time is in seconds.
	
	
	Accumulate toWaitA = new Accumulate("Taille de la file d'attente A pour n jour");
	Accumulate toWaitB = new Accumulate("Taille de la file d'attente B pour n jour");
	
	TallyStore[] tally =  new TallyStore[4];
	TallyStore SumwaitsA = tally[0] =new TallyStore("Temps d'attente moyenne des clients A pour n jours");
	TallyStore SumwaitsB = tally[1] = new TallyStore("Temps d'attente moyenne des clients B pour n jours");
	//TallyStore[] tally =  new TallyStore[4];
	
	
	Tally custWaitsA = new Tally("Temps d'attente moyen pour un client A i.e (wa)");
	Tally statWaitsDayA = new Tally ("Temps d'attente de chauqe client A pour une jour");
	Tally statArrivalsA = new Tally ("Nombre de clients A recu par jour");
	
	Tally custWaitsB = new Tally("Temps d'attente des clients B i.e (wb)");
	Tally statWaitsDayB = new Tally ("Temps d'attente de chauqe client B pour une jour");
	Tally statArrivalsB = new Tally ("Nombre de clients B recu par jours");
	
	double openingTime;    // Heure d'ouverture de la banque.
	double arrRate = 0.0;  // Current arrival rate.
	double[] lambda;       // Base arrival rate lambda_j for each j.
	double lambdaj;
	double muA;  // declaration de la variable muA.
	double sigmaA; 
	double muB;  
	double sigmaB;  
	double muR; 
	double sigmaR;  
	double s; //duree minmale pour qu'un conseiller serve un client A
	double p; // probabilite de ne pas se presenter pour le client type B
	double r; // Probabilite d'avoir rendez-vous sur une plage
	int numPeriods;        // Number de periodes de travail (hours) pour un jour.
	int numPlages = 12;        // Nombre de plages pour un jour.
	int n; // nombre de jours
	
	int nArrivalsA;         //  Nombre de clients A qui arrivent par jour;
	int nBusyCaissiers;         
	int[] nCaissiers;       // Number of agents for each period. 
	int nj;       		// Nombre de caissiers durant la periode j
	int nClientAPrevu; // Nombre de clients A prevu pour la journee.
	
	int mArrivalsB;         //  Nombre de clients B qui arrivent par jour;
	int mBusyConseillers; //Nombre de conseillers occupes durant une periode
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
		readData (fileName);
		genServA = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muA, sigmaA));
		genServB = new RandomVariateGen(new MRG32k3a(), new LognormalDist(muB, sigmaB));
	}
	
	
	class ArrivalA extends Event {
		public void actions() {
			nextArrivalA.schedule(ExponentialDist.inverseF (arrRate, streamArrA.nextDouble()));
			nArrivalsA++;
			ClientA custA = new ClientA(); // Client  qui vient d'arriver.
			custA.arrivalTime = Sim.time();
			custA.serviceTime =genServA.nextDouble();
			
			//Verifions s'il y a des caissiers libres
			if(nBusyCaissiers < nj) {
				statWaitsDayA.add(0.0);
				servListA.addLast(custA);
				nBusyCaissiers++;
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
			statWaitsDayA.add(Sim.time() - clienta.arrivalTime);
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
		public NextPeriod (int period) {
			j = period;
		}
		public void actions() {
			conseillerList.clear();
			if(j < numPeriods) {
				nj = nCaissiers[j];
				mj = mConseillers[j];
			for(int i = 0; i<mj; i++) {
					conseillerList.add(new Conseiller(i));
				}
				arrRate = lambda[j] / (2.0 * HOUR);
				
				if(j == 0) {
					//generer l'arrivee du premier clientA
					nextArrivalA.schedule(ExponentialDist.inverseF (arrRate, streamArrA.nextDouble()));
					nextArrivalB.schedule(0.0);
				}
				else {
					nextArrivalA.reschedule((nextArrivalA.time() - Sim.time()) 
                            * lambda[j-1] / lambda[j]);
					nextArrivalB.scheduleNext();
					
				}
				new NextPeriod(j+1).schedule(2.0 * HOUR);
			}
			else {nextArrivalA.cancel();}
		}
	}
	
	
	
	// ClientA (la possibilite d'etre servi par un conseiller)
	public boolean ConseillerServA(int numeroConseillerD) {
		Conseiller conseiller = new Conseiller(numeroConseillerD);
		ArrayList<Plage> Plage = conseiller.getReunions(r, openingTime);
		double heureRv = Plage.get(numPlages).Rv.heureRv;			
		if ((Sim.time() - heureRv) > s) {
			return true;
		}
		return false;	
	}
	
	
	
	// Generer l'arrivee des clients de type B (Verification de son arrivee)
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
	
	
	// Recuperer le conseiller qui doit servir un client specifique
	public Conseiller getConseiller(int numeroConseillerD) {
		Conseiller conseillerB;
		for(int i = 0; i<2 ; i++) {
			if(conseillerList.get(i).numeroConseillerD == numeroConseillerD ) {
				conseillerB = conseillerList.get(i);
				return conseillerB;
			}
		}
		return null;	
	}
	
	
	
	// Gestion de l'evenement Arrivee d'un client A
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
				try {
					ArrayList<Plage> Plage = conseillerB1.getReunions(r, openingTime);
					RendezVous Rv = Plage.get(custB1.numeroClientB).Rv;
					if ((Plage != null) && (Rv != null)) {
						custB1.heureRv = Rv.heureRv;
						
						if ((custB1.heureRv <= Sim.time()) && (conseillerB1.libre == true)) {
							if (retard <= 0) {
								double wait = custB1.heureRv - custB1.arrivalTime;
								statWaitsDayB.add(wait);
							}
							else {
								
								statWaitsDayB.add(0.0);
							}
							conseillerB1.libre = false;
							servListB.addLast(custB1);
							mBusyConseillers++;
							new DepartureB().schedule(custB1.serviceTime);
							conseillerB1.libre = true;
							
							try {
								RendezVous Rv1 =  Plage.get(custB1.numeroClientB + 1).Rv;
								double interprochainheureRv = Rv1.heureRv - Sim.time();
					
								while ((waitListA.size() > 0) && (interprochainheureRv >= s)) {
									//commencer un service pour le client A
									ClientA custA2 = (ClientA)waitListA.removeFirst();
									toWaitA.update(waitListA.size());
									statWaitsDayA.add (Sim.time() -  custA2.arrivalTime);
									servListA.addLast(custA2);
									nextDepartureA.schedule(custA2.serviceTime);
								}
						      }
						      catch (Exception e) { 
						    	 while ((waitListA.size() > 0)) {
										//commencer un service pour le client A
										ClientA custA2 = (ClientA)waitListA.removeFirst();
										toWaitA.update(waitListA.size());
										statWaitsDayA.add (Sim.time() -  custA2.arrivalTime);
										servListA.addLast(custA2);
										nextDepartureA.schedule(custA2.serviceTime);
									}
						      }
						}
						else {
							waitListB.addLast(custB1);
							toWaitB.update(waitListB.size());
						}
					}
				}
			    catch (Exception e) { 

			    }
		
			}
		}
	}
	
	
	
	public void checkQueueB() {
		try {
			if(waitListB.size()>0 && mBusyConseillers < mj) {
				//on parcourt la liste d'attente des clients de type B pour en extraire ceux dont le serveur est libre
				for(int i = 0; i<waitListB.size(); i++) {
					// on recupere le numero du conseiller du client
					int numeroConsCust1 = waitListB.get(i).numeroConseillerD;
					
					
					// Si le conseiller est libre le client passe entre en service
					if(conseillerList.get(numeroConsCust1).libre) {
						
						
						double attente = Sim.time()-waitListB.get(i).arrivalTime;
						waitListB.remove(i);
						toWaitB.update(waitListB.size());
						mBusyConseillers++;
						conseillerList.get(numeroConsCust1).libre = false;
						statWaitsDayB.add(attente);
						nextDepartureB.schedule(waitListB.get(i).serviceTime);
					}
				}
			}
	      }
		
	      catch (Exception e) { 
	       
	      }
	}
	

	
	class DepartureB extends Event {
		public void actions() {	
			try {
				servListB.removeFirst();
				
			}
			catch(Exception e) {
				mBusyConseillers--;
				checkQueueB();
			}
			
	    }
		
	}
	
	
	// Affectiation des valeurs a nos variables en important le fichier SUCCURSALE.JAVA.
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
	        lambdaj = lambda[j];
	        genArrA = new RandomVariateGen(new MRG32k3a(), new PoissonDist(lambdaj));
			
	   
	        nClientAPrevu += genArrA.nextDouble();
	        mClientBPrevu += 4 * mConseillers[j];
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
	      Sim.start();
	      // La simulation  a commence...
	      statArrivalsA.add (nArrivalsA);
	      statArrivalsB.add (mArrivalsB);
	      SumwaitsA.add(statWaitsDayA.sum());
	      SumwaitsB.add(custWaitsB.sum());
	      
	      custWaitsA.add (statWaitsDayA.sum() / nClientAPrevu);
	      custWaitsB.add (statWaitsDayB.sum() / mClientBPrevu);	      
	}

	   
	public static void main (String[] args) throws IOException{ 
	      Succursale cc = new Succursale ("Succursale.dat"); 
	      for (int i=0; i <1000; i++) {
	    	  cc.simulateOneDay();
	      }
	      System.out.println (cc.toWaitA.report());
	      System.out.println (cc.custWaitsA.report()); 
	      System.out.println (cc.statArrivalsA.report());
	      System.out.println ("\n==================================================================================\n");
	      System.out.println (cc.toWaitB.report());
	      System.out.println (cc.custWaitsB.report());
	      System.out.println (cc.statArrivalsB.report());
	      HistogramChart HistoA;	HistogramChart HistoB;
	      
	      //HistoA = new HistogramChart( cc.SumwaitsA);
	      
	      //HistoB = new HistogramChart("temps d'attente par jour pour B", "temps d'attente B", "n", cc.SumwaitsB.getDoubleArrayList());
	      //HistoA.view(600, 600);
		}
	}