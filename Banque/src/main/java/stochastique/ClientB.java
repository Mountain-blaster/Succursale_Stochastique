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
	
	//RandomStream numeroClientB = new MRG32k3a();
	int numeroClientB;
	int numeroConseillerD;
	double heureArrivee;
	
	public ClientB(int numeroConseillerD, double heureArrivee, int numeroClientB) throws IOException {
		
		super();
		
		this.numeroConseillerD = numeroConseillerD;
		this.heureArrivee = heureArrivee;
		this.numeroClientB = numeroClientB;
		// TODO Auto-generated constructor stub
	}
}
