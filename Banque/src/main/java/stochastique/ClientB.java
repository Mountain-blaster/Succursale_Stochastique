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
	int numeroClientB ;
	int numeroConseillerD;
	double heureRv;
	double p; // probabilite de ne pas se presenter
	
	
	public ClientB(int numeroConseillerD) {
		super();	
		this.numeroConseillerD = numeroConseillerD;
		this.numeroClientB = new MRG32k3a().nextInt(0, 11);
		// TODO Auto-generated constructor stub
	}
}
