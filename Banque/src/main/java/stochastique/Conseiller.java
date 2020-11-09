package stochastique;
import umontreal.ssj.rng.*;
import java.util.ArrayList;

public class Conseiller {
	
	int numeroConseillerD;
	boolean libre = true; //dit si le conseiller est libre ou pas
	
	
	ArrayList<Plage> liste_des_reunions = new ArrayList<Plage>();
	
	//Constructor;
	public Conseiller(int numeroConseillerD) {
		
		this.numeroConseillerD = numeroConseillerD;
//		this.libre = true;
	}
	
	
	public ArrayList<Plage> getReunions(double r, double openingTime) {
		Plage reunion;
		
		for(int i = 0; i<=11 ; i++) {
			double u = new MRG32k3a().nextDouble();
			
			if(u >= r) {
				double heureRv = openingTime*3600 + i*1800;
				RendezVous Rv = new RendezVous(heureRv, i);
				reunion = new Plage(Rv, i);
			}
			
			else {
				
				RendezVous Rv = null;
				reunion = new Plage(Rv, i);
			}
			
			liste_des_reunions.add(reunion);
		}
		
		return liste_des_reunions;
	}
	/*
	
	public String toString() {
		return "\n Conseiller de numero : " + numeroConseillerD + " est libre ? " + libre;
	}*/
}
